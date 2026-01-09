package org.mehlib.marked.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.mehlib.marked.dao.entities.Calendar;
import org.mehlib.marked.dao.entities.CourseAssignment;
import org.mehlib.marked.dao.entities.DayType;
import org.mehlib.marked.dao.entities.Section;
import org.mehlib.marked.dao.entities.Semester;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.SessionType;
import org.mehlib.marked.dao.entities.TimeTable;
import org.mehlib.marked.dao.repositories.CalendarRepository;
import org.mehlib.marked.dao.repositories.CourseAssignmentRepository;
import org.mehlib.marked.dao.repositories.SectionRepository;
import org.mehlib.marked.dao.repositories.SemesterRepository;
import org.mehlib.marked.dao.repositories.SessionRepository;
import org.mehlib.marked.dao.repositories.TimeTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SessionGenerationService.
 *
 * Generates Session instances by matching TimeTable entries (recurring schedules)
 * with Calendar entries (actual dates).
 */
@Service
@Transactional
public class SessionGenerationManager implements SessionGenerationService {

    private final SessionRepository sessionRepository;
    private final TimeTableRepository timeTableRepository;
    private final CalendarRepository calendarRepository;
    private final SemesterRepository semesterRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final SectionRepository sectionRepository;

    public SessionGenerationManager(
        SessionRepository sessionRepository,
        TimeTableRepository timeTableRepository,
        CalendarRepository calendarRepository,
        SemesterRepository semesterRepository,
        CourseAssignmentRepository courseAssignmentRepository,
        SectionRepository sectionRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.timeTableRepository = timeTableRepository;
        this.calendarRepository = calendarRepository;
        this.semesterRepository = semesterRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.sectionRepository = sectionRepository;
    }

    @Override
    public List<Session> generateSessionsForSemester(Long semesterId) {
        Semester semester = semesterRepository
            .findById(semesterId)
            .orElseThrow(() ->
                new IllegalArgumentException("Semester not found with ID: " + semesterId)
            );

        if (semester.getStartDate() == null || semester.getEndDate() == null) {
            throw new IllegalStateException(
                "Semester must have start and end dates defined"
            );
        }

        LocalDate startDate = semester.getStartDate().toLocalDate();
        LocalDate endDate = semester.getEndDate().toLocalDate();

        // Ensure calendar entries exist for the semester period
        ensureCalendarEntriesExist(startDate, endDate);

        // Get all timetables for this semester
        List<TimeTable> timeTables = timeTableRepository.findBySemesterId(semesterId);

        List<Session> createdSessions = new ArrayList<>();
        for (TimeTable timeTable : timeTables) {
            List<Session> sessions = generateSessionsForTimeTable(
                timeTable.getId(),
                startDate,
                endDate
            );
            createdSessions.addAll(sessions);
        }

        return createdSessions;
    }

    @Override
    public List<Session> generateSessionsForCourseAssignment(
        Long courseAssignmentId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        CourseAssignment courseAssignment = courseAssignmentRepository
            .findById(courseAssignmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "CourseAssignment not found with ID: " + courseAssignmentId
                )
            );

        // Ensure calendar entries exist for the date range
        ensureCalendarEntriesExist(startDate, endDate);

        // Get all timetables for this course assignment
        List<TimeTable> timeTables = timeTableRepository
            .findByCourseAssignmentId(courseAssignmentId);

        List<Session> createdSessions = new ArrayList<>();
        for (TimeTable timeTable : timeTables) {
            List<Session> sessions = generateSessionsForTimeTable(
                timeTable.getId(),
                startDate,
                endDate
            );
            createdSessions.addAll(sessions);
        }

        return createdSessions;
    }

    @Override
    public List<Session> generateSessionsForTimeTable(
        Long timeTableId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        TimeTable timeTable = timeTableRepository
            .findById(timeTableId)
            .orElseThrow(() ->
                new IllegalArgumentException("TimeTable not found with ID: " + timeTableId)
            );

        if (timeTable.getDayOfWeek() == null) {
            throw new IllegalStateException("TimeTable must have a day of week defined");
        }

        // Ensure calendar entries exist
        ensureCalendarEntriesExist(startDate, endDate);

        // Find all calendar entries that match the day of week and are workdays
        List<Calendar> matchingDays = findMatchingCalendarDays(
            timeTable.getDayOfWeek(),
            startDate,
            endDate
        );

        // Get sections that should attend this session
        // Sessions are for all sections of the class that the semester belongs to
        List<Section> sections = getSectionsForTimeTable(timeTable);

        List<Session> createdSessions = new ArrayList<>();
        for (Calendar calendar : matchingDays) {
            // Skip if session already exists
            if (sessionExists(timeTableId, calendar.getId())) {
                continue;
            }

            Session session = createSession(timeTable, calendar, sections);
            createdSessions.add(session);
        }

        return createdSessions;
    }

    @Override
    public List<Session> generateSessionsForInstitution(
        Long institutionId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        // Ensure calendar entries exist
        ensureCalendarEntriesExist(startDate, endDate);

        // Get all timetables for the institution
        List<TimeTable> timeTables = timeTableRepository.findByInstitutionId(institutionId);

        List<Session> createdSessions = new ArrayList<>();
        for (TimeTable timeTable : timeTables) {
            List<Session> sessions = generateSessionsForTimeTable(
                timeTable.getId(),
                startDate,
                endDate
            );
            createdSessions.addAll(sessions);
        }

        return createdSessions;
    }

    @Override
    public List<Session> generateSessionsForClass(
        Long classId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        // Ensure calendar entries exist
        ensureCalendarEntriesExist(startDate, endDate);

        // Get all timetables for the class
        List<TimeTable> timeTables = timeTableRepository.findByClassId(classId);

        List<Session> createdSessions = new ArrayList<>();
        for (TimeTable timeTable : timeTables) {
            List<Session> sessions = generateSessionsForTimeTable(
                timeTable.getId(),
                startDate,
                endDate
            );
            createdSessions.addAll(sessions);
        }

        return createdSessions;
    }

    @Override
    public int ensureCalendarEntriesExist(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        int created = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            LocalDateTime dateTime = current.atStartOfDay();

            // Check if calendar entry already exists for this date
            if (!calendarRepository.existsByDate(dateTime)) {
                Calendar calendar = new Calendar();
                calendar.setDate(dateTime);
                calendar.setDayOfWeek(current.getDayOfWeek());

                // Determine day type based on day of week
                DayOfWeek dayOfWeek = current.getDayOfWeek();
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    calendar.setDayType(DayType.WEEKEND);
                } else {
                    calendar.setDayType(DayType.WORKDAY);
                }

                calendarRepository.save(calendar);
                created++;
            }

            current = current.plusDays(1);
        }

        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean sessionExists(Long timeTableId, Long calendarId) {
        return sessionRepository.existsByTimeTableIdAndCalendarId(timeTableId, calendarId);
    }

    /**
     * Find calendar entries that match a specific day of week within a date range
     * and are WORKDAY type (not holidays or weekends).
     */
    private List<Calendar> findMatchingCalendarDays(
        DayOfWeek dayOfWeek,
        LocalDate startDate,
        LocalDate endDate
    ) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get all workdays in the date range
        List<Calendar> workdays = calendarRepository.findByDateRangeAndDayType(
            startDateTime,
            endDateTime,
            DayType.WORKDAY
        );

        // Filter to only those matching the day of week
        return workdays.stream()
            .filter(c -> c.getDayOfWeek() == dayOfWeek)
            .toList();
    }

    /**
     * Get the sections that should attend sessions for a given timetable.
     * This returns all sections of the class that the semester (via course assignment) belongs to.
     */
    private List<Section> getSectionsForTimeTable(TimeTable timeTable) {
        CourseAssignment courseAssignment = timeTable.getCourseAssignment();
        if (courseAssignment == null) {
            return List.of();
        }

        Semester semester = courseAssignment.getSemester();
        if (semester == null) {
            return List.of();
        }

        org.mehlib.marked.dao.entities.Class academicClass = semester.getAcademicClass();
        if (academicClass == null) {
            return List.of();
        }

        return sectionRepository.findByAcademicClassId(academicClass.getId());
    }

    /**
     * Create a new session for a given timetable and calendar entry.
     */
    private Session createSession(
        TimeTable timeTable,
        Calendar calendar,
        List<Section> sections
    ) {
        Session session = new Session();
        session.setTimeTable(timeTable);
        session.setCalendar(calendar);
        session.setStartTime(timeTable.getStartTime());
        session.setEndTime(timeTable.getEndTime());
        session.setType(SessionType.REGULAR);
        session.setSections(new ArrayList<>(sections));

        return sessionRepository.save(session);
    }
}
