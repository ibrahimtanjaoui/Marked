package org.mehlib.marked.web;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.TimeTable;
import org.mehlib.marked.dto.JustificationReviewRequest;
import org.mehlib.marked.service.AttendanceExportService;
import org.mehlib.marked.service.ProfessorService;
import org.mehlib.marked.service.SessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/professors")
public class ProfessorController {

    private final ProfessorService professorService;
    private final SessionService sessionService;
    private final AttendanceExportService attendanceExportService;

    public ProfessorController(
        ProfessorService professorService,
        SessionService sessionService,
        AttendanceExportService attendanceExportService
    ) {
        this.professorService = professorService;
        this.sessionService = sessionService;
        this.attendanceExportService = attendanceExportService;
    }

    /**
     * Professor dashboard/home page.
     * GET /professors/{professorId}
     */
    @GetMapping("/{professorId}")
    public String dashboard(@PathVariable Long professorId, Model model) {
        try {
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            List<Session> sessions = professorService.getProfessorSessions(
                professorId
            );
            List<Attendance> pendingJustifications =
                professorService.getPendingJustifications(professorId);
            List<TimeTable> weeklyTimetable =
                professorService.getWeeklyTimetable(professorId);
            DayOfWeek currentDayOfWeek = LocalDate.now().getDayOfWeek();

            model.addAttribute("professor", professor);
            model.addAttribute("sessions", sessions);
            model.addAttribute("pendingJustifications", pendingJustifications);
            model.addAttribute("weeklyTimetable", weeklyTimetable);
            model.addAttribute("currentDayOfWeek", currentDayOfWeek.name());

            return "professor/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * View all sessions taught by the professor with pagination and search.
     * GET /professors/{professorId}/sessions
     */
    @GetMapping("/{professorId}/sessions")
    public String viewSessions(
        @PathVariable Long professorId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String courseFilter,
        Model model
    ) {
        try {
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            Pageable pageable = PageRequest.of(page, size);

            Page<Session> sessionsPage;
            if (search != null && !search.trim().isEmpty()) {
                sessionsPage = professorService.searchProfessorSessions(
                    professorId,
                    search,
                    pageable
                );
            } else if (courseFilter != null && !courseFilter.trim().isEmpty()) {
                sessionsPage = professorService.getProfessorSessionsByCourse(
                    professorId,
                    Long.parseLong(courseFilter),
                    pageable
                );
            } else {
                sessionsPage = professorService.getProfessorSessions(
                    professorId,
                    pageable
                );
            }

            model.addAttribute("professor", professor);
            model.addAttribute("sessionsPage", sessionsPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", sessionsPage.getTotalPages());
            model.addAttribute("totalItems", sessionsPage.getTotalElements());
            model.addAttribute("search", search);
            model.addAttribute("courseFilter", courseFilter);

            return "professor/sessions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * View session details with attendance list.
     * GET /professors/{professorId}/sessions/{sessionId}
     */
    @GetMapping("/{professorId}/sessions/{sessionId}")
    public String viewSessionDetails(
        @PathVariable Long professorId,
        @PathVariable Long sessionId,
        Model model
    ) {
        try {
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            var classSession = sessionService
                .getSessionWithDetails(sessionId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Session not found")
                );

            List<Attendance> attendances =
                professorService.getSessionAttendance(professorId, sessionId);

            model.addAttribute("professor", professor);
            model.addAttribute("classSession", classSession);
            model.addAttribute("attendances", attendances);

            return "professor/session-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Generate a session code for attendance marking.
     * POST /professors/{professorId}/sessions/{sessionId}/generate-code
     */
    @PostMapping("/{professorId}/sessions/{sessionId}/generate-code")
    public String generateSessionCode(
        @PathVariable Long professorId,
        @PathVariable Long sessionId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            sessionService.generateSessionCode(sessionId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Session code generated successfully!"
            );
            return (
                "redirect:/professors/" + professorId + "/sessions/" + sessionId
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return (
                "redirect:/professors/" + professorId + "/sessions/" + sessionId
            );
        }
    }

    /**
     * Regenerate the session code.
     * POST /professors/{professorId}/sessions/{sessionId}/regenerate-code
     */
    @PostMapping("/{professorId}/sessions/{sessionId}/regenerate-code")
    public String regenerateSessionCode(
        @PathVariable Long professorId,
        @PathVariable Long sessionId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            sessionService.regenerateSessionCode(sessionId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Session code regenerated successfully!"
            );
            return (
                "redirect:/professors/" + professorId + "/sessions/" + sessionId
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return (
                "redirect:/professors/" + professorId + "/sessions/" + sessionId
            );
        }
    }

    /**
     * View class attendance with date range filter.
     * GET /professors/{professorId}/classes/{classId}/attendance
     */
    @GetMapping("/{professorId}/classes/{classId}/attendance")
    public String viewClassAttendance(
        @PathVariable Long professorId,
        @PathVariable Long classId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime to,
        Model model
    ) {
        try {
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            // Default to last 30 days if no date range provided
            LocalDateTime endDate = to != null ? to : LocalDateTime.now();
            LocalDateTime startDate =
                from != null ? from : endDate.minusDays(30);

            List<Attendance> attendances = professorService.getClassAttendance(
                professorId,
                classId,
                startDate,
                endDate
            );

            model.addAttribute("professor", professor);
            model.addAttribute("classId", classId);
            model.addAttribute("attendances", attendances);
            model.addAttribute("from", startDate);
            model.addAttribute("to", endDate);

            return "professor/class-attendance";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * View student attendance within a class.
     * GET /professors/{professorId}/classes/{classId}/students/{studentId}/attendance
     */
    @GetMapping(
        "/{professorId}/classes/{classId}/students/{studentId}/attendance"
    )
    public String viewStudentAttendance(
        @PathVariable Long professorId,
        @PathVariable Long classId,
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
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            // Default to last 30 days if no date range provided
            LocalDateTime endDate = to != null ? to : LocalDateTime.now();
            LocalDateTime startDate =
                from != null ? from : endDate.minusDays(30);

            List<Attendance> attendances =
                professorService.getStudentAttendance(
                    professorId,
                    classId,
                    studentId,
                    startDate,
                    endDate
                );

            model.addAttribute("professor", professor);
            model.addAttribute("classId", classId);
            model.addAttribute("studentId", studentId);
            model.addAttribute("attendances", attendances);
            model.addAttribute("from", startDate);
            model.addAttribute("to", endDate);

            return "professor/student-attendance";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * View all justifications for the professor with pagination and search.
     * GET /professors/{professorId}/justifications
     */
    @GetMapping("/{professorId}/justifications")
    public String viewAllJustifications(
        @PathVariable Long professorId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String statusFilter,
        Model model
    ) {
        try {
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            List<Attendance> allJustifications =
                professorService.getAllJustifications(professorId);

            // Filter by search and status
            List<Attendance> filteredJustifications = allJustifications;

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredJustifications = filteredJustifications
                    .stream()
                    .filter(
                        att ->
                            att
                                .getStudent()
                                .getFullName()
                                .toLowerCase()
                                .contains(searchLower) ||
                            att
                                .getStudent()
                                .getStudentId()
                                .toLowerCase()
                                .contains(searchLower) ||
                            att
                                .getSession()
                                .getTimeTable()
                                .getCourseAssignment()
                                .getCourse()
                                .getName()
                                .toLowerCase()
                                .contains(searchLower)
                    )
                    .toList();
            }

            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                filteredJustifications = filteredJustifications
                    .stream()
                    .filter(
                        att ->
                            att.getJustificationStatus() != null &&
                            att
                                .getJustificationStatus()
                                .name()
                                .equalsIgnoreCase(statusFilter)
                    )
                    .toList();
            }

            // Pagination logic
            int start = page * size;
            int end = Math.min(start + size, filteredJustifications.size());
            List<Attendance> pageContent =
                start < filteredJustifications.size()
                    ? filteredJustifications.subList(start, end)
                    : List.of();

            int totalPages = (int) Math.ceil(
                (double) filteredJustifications.size() / size
            );

            // Count pending for the badge
            long pendingCount = allJustifications
                .stream()
                .filter(
                    a ->
                        a.getJustificationStatus() != null &&
                        a.getJustificationStatus().name().equals("PENDING")
                )
                .count();

            model.addAttribute("professor", professor);
            model.addAttribute("justifications", pageContent);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", filteredJustifications.size());
            model.addAttribute("search", search);
            model.addAttribute("statusFilter", statusFilter);

            return "professor/justifications";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * View justification details.
     * GET /professors/{professorId}/justifications/{attendanceId}
     */
    @GetMapping("/{professorId}/justifications/{attendanceId}")
    public String viewJustificationDetails(
        @PathVariable Long professorId,
        @PathVariable Long attendanceId,
        Model model
    ) {
        try {
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            // Get the attendance record by ID
            var attendance = professorService
                .getAttendanceById(professorId, attendanceId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Attendance record not found")
                );

            model.addAttribute("professor", professor);
            model.addAttribute("attendance", attendance);
            model.addAttribute(
                "reviewRequest",
                new JustificationReviewRequest(null)
            );

            return "professor/justification-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Approve a justification.
     * POST /professors/{professorId}/justifications/{attendanceId}/approve
     */
    @PostMapping("/{professorId}/justifications/{attendanceId}/approve")
    public String approveJustification(
        @PathVariable Long professorId,
        @PathVariable Long attendanceId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            professorService.approveJustification(professorId, attendanceId);
            redirectAttributes.addFlashAttribute(
                "success",
                "Justification approved successfully!"
            );
            return "redirect:/professors/" + professorId + "/justifications";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return (
                "redirect:/professors/" +
                professorId +
                "/justifications/" +
                attendanceId
            );
        }
    }

    /**
     * Reject a justification.
     * POST /professors/{professorId}/justifications/{attendanceId}/reject
     */
    @PostMapping("/{professorId}/justifications/{attendanceId}/reject")
    public String rejectJustification(
        @PathVariable Long professorId,
        @PathVariable Long attendanceId,
        @Valid @ModelAttribute JustificationReviewRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            var professor = professorService.getUser(professorId).orElse(null);
            model.addAttribute("professor", professor);
            model.addAttribute("attendanceId", attendanceId);
            return "professor/justification-detail";
        }

        try {
            String reason = request != null ? request.reason() : null;
            professorService.rejectJustification(
                professorId,
                attendanceId,
                reason
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Justification rejected."
            );
            return "redirect:/professors/" + professorId + "/justifications";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return (
                "redirect:/professors/" +
                professorId +
                "/justifications/" +
                attendanceId
            );
        }
    }

    /**
     * Update attendance status for a student.
     * POST /professors/{professorId}/sessions/{sessionId}/attendance/{attendanceId}/status
     */
    @PostMapping(
        "/{professorId}/sessions/{sessionId}/attendance/{attendanceId}/status"
    )
    public String updateAttendanceStatus(
        @PathVariable Long professorId,
        @PathVariable Long sessionId,
        @PathVariable Long attendanceId,
        @RequestParam String status,
        RedirectAttributes redirectAttributes
    ) {
        try {
            professorService.updateAttendanceStatus(
                professorId,
                attendanceId,
                status
            );
            redirectAttributes.addFlashAttribute(
                "success",
                "Attendance status updated successfully."
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return (
            "redirect:/professors/" + professorId + "/sessions/" + sessionId
        );
    }

    /**
     * Export session attendance to Excel.
     * GET /professors/{professorId}/sessions/{sessionId}/export
     */
    @GetMapping("/{professorId}/sessions/{sessionId}/export")
    public ResponseEntity<byte[]> exportSessionAttendance(
        @PathVariable Long professorId,
        @PathVariable Long sessionId
    ) {
        try {
            byte[] excelData = attendanceExportService.exportSessionAttendance(
                sessionId
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            );
            headers.setContentDispositionFormData(
                "attachment",
                "session_" + sessionId + "_attendance.xlsx"
            );

            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export course attendance to Excel (all sessions).
     * GET /professors/{professorId}/courses/{courseAssignmentId}/export
     */
    @GetMapping("/{professorId}/courses/{courseAssignmentId}/export")
    public ResponseEntity<byte[]> exportCourseAttendance(
        @PathVariable Long professorId,
        @PathVariable Long courseAssignmentId
    ) {
        try {
            byte[] excelData = attendanceExportService.exportCourseAttendance(
                courseAssignmentId
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            );
            headers.setContentDispositionFormData(
                "attachment",
                "course_" + courseAssignmentId + "_attendance.xlsx"
            );

            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Export student attendance for a course to Excel.
     * GET /professors/{professorId}/courses/{courseAssignmentId}/students/{studentId}/export
     */
    @GetMapping(
        "/{professorId}/courses/{courseAssignmentId}/students/{studentId}/export"
    )
    public ResponseEntity<byte[]> exportStudentAttendance(
        @PathVariable Long professorId,
        @PathVariable Long courseAssignmentId,
        @PathVariable Long studentId
    ) {
        try {
            byte[] excelData = attendanceExportService.exportStudentAttendance(
                studentId,
                courseAssignmentId
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            );
            headers.setContentDispositionFormData(
                "attachment",
                "student_" +
                    studentId +
                    "_course_" +
                    courseAssignmentId +
                    "_attendance.xlsx"
            );

            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * View attendance search page.
     * GET /professors/{professorId}/attendance/search
     */
    @GetMapping("/{professorId}/attendance/search")
    public String attendanceSearch(
        @PathVariable Long professorId,
        @RequestParam(required = false) String studentSearch,
        @RequestParam(required = false) String courseSearch,
        @RequestParam(required = false) @DateTimeFormat(
            pattern = "yyyy-MM-dd"
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            pattern = "yyyy-MM-dd"
        ) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Model model
    ) {
        try {
            var professor = professorService
                .getUser(professorId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Professor not found")
                );

            model.addAttribute("professor", professor);
            model.addAttribute("studentSearch", studentSearch);
            model.addAttribute("courseSearch", courseSearch);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            // Get professor's sessions for filtering
            List<Session> allSessions = professorService.getProfessorSessions(
                professorId
            );

            // Filter sessions based on search criteria
            List<Attendance> attendances = new ArrayList<>();

            if (
                studentSearch != null ||
                courseSearch != null ||
                startDate != null ||
                endDate != null
            ) {
                for (Session session : allSessions) {
                    // Check course filter
                    if (
                        courseSearch != null && !courseSearch.trim().isEmpty()
                    ) {
                        String courseName = session
                            .getTimeTable()
                            .getCourseAssignment()
                            .getCourse()
                            .getName()
                            .toLowerCase();
                        if (!courseName.contains(courseSearch.toLowerCase())) {
                            continue;
                        }
                    }

                    // Check date filter
                    if (
                        startDate != null &&
                        session
                            .getCalendar()
                            .getDate()
                            .toLocalDate()
                            .isBefore(startDate)
                    ) {
                        continue;
                    }
                    if (
                        endDate != null &&
                        session
                            .getCalendar()
                            .getDate()
                            .toLocalDate()
                            .isAfter(endDate)
                    ) {
                        continue;
                    }

                    // Get attendance for this session
                    List<Attendance> sessionAttendances =
                        professorService.getSessionAttendance(
                            professorId,
                            session.getId()
                        );

                    // Filter by student if needed
                    if (
                        studentSearch != null && !studentSearch.trim().isEmpty()
                    ) {
                        sessionAttendances = sessionAttendances
                            .stream()
                            .filter(att -> {
                                String studentName = att
                                    .getStudent()
                                    .getFullName()
                                    .toLowerCase();
                                String studentId = att
                                    .getStudent()
                                    .getStudentId()
                                    .toLowerCase();
                                String search = studentSearch.toLowerCase();
                                return (
                                    studentName.contains(search) ||
                                    studentId.contains(search)
                                );
                            })
                            .toList();
                    }

                    attendances.addAll(sessionAttendances);
                }
            }

            model.addAttribute("attendances", attendances);
            model.addAttribute("totalResults", attendances.size());

            return "professor/attendance-search";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
