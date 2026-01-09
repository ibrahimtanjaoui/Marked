package org.mehlib.marked.web;

import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.AttendanceToken;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.TimeTable;
import org.mehlib.marked.dto.ConfirmTokenRequest;
import org.mehlib.marked.dto.JustificationRequest;
import org.mehlib.marked.dto.MarkAttendanceRequest;
import org.mehlib.marked.dto.TokenRequest;
import org.mehlib.marked.service.AttendanceTokenService;
import org.mehlib.marked.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(
        StudentController.class
    );

    private final StudentService studentService;
    private final AttendanceTokenService tokenService;

    public StudentController(
        StudentService studentService,
        AttendanceTokenService tokenService
    ) {
        this.studentService = studentService;
        this.tokenService = tokenService;
    }

    /**
     * Student dashboard/home page.
     * GET /students/{studentId}
     */
    @GetMapping("/{studentId}")
    public String dashboard(@PathVariable Long studentId, Model model) {
        try {
            var student = studentService
                .getUser(studentId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Student not found")
                );

            List<Session> availableSessions =
                studentService.getAvailableSessions(studentId);
            List<Attendance> recentAttendance = studentService.getAttendances(
                studentId
            );
            List<TimeTable> weeklyTimetable = studentService.getWeeklyTimetable(
                studentId
            );
            DayOfWeek currentDayOfWeek =
                java.time.LocalDate.now().getDayOfWeek();

            model.addAttribute("student", student);
            model.addAttribute("availableSessions", availableSessions);
            model.addAttribute("recentAttendance", recentAttendance);
            model.addAttribute("weeklyTimetable", weeklyTimetable);
            model.addAttribute("currentDayOfWeek", currentDayOfWeek.name());

            return "student/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show mark attendance form.
     * GET /students/{studentId}/attendance/mark
     */
    @GetMapping("/{studentId}/attendance/mark")
    public String showMarkAttendanceForm(
        @PathVariable Long studentId,
        @RequestParam(required = false) Long sessionId,
        Model model
    ) {
        try {
            var student = studentService
                .getUser(studentId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Student not found")
                );

            List<Session> availableSessions =
                studentService.getAvailableSessions(studentId);

            model.addAttribute("student", student);
            model.addAttribute("availableSessions", availableSessions);
            model.addAttribute("selectedSessionId", sessionId);
            model.addAttribute(
                "markAttendanceRequest",
                new MarkAttendanceRequest(sessionId, null)
            );
            model.addAttribute(
                "tokenRequest",
                new TokenRequest(sessionId, null, null, null)
            );
            model.addAttribute(
                "confirmTokenRequest",
                new ConfirmTokenRequest(null)
            );
            model.addAttribute("tokenSent", false);

            return "student/mark-attendance";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Process mark attendance form - Step 1: Request verification token.
     * Validates session code, time window, and geolocation.
     * Sends a verification token to the student's email.
     * POST /students/{studentId}/attendance/request-token
     */
    @PostMapping("/{studentId}/attendance/request-token")
    public String requestToken(
        @PathVariable Long studentId,
        @Valid @ModelAttribute TokenRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        var student = studentService.getUser(studentId).orElse(null);
        List<Session> availableSessions = studentService.getAvailableSessions(
            studentId
        );

        if (bindingResult.hasErrors()) {
            model.addAttribute("student", student);
            model.addAttribute("availableSessions", availableSessions);
            model.addAttribute("tokenRequest", request);
            model.addAttribute(
                "confirmTokenRequest",
                new ConfirmTokenRequest(null)
            );
            model.addAttribute("tokenSent", false);
            return "student/mark-attendance";
        }

        try {
            AttendanceToken token = tokenService.requestToken(
                studentId,
                request.sessionId(),
                request.sessionCode(),
                request.latitude(),
                request.longitude()
            );

            log.info(
                "Token {} generated for student {}",
                token.getToken(),
                studentId
            );

            // Redirect back to mark attendance page with step 2 visible
            model.addAttribute("student", student);
            model.addAttribute("availableSessions", availableSessions);
            model.addAttribute("tokenRequest", request);
            model.addAttribute(
                "confirmTokenRequest",
                new ConfirmTokenRequest(null)
            );
            model.addAttribute("tokenSent", true);
            model.addAttribute("tokenSessionId", request.sessionId());
            model.addAttribute(
                "success",
                "Verification code sent to " +
                    student.getEmail() +
                    ". Please check your email and enter the code below."
            );

            return "student/mark-attendance";
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn(
                "Token request failed for student {}: {}",
                studentId,
                e.getMessage()
            );
            model.addAttribute("student", student);
            model.addAttribute("availableSessions", availableSessions);
            model.addAttribute("tokenRequest", request);
            model.addAttribute(
                "confirmTokenRequest",
                new ConfirmTokenRequest(null)
            );
            model.addAttribute("tokenSent", false);
            model.addAttribute("error", e.getMessage());
            return "student/mark-attendance";
        }
    }

    /**
     * Process mark attendance form - Step 2: Confirm with verification token.
     * Validates the token and marks the student as present.
     * POST /students/{studentId}/attendance/confirm-token
     */
    @PostMapping("/{studentId}/attendance/confirm-token")
    public String confirmToken(
        @PathVariable Long studentId,
        @Valid @ModelAttribute ConfirmTokenRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        var student = studentService.getUser(studentId).orElse(null);
        List<Session> availableSessions = studentService.getAvailableSessions(
            studentId
        );

        if (bindingResult.hasErrors()) {
            model.addAttribute("student", student);
            model.addAttribute("availableSessions", availableSessions);
            model.addAttribute(
                "tokenRequest",
                new TokenRequest(null, null, null, null)
            );
            model.addAttribute("confirmTokenRequest", request);
            model.addAttribute("tokenSent", true);
            return "student/mark-attendance";
        }

        try {
            Attendance attendance = tokenService.confirmAttendance(
                studentId,
                request.token()
            );
            log.info(
                "Attendance confirmed for student {} session {}",
                studentId,
                attendance.getSession().getId()
            );

            redirectAttributes.addFlashAttribute(
                "success",
                "Attendance marked successfully! You are now marked as present."
            );
            return "redirect:/students/" + studentId + "/attendance";
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn(
                "Token confirmation failed for student {}: {}",
                studentId,
                e.getMessage()
            );
            model.addAttribute("student", student);
            model.addAttribute("availableSessions", availableSessions);
            model.addAttribute(
                "tokenRequest",
                new TokenRequest(null, null, null, null)
            );
            model.addAttribute("confirmTokenRequest", request);
            model.addAttribute("tokenSent", true);
            model.addAttribute("error", e.getMessage());
            return "student/mark-attendance";
        }
    }

    /**
     * Legacy mark attendance endpoint (redirect to new flow).
     * POST /students/{studentId}/attendance/mark
     */
    @PostMapping("/{studentId}/attendance/mark")
    public String markAttendance(
        @PathVariable Long studentId,
        @Valid @ModelAttribute MarkAttendanceRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        // Redirect to the new two-step flow
        redirectAttributes.addFlashAttribute(
            "info",
            "Please use the new secure attendance marking process."
        );
        return "redirect:/students/" + studentId + "/attendance/mark";
    }

    /**
     * View attendance history.
     * GET /students/{studentId}/attendance
     */
    @GetMapping("/{studentId}/attendance")
    public String viewAttendance(
        @PathVariable Long studentId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime to,
        Model model
    ) {
        try {
            var student = studentService
                .getUser(studentId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Student not found")
                );

            List<Attendance> attendances;
            if (from != null && to != null) {
                attendances = studentService.getAttendances(
                    studentId,
                    from,
                    to
                );
            } else {
                attendances = studentService.getAttendances(studentId);
            }

            model.addAttribute("student", student);
            model.addAttribute("attendances", attendances);
            model.addAttribute("from", from);
            model.addAttribute("to", to);

            return "student/attendance-history";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * View attendance details for a specific session.
     * GET /students/{studentId}/attendance/sessions/{sessionId}
     */
    @GetMapping("/{studentId}/attendance/sessions/{sessionId}")
    public String viewSessionAttendance(
        @PathVariable Long studentId,
        @PathVariable Long sessionId,
        Model model
    ) {
        try {
            var student = studentService
                .getUser(studentId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Student not found")
                );

            var attendance = studentService
                .getSessionAttendance(studentId, sessionId)
                .orElse(null);

            model.addAttribute("student", student);
            model.addAttribute("attendance", attendance);
            model.addAttribute("sessionId", sessionId);

            return "student/attendance-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Show justification form for an absence.
     * GET /students/{studentId}/attendance/sessions/{sessionId}/justify
     */
    @GetMapping("/{studentId}/attendance/sessions/{sessionId}/justify")
    public String showJustificationForm(
        @PathVariable Long studentId,
        @PathVariable Long sessionId,
        Model model
    ) {
        try {
            var student = studentService
                .getUser(studentId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Student not found")
                );

            var attendance = studentService
                .getSessionAttendance(studentId, sessionId)
                .orElse(null);

            model.addAttribute("student", student);
            model.addAttribute("attendance", attendance);
            model.addAttribute("sessionId", sessionId);
            model.addAttribute(
                "justificationRequest",
                new JustificationRequest(null)
            );

            return "student/justify-absence";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Submit justification for an absence.
     * POST /students/{studentId}/attendance/sessions/{sessionId}/justify
     */
    @PostMapping("/{studentId}/attendance/sessions/{sessionId}/justify")
    public String submitJustification(
        @PathVariable Long studentId,
        @PathVariable Long sessionId,
        @Valid @ModelAttribute JustificationRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var student = studentService.getUser(studentId).orElse(null);
            var attendance = studentService
                .getSessionAttendance(studentId, sessionId)
                .orElse(null);
            model.addAttribute("student", student);
            model.addAttribute("attendance", attendance);
            model.addAttribute("sessionId", sessionId);
            return "student/justify-absence";
        }

        try {
            studentService.submitJustification(
                studentId,
                sessionId,
                request.justificationText()
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Justification submitted successfully!"
            );
            return (
                "redirect:/students/" +
                studentId +
                "/attendance/sessions/" +
                sessionId
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            var student = studentService.getUser(studentId).orElse(null);
            var attendance = studentService
                .getSessionAttendance(studentId, sessionId)
                .orElse(null);
            model.addAttribute("student", student);
            model.addAttribute("attendance", attendance);
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("error", e.getMessage());
            return "student/justify-absence";
        }
    }

    /**
     * View available sessions for the student.
     * GET /students/{studentId}/sessions
     */
    @GetMapping("/{studentId}/sessions")
    public String viewSessions(@PathVariable Long studentId, Model model) {
        try {
            var student = studentService
                .getUser(studentId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Student not found")
                );

            List<Session> sessions = studentService.getAvailableSessions(
                studentId
            );

            model.addAttribute("student", student);
            model.addAttribute("sessions", sessions);

            return "student/sessions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
