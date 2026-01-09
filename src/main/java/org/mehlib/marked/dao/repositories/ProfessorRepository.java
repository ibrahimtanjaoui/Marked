package org.mehlib.marked.dao.repositories;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Professor;
import org.mehlib.marked.dao.entities.ProfessorRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByEmail(String email);

    List<Professor> findByInstitutionId(Long institutionId);

    List<Professor> findByInstitutionIdOrderByFamilyNameAsc(Long institutionId);

    List<Professor> findByDepartmentId(Long departmentId);

    List<Professor> findByDepartmentIdOrderByFamilyNameAsc(Long departmentId);

    List<Professor> findByInstitutionIdAndRole(
        Long institutionId,
        ProfessorRole role
    );

    boolean existsByEmailAndInstitutionId(String email, Long institutionId);

    long countByInstitutionId(Long institutionId);

    long countByDepartmentId(Long departmentId);
}
