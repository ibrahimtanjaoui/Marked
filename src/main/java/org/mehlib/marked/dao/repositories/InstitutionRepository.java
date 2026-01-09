package org.mehlib.marked.dao.repositories;

import org.mehlib.marked.dao.entities.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
}
