package org.mehlib.marked.dao.repositories;

import java.util.List;
import org.mehlib.marked.dao.entities.CourseAssignment;
import org.mehlib.marked.dao.entities.LectureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseAssignmentRepository
    extends JpaRepository<CourseAssignment, Long>
{
    List<CourseAssignment> findByProfessorId(Long professorId);

    List<CourseAssignment> findByProfessorIdOrderByCreatedOnDesc(
        Long professorId
    );

    List<CourseAssignment> findBySemesterId(Long semesterId);

    List<CourseAssignment> findBySemesterIdOrderByCreatedOnDesc(
        Long semesterId
    );

    List<CourseAssignment> findByCourseId(Long courseId);

    List<CourseAssignment> findByType(LectureType type);

    List<CourseAssignment> findByProfessorIdAndSemesterId(
        Long professorId,
        Long semesterId
    );

    List<CourseAssignment> findByProfessorIdAndCourseId(
        Long professorId,
        Long courseId
    );

    @Query(
        "SELECT ca FROM CourseAssignment ca WHERE ca.professor.institution.id = :institutionId"
    )
    List<CourseAssignment> findByInstitutionId(
        @Param("institutionId") Long institutionId
    );

    @Query(
        "SELECT ca FROM CourseAssignment ca WHERE ca.professor.institution.id = :institutionId ORDER BY ca.createdOn DESC"
    )
    List<CourseAssignment> findByInstitutionIdOrderByCreatedOnDesc(
        @Param("institutionId") Long institutionId
    );

    @Query(
        "SELECT ca FROM CourseAssignment ca WHERE ca.professor.department.id = :departmentId"
    )
    List<CourseAssignment> findByDepartmentId(
        @Param("departmentId") Long departmentId
    );

    @Query(
        "SELECT ca FROM CourseAssignment ca WHERE ca.semester.academicClass.id = :classId"
    )
    List<CourseAssignment> findByClassId(@Param("classId") Long classId);

    @Query(
        "SELECT ca FROM CourseAssignment ca WHERE ca.semester.academicClass.major.id = :majorId"
    )
    List<CourseAssignment> findByMajorId(@Param("majorId") Long majorId);

    boolean existsByProfessorIdAndSemesterIdAndCourseId(
        Long professorId,
        Long semesterId,
        Long courseId
    );

    long countByProfessorId(Long professorId);

    long countBySemesterId(Long semesterId);

    long countByCourseId(Long courseId);

    @Query(
        "SELECT COUNT(ca) FROM CourseAssignment ca WHERE ca.professor.institution.id = :institutionId"
    )
    long countByInstitutionId(@Param("institutionId") Long institutionId);
}
