package org.mehlib.marked.dao.repositories;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.InstitutionAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionAdminRepository
    extends JpaRepository<InstitutionAdmin, Long>
{
    Optional<InstitutionAdmin> findByEmail(String email);

    List<InstitutionAdmin> findByInstitutionId(Long institutionId);

    List<InstitutionAdmin> findByInstitutionIdOrderByFamilyNameAsc(
        Long institutionId
    );

    boolean existsByEmailAndInstitutionId(String email, Long institutionId);

    long countByInstitutionId(Long institutionId);
}
