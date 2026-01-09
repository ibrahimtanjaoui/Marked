package org.mehlib.marked.dao.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.mehlib.marked.dao.entities.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findByAcademicClassId(Long classId);

    List<Semester> findByAcademicClassIdOrderByStartDateAsc(Long classId);

    boolean existsByNameAndAcademicClassId(String name, Long classId);

    long countByAcademicClassId(Long classId);

    @Query("SELECT s FROM Semester s WHERE s.academicClass.major.id = :majorId")
    List<Semester> findByMajorId(@Param("majorId") Long majorId);

    @Query(
        "SELECT s FROM Semester s WHERE s.academicClass.major.id = :majorId ORDER BY s.startDate ASC"
    )
    List<Semester> findByMajorIdOrderByStartDateAsc(
        @Param("majorId") Long majorId
    );

    @Query(
        "SELECT s FROM Semester s WHERE s.academicClass.major.department.id = :departmentId"
    )
    List<Semester> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query(
        "SELECT s FROM Semester s WHERE s.academicClass.major.department.id = :departmentId ORDER BY s.startDate ASC"
    )
    List<Semester> findByDepartmentIdOrderByStartDateAsc(
        @Param("departmentId") Long departmentId
    );

    @Query(
        "SELECT s FROM Semester s WHERE s.academicClass.major.department.institution.id = :institutionId"
    )
    List<Semester> findByInstitutionId(
        @Param("institutionId") Long institutionId
    );

    @Query(
        "SELECT s FROM Semester s WHERE s.academicClass.major.department.institution.id = :institutionId ORDER BY s.startDate ASC"
    )
    List<Semester> findByInstitutionIdOrderByStartDateAsc(
        @Param("institutionId") Long institutionId
    );

    @Query(
        "SELECT s FROM Semester s WHERE s.startDate <= :date AND s.endDate >= :date"
    )
    List<Semester> findActiveSemesters(@Param("date") LocalDateTime date);

    @Query(
        "SELECT s FROM Semester s WHERE s.academicClass.id = :classId AND s.startDate <= :date AND s.endDate >= :date"
    )
    List<Semester> findActiveSemestersByClass(
        @Param("classId") Long classId,
        @Param("date") LocalDateTime date
    );
}
