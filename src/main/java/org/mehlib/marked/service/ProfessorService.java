package org.mehlib.marked.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.Professor;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.TimeTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfessorService extends UserService<Professor> {
    /**
     * Get all attendance records for a specific class within a date range.
     *
     * @param professorId the ID of the professor (for authorization)
     * @param classId the ID of the academic class
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of attendance records
     */
    List<Attendance> getClassAttendance(
        Long professorId,
        Long classId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Get attendance records for a specific student in a class within a date range.
     *
     * @param professorId the ID of the professor (for authorization)
     * @param classId the ID of the academic class
     * @param studentId the ID of the student
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of attendance records for the student
     */
    List<Attendance> getStudentAttendance(
        Long professorId,
        Long classId,
        Long studentId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Get attendance records for a specific session.
     *
     * @param professorId the ID of the professor (for authorization)
     * @param sessionId the ID of the session
     * @return list of attendance records for the session
     */
    List<Attendance> getSessionAttendance(Long professorId, Long sessionId);

    /**
     * Get all pending justifications for the professor's classes.
     *
     * @param professorId the ID of the professor
     * @return list of attendance records with pending justifications
     */
    List<Attendance> getPendingJustifications(Long professorId);

    /**
     * Get all justifications for the professor's classes (pending, approved, rejected).
     *
     * @param professorId the ID of the professor
     * @return list of attendance records with any justification status
     */
    List<Attendance> getAllJustifications(Long professorId);

    /**
     * Approve a justification for an absence.
     *
     * @param professorId the ID of the professor reviewing
     * @param attendanceId the ID of the attendance record
     * @return the updated attendance record
     */
    Attendance approveJustification(Long professorId, Long attendanceId);

    /**
     * Reject a justification for an absence.
     *
     * @param professorId the ID of the professor reviewing
     * @param attendanceId the ID of the attendance record
     * @param reason optional reason for rejection
     * @return the updated attendance record
     */
    Attendance rejectJustification(
        Long professorId,
        Long attendanceId,
        String reason
    );

    /**
     * Get all sessions taught by the professor.
     *
     * @param professorId the ID of the professor
     * @return list of sessions
     */
    List<Session> getProfessorSessions(Long professorId);

    /**
     * Get paginated sessions taught by the professor.
     *
     * @param professorId the ID of the professor
     * @param pageable pagination information
     * @return page of sessions
     */
    Page<Session> getProfessorSessions(Long professorId, Pageable pageable);

    /**
     * Search professor's sessions by course name or other criteria.
     *
     * @param professorId the ID of the professor
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching sessions
     */
    Page<Session> searchProfessorSessions(
        Long professorId,
        String searchTerm,
        Pageable pageable
    );

    /**
     * Get professor's sessions filtered by course.
     *
     * @param professorId the ID of the professor
     * @param courseAssignmentId the course assignment ID
     * @param pageable pagination information
     * @return page of sessions for the course
     */
    Page<Session> getProfessorSessionsByCourse(
        Long professorId,
        Long courseAssignmentId,
        Pageable pageable
    );

    /**
     * Find a professor by their email.
     *
     * @param email the professor's email
     * @return the professor if found
     */
    Optional<Professor> findByEmail(String email);

    /**
     * Get an attendance record by ID (for viewing justification details).
     *
     * @param professorId the ID of the professor (for authorization)
     * @param attendanceId the ID of the attendance record
     * @return the attendance record if found and professor is authorized
     */
    Optional<Attendance> getAttendanceById(Long professorId, Long attendanceId);

    /**
     * Get the weekly timetable for a professor (classes they teach).
     *
     * @param professorId the ID of the professor
     * @return list of timetable entries ordered by day of week and start time
     */
    List<TimeTable> getWeeklyTimetable(Long professorId);

    /**
     * Update the attendance status for a student in a session.
     *
     * @param professorId the ID of the professor (for authorization)
     * @param attendanceId the ID of the attendance record
     * @param status the new attendance status (PRESENT, ABSENT, LATE, EXCUSED)
     * @return the updated attendance record
     */
    Attendance updateAttendanceStatus(
        Long professorId,
        Long attendanceId,
        String status
    );
}
