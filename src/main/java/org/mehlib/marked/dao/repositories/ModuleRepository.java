package org.mehlib.marked.dao.repositories;

import org.mehlib.marked.dao.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
}
