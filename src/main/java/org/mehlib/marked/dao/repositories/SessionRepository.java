package org.mehlib.marked.dao.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Section;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<Session, Long> {
    @Query(
        """
        SELECT s FROM Session s
        LEFT JOIN FETCH s.timeTable tt
        LEFT JOIN FETCH tt.courseAssignment ca
        LEFT JOIN FETCH ca.course
        LEFT JOIN FETCH ca.professor
        LEFT JOIN FETCH s.calendar
        WHERE s.id = :id
        """
    )
    Optional<Session> findByIdWithDetails(@Param("id") Long id);

    @Query(
        """
        SELECT s FROM Session s
        JOIN s.sections sec
        WHERE sec = :section
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findBySection(@Param("section") Section section);

    @Query(
        """
        SELECT s FROM Session s
        JOIN s.sections sec
        WHERE sec.id = :sectionId
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findBySectionId(@Param("sectionId") Long sectionId);

    @Query(
        """
        SELECT DISTINCT s FROM Session s
        LEFT JOIN FETCH s.timeTable tt
        LEFT JOIN FETCH tt.courseAssignment ca
        LEFT JOIN FETCH ca.course
        LEFT JOIN FETCH s.calendar cal
        JOIN s.sections sec
        WHERE sec.id = :sectionId
        AND cal.date >= :fromDate
        AND s.type <> org.mehlib.marked.dao.entities.SessionType.CANCELLED
        ORDER BY cal.date ASC, s.startTime ASC
        """
    )
    List<Session> findUpcomingSessionsBySectionId(
        @Param("sectionId") Long sectionId,
        @Param("fromDate") java.time.LocalDateTime fromDate
    );

    @Query(
        """
        SELECT s FROM Session s
        JOIN s.sections sec
        WHERE sec = :section
        AND s.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findBySectionAndDateRange(
        @Param("section") Section section,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query(
        """
        SELECT s FROM Session s
        WHERE s.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    List<Session> findByType(SessionType type);

    @Query(
        """
        SELECT s FROM Session s
        WHERE s.timeTable.courseAssignment.professor.id = :professorId
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByProfessorId(@Param("professorId") Long professorId);

    @Query(
        """
        SELECT s FROM Session s
        WHERE s.timeTable.courseAssignment.professor.id = :professorId
        AND s.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByProfessorIdAndDateRange(
        @Param("professorId") Long professorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query(
        """
        SELECT DISTINCT s FROM Session s
        JOIN s.sections sec
        WHERE sec.academicClass.major.department.institution.id = :institutionId
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByInstitutionId(
        @Param("institutionId") Long institutionId
    );

    @Query(
        """
        SELECT DISTINCT s FROM Session s
        JOIN s.sections sec
        WHERE sec.academicClass.major.department.institution.id = :institutionId
        AND s.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByInstitutionIdAndDateRange(
        @Param("institutionId") Long institutionId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query(
        """
        SELECT DISTINCT s FROM Session s
        JOIN s.sections sec
        WHERE sec.academicClass.id = :classId
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByClassId(@Param("classId") Long classId);

    @Query(
        """
        SELECT DISTINCT s FROM Session s
        JOIN s.sections sec
        WHERE sec.academicClass.id = :classId
        AND s.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByClassIdAndDateRange(
        @Param("classId") Long classId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query(
        """
        SELECT DISTINCT s FROM Session s
        JOIN s.sections sec
        WHERE sec.academicClass.major.department.id = :departmentId
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query(
        """
        SELECT s FROM Session s
        WHERE s.timeTable.id = :timeTableId
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByTimeTableId(@Param("timeTableId") Long timeTableId);

    @Query(
        """
        SELECT s FROM Session s
        WHERE s.calendar.id = :calendarId
        ORDER BY s.startTime ASC
        """
    )
    List<Session> findByCalendarId(@Param("calendarId") Long calendarId);

    @Query(
        """
        SELECT COUNT(DISTINCT s) FROM Session s
        JOIN s.sections sec
        WHERE sec.academicClass.major.department.institution.id = :institutionId
        """
    )
    long countByInstitutionId(@Param("institutionId") Long institutionId);

    @Query(
        """
        SELECT COUNT(s) FROM Session s
        WHERE s.type = :type
        """
    )
    long countByType(@Param("type") SessionType type);

    @Query(
        """
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Session s
        WHERE s.timeTable.id = :timeTableId AND s.calendar.id = :calendarId
        """
    )
    boolean existsByTimeTableIdAndCalendarId(
        @Param("timeTableId") Long timeTableId,
        @Param("calendarId") Long calendarId
    );

    @Query(
        """
        SELECT s FROM Session s
        WHERE s.timeTable.id = :timeTableId AND s.calendar.id = :calendarId
        """
    )
    List<Session> findByTimeTableIdAndCalendarId(
        @Param("timeTableId") Long timeTableId,
        @Param("calendarId") Long calendarId
    );

    @Query(
        """
        SELECT s FROM Session s
        WHERE s.timeTable.courseAssignment.id = :courseAssignmentId
        ORDER BY s.calendar.date ASC, s.startTime ASC
        """
    )
    List<Session> findByCourseAssignmentId(
        @Param("courseAssignmentId") Long courseAssignmentId
    );

    @Query(
        """
        SELECT DISTINCT tt FROM TimeTable tt
        WHERE tt.courseAssignment.id = :courseAssignmentId
        ORDER BY tt.dayOfWeek, tt.startTime
        """
    )
    List<
        org.mehlib.marked.dao.entities.TimeTable
    > findTimeTablesByCourseAssignmentId(
        @Param("courseAssignmentId") Long courseAssignmentId
    );
}
