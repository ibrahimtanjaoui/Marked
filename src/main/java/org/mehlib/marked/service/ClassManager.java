package org.mehlib.marked.service;

import java.util.List;
import org.mehlib.marked.dao.entities.Class;
import org.mehlib.marked.dao.repositories.ClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClassManager implements ClassService {

    private final ClassRepository classRepository;

    public ClassManager(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Class> getAllClasses() {
        return classRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Class getClassById(Long id) {
        return classRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Class not found with ID: " + id)
            );
    }

    @Override
    public Class createClass(Class academicClass) {
        return classRepository.save(academicClass);
    }

    @Override
    public Class updateClass(Class academicClass) {
        if (academicClass.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update class without an ID"
            );
        }
        return classRepository.save(academicClass);
    }

    @Override
    public void deleteClass(Class academicClass) {
        classRepository.delete(academicClass);
    }

    @Override
    public void deleteClassById(Long id) {
        classRepository.deleteById(id);
    }
}
