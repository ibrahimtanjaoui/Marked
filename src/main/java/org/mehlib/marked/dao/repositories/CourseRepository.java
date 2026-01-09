package org.mehlib.marked.dao.repositories;

import org.mehlib.marked.dao.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
