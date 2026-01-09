package org.mehlib.marked.service;

import java.time.LocalDate;
import java.util.List;
import org.mehlib.marked.dao.entities.Session;

/**
 * Service for generating Session instances from TimeTable and Calendar data.
 *
 * The logic is:
 * - TimeTable defines recurring schedule (e.g., "Monday 10:00-12:00")
 * - Calendar defines actual dates and their types (WORKDAY, HOLIDAY, WEEKEND)
 * - Session is the actual class instance on a specific date
 *
 * This service creates Sessions by matching TimeTable entries with Calendar dates.
 */
public interface SessionGenerationService {

    /**
     * Generate sessions for a specific semester within its date range.
     * This will create sessions for all course assignments in the semester.
     *
     * @param semesterId the ID of the semester
     * @return list of created sessions
     */
    List<Session> generateSessionsForSemester(Long semesterId);

    /**
     * Generate sessions for a specific course assignment within a date range.
     *
     * @param courseAssignmentId the ID of the course assignment
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of created sessions
     */
    List<Session> generateSessionsForCourseAssignment(
        Long courseAssignmentId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Generate sessions for a specific timetable entry within a date range.
     *
     * @param timeTableId the ID of the timetable entry
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of created sessions
     */
    List<Session> generateSessionsForTimeTable(
        Long timeTableId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Generate sessions for all timetables in an institution within a date range.
     *
     * @param institutionId the ID of the institution
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of created sessions
     */
    List<Session> generateSessionsForInstitution(
        Long institutionId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Generate sessions for a specific class within a date range.
     *
     * @param classId the ID of the academic class
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of created sessions
     */
    List<Session> generateSessionsForClass(
        Long classId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Ensure calendar entries exist for a date range.
     * Creates WORKDAY entries for weekdays and WEEKEND entries for weekends.
     * Does not overwrite existing entries.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return number of calendar entries created
     */
    int ensureCalendarEntriesExist(LocalDate startDate, LocalDate endDate);

    /**
     * Check if a session already exists for a given timetable and calendar entry.
     *
     * @param timeTableId the ID of the timetable
     * @param calendarId the ID of the calendar entry
     * @return true if session exists
     */
    boolean sessionExists(Long timeTableId, Long calendarId);
}
