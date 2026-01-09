package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;

import org.mehlib.marked.dao.entities.Module;

public interface ModuleService {
    List<Module> getAllModules();
    Optional<Module> getModule(Long id);
    Module createModule(Module module);
    Module updateModule(Module module);
    void deleteModule(Module module);
    void deleteModuleById(Long id);
}
