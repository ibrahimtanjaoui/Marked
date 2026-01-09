package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.CourseAssignment;

import java.util.List;
import java.util.Optional;

public interface CourseAssignmentService {
    List<CourseAssignment> getAllAssignments();
    Optional<CourseAssignment> getAssignment(Long id);
    CourseAssignment createAssignment(CourseAssignment courseAssignment);
    CourseAssignment updateAssignment(CourseAssignment courseAssignment);
    void deleteAssignment(CourseAssignment courseAssignment);
    void deleteAssignmentById(Long id);
}
