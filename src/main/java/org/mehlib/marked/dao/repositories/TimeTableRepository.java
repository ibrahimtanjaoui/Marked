package org.mehlib.marked.dao.repositories;

import java.time.DayOfWeek;
import java.util.List;
import org.mehlib.marked.dao.entities.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {
    List<TimeTable> findByCourseAssignmentId(Long courseAssignmentId);

    List<TimeTable> findByCourseAssignmentIdOrderByDayOfWeekAscStartTimeAsc(
        Long courseAssignmentId
    );

    List<TimeTable> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<TimeTable> findByDayOfWeekOrderByStartTimeAsc(DayOfWeek dayOfWeek);

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.professor.id = :professorId"
    )
    List<TimeTable> findByProfessorId(@Param("professorId") Long professorId);

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.professor.id = :professorId ORDER BY t.dayOfWeek ASC, t.startTime ASC"
    )
    List<TimeTable> findByProfessorIdOrdered(
        @Param("professorId") Long professorId
    );

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.semester.id = :semesterId"
    )
    List<TimeTable> findBySemesterId(@Param("semesterId") Long semesterId);

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.semester.id = :semesterId ORDER BY t.dayOfWeek ASC, t.startTime ASC"
    )
    List<TimeTable> findBySemesterIdOrdered(
        @Param("semesterId") Long semesterId
    );

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.semester.academicClass.id = :classId"
    )
    List<TimeTable> findByClassId(@Param("classId") Long classId);

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.semester.academicClass.id = :classId ORDER BY t.dayOfWeek ASC, t.startTime ASC"
    )
    List<TimeTable> findByClassIdOrdered(@Param("classId") Long classId);

    @Query(
        """
        SELECT DISTINCT t FROM TimeTable t
        LEFT JOIN FETCH t.courseAssignment ca
        LEFT JOIN FETCH ca.course
        LEFT JOIN FETCH ca.professor
        WHERE ca.semester.academicClass.id = :classId
        ORDER BY t.dayOfWeek ASC, t.startTime ASC
        """
    )
    List<TimeTable> findByClassIdWithDetails(@Param("classId") Long classId);

    @Query(
        """
        SELECT DISTINCT t FROM TimeTable t
        LEFT JOIN FETCH t.courseAssignment ca
        LEFT JOIN FETCH ca.course
        LEFT JOIN FETCH ca.professor
        WHERE ca.professor.id = :professorId
        ORDER BY t.dayOfWeek ASC, t.startTime ASC
        """
    )
    List<TimeTable> findByProfessorIdWithDetails(
        @Param("professorId") Long professorId
    );

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.professor.institution.id = :institutionId"
    )
    List<TimeTable> findByInstitutionId(
        @Param("institutionId") Long institutionId
    );

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.professor.institution.id = :institutionId ORDER BY t.dayOfWeek ASC, t.startTime ASC"
    )
    List<TimeTable> findByInstitutionIdOrdered(
        @Param("institutionId") Long institutionId
    );

    @Query(
        "SELECT t FROM TimeTable t WHERE t.courseAssignment.course.id = :courseId"
    )
    List<TimeTable> findByCourseId(@Param("courseId") Long courseId);

    long countByCourseAssignmentId(Long courseAssignmentId);

    boolean existsByCourseAssignmentIdAndDayOfWeek(
        Long courseAssignmentId,
        DayOfWeek dayOfWeek
    );
}
