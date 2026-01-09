package org.mehlib.marked.dao.repositories;

import java.util.List;
import org.mehlib.marked.dao.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByInstitutionId(Long institutionId);

    List<Department> findByInstitutionIdOrderByNameAsc(Long institutionId);

    boolean existsByNameAndInstitutionId(String name, Long institutionId);
}
