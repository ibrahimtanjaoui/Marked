package org.mehlib.marked.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.AttendanceStatus;
import org.mehlib.marked.dao.entities.JustificationStatus;
import org.mehlib.marked.dao.entities.Professor;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.dao.entities.TimeTable;
import org.mehlib.marked.dao.repositories.AttendanceRepository;
import org.mehlib.marked.dao.repositories.ProfessorRepository;
import org.mehlib.marked.dao.repositories.SessionRepository;
import org.mehlib.marked.dao.repositories.StudentRepository;
import org.mehlib.marked.dao.repositories.TimeTableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfessorManager
    extends UserManager<Professor>
    implements ProfessorService
{

    private final ProfessorRepository professorRepository;
    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final TimeTableRepository timeTableRepository;

    public ProfessorManager(
        ProfessorRepository professorRepository,
        AttendanceRepository attendanceRepository,
        SessionRepository sessionRepository,
        StudentRepository studentRepository,
        TimeTableRepository timeTableRepository
    ) {
        super(professorRepository);
        this.professorRepository = professorRepository;
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.timeTableRepository = timeTableRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getClassAttendance(
        Long professorId,
        Long classId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        return attendanceRepository.findByClassIdAndDateRange(
            classId,
            startDate,
            endDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getStudentAttendance(
        Long professorId,
        Long classId,
        Long studentId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        return attendanceRepository.findByStudentAndClassIdAndDateRange(
            student,
            classId,
            startDate,
            endDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getSessionAttendance(
        Long professorId,
        Long sessionId
    ) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Session not found with ID: " + sessionId
                )
            );

        return attendanceRepository.findBySession(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getPendingJustifications(Long professorId) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        return attendanceRepository.findPendingJustificationsByProfessor(
            JustificationStatus.PENDING,
            professorId
        );
    }

    @Override
    public Attendance approveJustification(
        Long professorId,
        Long attendanceId
    ) {
        Professor professor = professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        Attendance attendance = attendanceRepository
            .findById(attendanceId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Attendance not found with ID: " + attendanceId
                )
            );

        // Validate that justification is pending
        if (
            attendance.getJustificationStatus() != JustificationStatus.PENDING
        ) {
            throw new IllegalStateException(
                "Justification is not pending review"
            );
        }

        attendance.setJustificationStatus(JustificationStatus.APPROVED);
        attendance.setJustificationReviewedAt(Instant.now());
        attendance.setJustificationReviewedBy(professor);
        attendance.setStatus(AttendanceStatus.EXCUSED);

        return attendanceRepository.save(attendance);
    }

    @Override
    public Attendance rejectJustification(
        Long professorId,
        Long attendanceId,
        String reason
    ) {
        Professor professor = professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        Attendance attendance = attendanceRepository
            .findById(attendanceId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Attendance not found with ID: " + attendanceId
                )
            );

        // Validate that justification is pending
        if (
            attendance.getJustificationStatus() != JustificationStatus.PENDING
        ) {
            throw new IllegalStateException(
                "Justification is not pending review"
            );
        }

        attendance.setJustificationStatus(JustificationStatus.REJECTED);
        attendance.setJustificationReviewedAt(Instant.now());
        attendance.setJustificationReviewedBy(professor);

        // Add rejection reason to comment if provided
        if (reason != null && !reason.isBlank()) {
            String existingComment = attendance.getComment();
            if (existingComment != null && !existingComment.isBlank()) {
                attendance.setComment(
                    existingComment + " | Rejection reason: " + reason
                );
            } else {
                attendance.setComment("Rejection reason: " + reason);
            }
        }

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getProfessorSessions(Long professorId) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        return sessionRepository.findByProfessorId(professorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Professor> findByEmail(String email) {
        return professorRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Attendance> getAttendanceById(
        Long professorId,
        Long attendanceId
    ) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        return attendanceRepository.findByIdWithDetails(attendanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAllJustifications(Long professorId) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        return attendanceRepository.findAllJustificationsByProfessor(
            professorId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeTable> getWeeklyTimetable(Long professorId) {
        // Verify professor exists
        professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        return timeTableRepository.findByProfessorIdWithDetails(professorId);
    }

    @Override
    @Transactional
    public Attendance updateAttendanceStatus(
        Long professorId,
        Long attendanceId,
        String status
    ) {
        // Verify professor exists
        Professor professor = professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        // Get the attendance record
        Attendance attendance = attendanceRepository
            .findById(attendanceId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Attendance record not found with ID: " + attendanceId
                )
            );

        // Verify professor is authorized to update this attendance
        // (they must be the professor teaching this session)
        if (
            attendance.getSession() == null ||
            attendance.getSession().getTimeTable() == null ||
            attendance.getSession().getTimeTable().getCourseAssignment() ==
            null ||
            attendance
                .getSession()
                .getTimeTable()
                .getCourseAssignment()
                .getProfessor() ==
            null ||
            !attendance
                .getSession()
                .getTimeTable()
                .getCourseAssignment()
                .getProfessor()
                .getId()
                .equals(professorId)
        ) {
            throw new IllegalArgumentException(
                "You are not authorized to update attendance for this session"
            );
        }

        // Parse and validate the status
        AttendanceStatus newStatus;
        try {
            newStatus = AttendanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid attendance status: " + status
            );
        }

        // Update the status
        attendance.setStatus(newStatus);

        // If marking as EXCUSED, also set the justification status to APPROVED
        if (newStatus == AttendanceStatus.EXCUSED) {
            attendance.setJustificationStatus(JustificationStatus.APPROVED);
            attendance.setJustificationReviewedAt(Instant.now());
            attendance.setJustificationReviewedBy(professor);
        }

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Session> getProfessorSessions(
        Long professorId,
        Pageable pageable
    ) {
        List<Session> allSessions = sessionRepository.findByProfessorId(
            professorId
        );
        return convertListToPage(allSessions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Session> searchProfessorSessions(
        Long professorId,
        String searchTerm,
        Pageable pageable
    ) {
        List<Session> allSessions = sessionRepository.findByProfessorId(
            professorId
        );

        // Filter sessions by search term (course name or description)
        List<Session> filteredSessions = allSessions
            .stream()
            .filter(session -> {
                String courseName = session
                    .getTimeTable()
                    .getCourseAssignment()
                    .getCourse()
                    .getName()
                    .toLowerCase();
                return courseName.contains(searchTerm.toLowerCase());
            })
            .toList();

        return convertListToPage(filteredSessions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Session> getProfessorSessionsByCourse(
        Long professorId,
        Long courseAssignmentId,
        Pageable pageable
    ) {
        List<Session> allSessions = sessionRepository.findByCourseAssignmentId(
            courseAssignmentId
        );

        // Verify professor authorization
        if (!allSessions.isEmpty()) {
            Long sessionProfessorId = allSessions
                .get(0)
                .getTimeTable()
                .getCourseAssignment()
                .getProfessor()
                .getId();

            if (!sessionProfessorId.equals(professorId)) {
                throw new IllegalArgumentException(
                    "You are not authorized to view sessions for this course"
                );
            }
        }

        return convertListToPage(allSessions, pageable);
    }

    /**
     * Helper method to convert a list to a Page.
     */
    private Page<Session> convertListToPage(
        List<Session> list,
        Pageable pageable
    ) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }

        List<Session> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }
}
