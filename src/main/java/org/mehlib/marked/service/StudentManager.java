package org.mehlib.marked.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.AttendanceStatus;
import org.mehlib.marked.dao.entities.JustificationStatus;
import org.mehlib.marked.dao.entities.Section;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.SessionType;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.dao.entities.TimeTable;
import org.mehlib.marked.dao.repositories.AttendanceRepository;
import org.mehlib.marked.dao.repositories.SessionRepository;
import org.mehlib.marked.dao.repositories.StudentRepository;
import org.mehlib.marked.dao.repositories.TimeTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentManager
    extends UserManager<Student>
    implements StudentService
{

    private static final int TIME_WINDOW_MINUTES_BEFORE = 5;

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final TimeTableRepository timeTableRepository;

    public StudentManager(
        StudentRepository studentRepository,
        AttendanceRepository attendanceRepository,
        SessionRepository sessionRepository,
        TimeTableRepository timeTableRepository
    ) {
        super(studentRepository);
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.timeTableRepository = timeTableRepository;
    }

    @Override
    public Attendance markAttendance(
        Long studentId,
        Long sessionId,
        String sessionCode
    ) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Session not found with ID: " + sessionId
                )
            );

        // Validate session is not cancelled
        if (session.getType() == SessionType.CANCELLED) {
            throw new IllegalStateException(
                "Cannot mark attendance for a cancelled session"
            );
        }

        // Validate student's section is part of this session
        validateStudentSectionInSession(student, session);

        // Validate session code
        validateSessionCode(session, sessionCode);

        // Validate time window
        validateTimeWindow(session);

        // Check if attendance already exists
        Optional<Attendance> existingAttendance =
            attendanceRepository.findByStudentAndSession(student, session);

        if (existingAttendance.isPresent()) {
            Attendance existing = existingAttendance.get();
            // If already marked present, return existing
            if (existing.getStatus() == AttendanceStatus.PRESENT) {
                return existing;
            }
            // If was marked absent/not marked, update to present
            existing.setStatus(AttendanceStatus.PRESENT);
            return attendanceRepository.save(existing);
        }

        // Create new attendance record
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSession(session);
        attendance.setStatus(AttendanceStatus.PRESENT);

        return attendanceRepository.save(attendance);
    }

    private void validateStudentSectionInSession(
        Student student,
        Session session
    ) {
        Section studentSection = student.getSection();
        if (studentSection == null) {
            throw new IllegalStateException(
                "Student is not assigned to any section"
            );
        }

        boolean sectionInSession = session
            .getSections()
            .stream()
            .anyMatch(s -> s.getId().equals(studentSection.getId()));

        if (!sectionInSession) {
            throw new IllegalStateException(
                "Student's section is not scheduled for this session"
            );
        }
    }

    private void validateSessionCode(Session session, String providedCode) {
        String expectedCode = session.getSessionCode();

        if (expectedCode == null || expectedCode.isBlank()) {
            throw new IllegalStateException(
                "Session code has not been generated for this session"
            );
        }

        if (providedCode == null || providedCode.isBlank()) {
            throw new IllegalArgumentException("Session code is required");
        }

        if (!expectedCode.equals(providedCode.trim())) {
            throw new IllegalStateException("Invalid session code");
        }
    }

    private void validateTimeWindow(Session session) {
        LocalTime sessionStart = session.getStartTime();
        LocalTime sessionEnd = session.getEndTime();

        if (sessionStart == null || sessionEnd == null) {
            throw new IllegalStateException("Session times are not configured");
        }

        if (
            session.getCalendar() == null ||
            session.getCalendar().getDate() == null
        ) {
            throw new IllegalStateException("Session date is not configured");
        }

        LocalDate sessionDate = session.getCalendar().getDate().toLocalDate();
        LocalDateTime windowStart = LocalDateTime.of(
            sessionDate,
            sessionStart.minusMinutes(TIME_WINDOW_MINUTES_BEFORE)
        );
        LocalDateTime windowEnd = LocalDateTime.of(sessionDate, sessionEnd);

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(windowStart)) {
            throw new IllegalStateException(
                "Attendance marking has not started yet. Session starts at " +
                    sessionStart
            );
        }

        if (now.isAfter(windowEnd)) {
            throw new IllegalStateException(
                "Attendance marking window has closed. Session ended at " +
                    sessionEnd
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendances(Long studentId) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        return attendanceRepository.findByStudentOrderBySessionDateDesc(
            student
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendances(
        Long studentId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        return attendanceRepository.findByStudentAndDateRange(
            student,
            startDate,
            endDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Attendance> getSessionAttendance(
        Long studentId,
        Long sessionId
    ) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Session not found with ID: " + sessionId
                )
            );

        return attendanceRepository.findByStudentAndSession(student, session);
    }

    @Override
    public Attendance submitJustification(
        Long studentId,
        Long sessionId,
        String justificationText
    ) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Session not found with ID: " + sessionId
                )
            );

        // Find or create attendance record
        Attendance attendance = attendanceRepository
            .findByStudentAndSession(student, session)
            .orElseGet(() -> {
                Attendance newAttendance = new Attendance();
                newAttendance.setStudent(student);
                newAttendance.setSession(session);
                newAttendance.setStatus(AttendanceStatus.ABSENT);
                return newAttendance;
            });

        // Can only justify if absent or not marked
        if (attendance.getStatus() == AttendanceStatus.PRESENT) {
            throw new IllegalStateException(
                "Cannot justify attendance for a session where student was present"
            );
        }

        // Check if already has an approved justification
        if (
            attendance.getJustificationStatus() == JustificationStatus.APPROVED
        ) {
            throw new IllegalStateException(
                "Justification has already been approved"
            );
        }

        attendance.setJustificationText(justificationText);
        attendance.setJustificationStatus(JustificationStatus.PENDING);
        attendance.setJustificationSubmittedAt(Instant.now());

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getAvailableSessions(Long studentId) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        if (student.getSection() == null) {
            return List.of();
        }

        // Get sessions from today onwards
        LocalDateTime today = LocalDate.now().atStartOfDay();
        return sessionRepository.findUpcomingSessionsBySectionId(
            student.getSection().getId(),
            today
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeTable> getWeeklyTimetable(Long studentId) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        if (student.getSection() == null) {
            return List.of();
        }

        org.mehlib.marked.dao.entities.Class academicClass = student
            .getSection()
            .getAcademicClass();

        if (academicClass == null) {
            return List.of();
        }

        return timeTableRepository.findByClassIdWithDetails(
            academicClass.getId()
        );
    }
}
