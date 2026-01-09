package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Module;
import org.mehlib.marked.dao.repositories.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleManager implements ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleManager(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Module> getModule(Long id) {
        return moduleRepository.findById(id);
    }

    @Override
    public Module createModule(Module module) {
        return moduleRepository.save(module);
    }

    @Override
    public Module updateModule(Module module) {
        if (module.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update module without an ID"
            );
        }
        return moduleRepository.save(module);
    }

    @Override
    public void deleteModule(Module module) {
        moduleRepository.delete(module);
    }

    @Override
    public void deleteModuleById(Long id) {
        moduleRepository.deleteById(id);
    }
}
