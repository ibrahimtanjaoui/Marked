package org.mehlib.marked.dao.repositories;

import java.util.List;
import org.mehlib.marked.dao.entities.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassRepository extends JpaRepository<Class, Long> {
    List<Class> findByMajorId(Long majorId);

    List<Class> findByMajorIdOrderByNameAsc(Long majorId);

    boolean existsByNameAndMajorId(String name, Long majorId);

    long countByMajorId(Long majorId);

    @Query("SELECT c FROM Class c WHERE c.major.department.id = :departmentId")
    List<Class> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query(
        "SELECT c FROM Class c WHERE c.major.department.id = :departmentId ORDER BY c.name ASC"
    )
    List<Class> findByDepartmentIdOrderByNameAsc(
        @Param("departmentId") Long departmentId
    );

    @Query(
        "SELECT c FROM Class c WHERE c.major.department.institution.id = :institutionId"
    )
    List<Class> findByInstitutionId(@Param("institutionId") Long institutionId);

    @Query(
        "SELECT c FROM Class c WHERE c.major.department.institution.id = :institutionId ORDER BY c.name ASC"
    )
    List<Class> findByInstitutionIdOrderByNameAsc(
        @Param("institutionId") Long institutionId
    );
}
