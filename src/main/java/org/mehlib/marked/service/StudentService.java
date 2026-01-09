package org.mehlib.marked.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.dao.entities.TimeTable;

public interface StudentService extends UserService<Student> {
    /**
     * Mark attendance for a student in a specific session.
     * Validates session code and time window for anti-cheat.
     *
     * @param studentId the ID of the student
     * @param sessionId the ID of the session
     * @param sessionCode the 6-digit code displayed by professor
     * @return the created or updated attendance record
     * @throws IllegalArgumentException if student/session not found
     * @throws IllegalStateException if code is invalid, time window expired, or student's section not in session
     */
    Attendance markAttendance(
        Long studentId,
        Long sessionId,
        String sessionCode
    );

    /**
     * Get all attendance records for a student.
     *
     * @param studentId the ID of the student
     * @return list of attendance records
     */
    List<Attendance> getAttendances(Long studentId);

    /**
     * Get attendance records for a student within a date range.
     *
     * @param studentId the ID of the student
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of attendance records within the date range
     */
    List<Attendance> getAttendances(
        Long studentId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Get attendance for a specific session.
     *
     * @param studentId the ID of the student
     * @param sessionId the ID of the session
     * @return the attendance record if exists
     */
    Optional<Attendance> getSessionAttendance(Long studentId, Long sessionId);

    /**
     * Submit a justification for an absence.
     *
     * @param studentId the ID of the student
     * @param sessionId the ID of the session
     * @param justificationText the justification text
     * @return the updated attendance record
     */
    Attendance submitJustification(
        Long studentId,
        Long sessionId,
        String justificationText
    );

    /**
     * Get all sessions available for the student's section.
     *
     * @param studentId the ID of the student
     * @return list of sessions
     */
    List<Session> getAvailableSessions(Long studentId);

    /**
     * Find a student by their student ID (not database ID).
     *
     * @param studentId the student's institutional ID
     * @return the student if found
     */
    Optional<Student> findByStudentId(String studentId);

    /**
     * Find a student by their email.
     *
     * @param email the student's email
     * @return the student if found
     */
    Optional<Student> findByEmail(String email);

    /**
     * Get the weekly timetable for a student based on their class.
     *
     * @param studentId the ID of the student
     * @return list of timetable entries ordered by day of week and start time
     */
    List<TimeTable> getWeeklyTimetable(Long studentId);
}
