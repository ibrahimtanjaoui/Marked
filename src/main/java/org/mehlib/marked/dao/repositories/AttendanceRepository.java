package org.mehlib.marked.dao.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.JustificationStatus;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query(
        """
        SELECT a FROM Attendance a
        LEFT JOIN FETCH a.student s
        LEFT JOIN FETCH s.section sec
        LEFT JOIN FETCH sec.academicClass
        LEFT JOIN FETCH a.session sess
        LEFT JOIN FETCH sess.timeTable tt
        LEFT JOIN FETCH tt.courseAssignment ca
        LEFT JOIN FETCH ca.course
        LEFT JOIN FETCH ca.professor
        LEFT JOIN FETCH sess.calendar
        WHERE a.id = :id
        """
    )
    Optional<Attendance> findByIdWithDetails(@Param("id") Long id);

    Optional<Attendance> findByStudentAndSession(
        Student student,
        Session session
    );

    List<Attendance> findByStudent(Student student);

    @Query(
        """
        SELECT a FROM Attendance a
        LEFT JOIN FETCH a.session sess
        LEFT JOIN FETCH sess.timeTable tt
        LEFT JOIN FETCH tt.courseAssignment ca
        LEFT JOIN FETCH ca.course
        LEFT JOIN FETCH sess.calendar
        WHERE a.student = :student
        ORDER BY sess.calendar.date DESC, sess.startTime DESC
        """
    )
    List<Attendance> findByStudentOrderBySessionDateDesc(
        @Param("student") Student student
    );

    List<Attendance> findBySession(Session session);

    @Query(
        """
        SELECT a FROM Attendance a
        WHERE a.student = :student
        AND a.session.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY a.session.calendar.date ASC
        """
    )
    List<Attendance> findByStudentAndDateRange(
        @Param("student") Student student,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query(
        """
        SELECT a FROM Attendance a
        WHERE a.student.section.academicClass.id = :classId
        AND a.session.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY a.session.calendar.date ASC
        """
    )
    List<Attendance> findByClassIdAndDateRange(
        @Param("classId") Long classId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query(
        """
        SELECT a FROM Attendance a
        WHERE a.student = :student
        AND a.student.section.academicClass.id = :classId
        AND a.session.calendar.date BETWEEN :startDate AND :endDate
        ORDER BY a.session.calendar.date ASC
        """
    )
    List<Attendance> findByStudentAndClassIdAndDateRange(
        @Param("student") Student student,
        @Param("classId") Long classId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    List<Attendance> findByJustificationStatus(JustificationStatus status);

    @Query(
        """
        SELECT a FROM Attendance a
        WHERE a.justificationStatus = :status
        AND a.session.timeTable.courseAssignment.professor.id = :professorId
        """
    )
    List<Attendance> findPendingJustificationsByProfessor(
        @Param("status") JustificationStatus status,
        @Param("professorId") Long professorId
    );

    @Query(
        """
        SELECT a FROM Attendance a
        LEFT JOIN FETCH a.student s
        LEFT JOIN FETCH a.session sess
        LEFT JOIN FETCH sess.timeTable tt
        LEFT JOIN FETCH tt.courseAssignment ca
        LEFT JOIN FETCH ca.course
        LEFT JOIN FETCH sess.calendar
        WHERE a.justificationStatus IS NOT NULL
        AND a.justificationStatus <> org.mehlib.marked.dao.entities.JustificationStatus.NOT_SUBMITTED
        AND a.session.timeTable.courseAssignment.professor.id = :professorId
        ORDER BY
            CASE a.justificationStatus
                WHEN org.mehlib.marked.dao.entities.JustificationStatus.PENDING THEN 0
                ELSE 1
            END,
            a.justificationSubmittedAt DESC
        """
    )
    List<Attendance> findAllJustificationsByProfessor(
        @Param("professorId") Long professorId
    );
}
