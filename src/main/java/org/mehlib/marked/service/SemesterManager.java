package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Semester;
import org.mehlib.marked.dao.repositories.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SemesterManager implements SemesterService {

    private final SemesterRepository semesterRepository;

    public SemesterManager(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Semester> getAllSemesters() {
        return semesterRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Semester> getSemester(Long id) {
        return semesterRepository.findById(id);
    }

    @Override
    public Semester createSemester(Semester semester) {
        return semesterRepository.save(semester);
    }

    @Override
    public Semester updateSemester(Long id, Semester semester) {
        if (!semesterRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Semester not found with ID: " + id
            );
        }
        semester.setId(id);
        return semesterRepository.save(semester);
    }

    @Override
    public void deleteSemester(Semester semester) {
        semesterRepository.delete(semester);
    }

    @Override
    public void deleteSemesterById(Long id) {
        semesterRepository.deleteById(id);
    }
}
