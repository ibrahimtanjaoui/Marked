package org.mehlib.marked.service;

import java.util.List;
import org.mehlib.marked.dao.entities.Class;

public interface ClassService {
    List<Class> getAllClasses();
    Class getClassById(Long id);
    Class createClass(Class academicClass);
    Class updateClass(Class academicClass);
    void deleteClass(Class academicClass);
    void deleteClassById(Long id);
}
