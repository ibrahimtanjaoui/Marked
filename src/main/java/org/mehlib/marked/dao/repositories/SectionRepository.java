package org.mehlib.marked.dao.repositories;

import java.util.List;
import org.mehlib.marked.dao.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByAcademicClassId(Long classId);

    List<Section> findByAcademicClassIdOrderByNameAsc(Long classId);

    boolean existsByNameAndAcademicClassId(String name, Long classId);

    long countByAcademicClassId(Long classId);

    @Query("SELECT s FROM Section s WHERE s.academicClass.major.id = :majorId")
    List<Section> findByMajorId(@Param("majorId") Long majorId);

    @Query(
        "SELECT s FROM Section s WHERE s.academicClass.major.id = :majorId ORDER BY s.name ASC"
    )
    List<Section> findByMajorIdOrderByNameAsc(@Param("majorId") Long majorId);

    @Query(
        "SELECT s FROM Section s WHERE s.academicClass.major.department.id = :departmentId"
    )
    List<Section> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query(
        "SELECT s FROM Section s WHERE s.academicClass.major.department.id = :departmentId ORDER BY s.name ASC"
    )
    List<Section> findByDepartmentIdOrderByNameAsc(
        @Param("departmentId") Long departmentId
    );

    @Query(
        "SELECT s FROM Section s WHERE s.academicClass.major.department.institution.id = :institutionId"
    )
    List<Section> findByInstitutionId(
        @Param("institutionId") Long institutionId
    );

    @Query(
        "SELECT s FROM Section s WHERE s.academicClass.major.department.institution.id = :institutionId ORDER BY s.name ASC"
    )
    List<Section> findByInstitutionIdOrderByNameAsc(
        @Param("institutionId") Long institutionId
    );
}
