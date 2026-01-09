package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.Semester;

import java.util.List;
import java.util.Optional;

public interface SemesterService {
    List<Semester> getAllSemesters();
    Optional<Semester> getSemester(Long id);
    Semester createSemester(Semester semester);
    Semester updateSemester(Long id, Semester semester);
    void deleteSemester(Semester semester);
    void deleteSemesterById(Long id);
}
