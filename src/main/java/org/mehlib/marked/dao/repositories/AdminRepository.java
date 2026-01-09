package org.mehlib.marked.dao.repositories;

import java.util.Optional;
import org.mehlib.marked.dao.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}
