package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Department;
import org.mehlib.marked.dao.repositories.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepartmentManager implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentManager(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Department> getDepartment(Long id) {
        return departmentRepository.findById(id);
    }

    @Override
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Department department) {
        if (department.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update department without an ID"
            );
        }
        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(Department department) {
        departmentRepository.delete(department);
    }

    @Override
    public void deleteDepartmentById(Department department) {
        departmentRepository.delete(department);
    }
}
