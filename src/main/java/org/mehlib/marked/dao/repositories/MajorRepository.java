package org.mehlib.marked.dao.repositories;

import java.util.List;
import org.mehlib.marked.dao.entities.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MajorRepository extends JpaRepository<Major, Long> {
    List<Major> findByDepartmentId(Long departmentId);

    List<Major> findByDepartmentIdOrderByNameAsc(Long departmentId);

    boolean existsByNameAndDepartmentId(String name, Long departmentId);

    long countByDepartmentId(Long departmentId);

    @Query(
        "SELECT m FROM Major m WHERE m.department.institution.id = :institutionId"
    )
    List<Major> findByInstitutionId(@Param("institutionId") Long institutionId);

    @Query(
        "SELECT m FROM Major m WHERE m.department.institution.id = :institutionId ORDER BY m.name ASC"
    )
    List<Major> findByInstitutionIdOrderByNameAsc(
        @Param("institutionId") Long institutionId
    );
}
