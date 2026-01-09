package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    List<Course> getAllCourses();
    Optional<Course> getCourse(Long id);
    Course createCourse(Course course);
    Course updateCourse(Long id, Course course);
    void deleteCourse(Course course);
    void deleteCourse(Long id);
}
