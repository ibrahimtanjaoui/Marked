package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Optional<Department> getDepartment(Long id);
    Department createDepartment(Department department);
    Department updateDepartment(Department department);
    void deleteDepartment(Department department);
    void deleteDepartmentById(Department department);
}
