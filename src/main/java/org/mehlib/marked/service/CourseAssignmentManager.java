package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.CourseAssignment;
import org.mehlib.marked.dao.repositories.CourseAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseAssignmentManager implements CourseAssignmentService {

    private final CourseAssignmentRepository courseAssignmentRepository;

    public CourseAssignmentManager(
        CourseAssignmentRepository courseAssignmentRepository
    ) {
        this.courseAssignmentRepository = courseAssignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseAssignment> getAllAssignments() {
        return courseAssignmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseAssignment> getAssignment(Long id) {
        return courseAssignmentRepository.findById(id);
    }

    @Override
    public CourseAssignment createAssignment(
        CourseAssignment courseAssignment
    ) {
        return courseAssignmentRepository.save(courseAssignment);
    }

    @Override
    public CourseAssignment updateAssignment(
        CourseAssignment courseAssignment
    ) {
        if (courseAssignment.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update course assignment without an ID"
            );
        }
        return courseAssignmentRepository.save(courseAssignment);
    }

    @Override
    public void deleteAssignment(CourseAssignment courseAssignment) {
        courseAssignmentRepository.delete(courseAssignment);
    }

    @Override
    public void deleteAssignmentById(Long id) {
        courseAssignmentRepository.deleteById(id);
    }
}
