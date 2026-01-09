package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.mehlib.marked.dao.entities.LectureType;

public record CourseAssignmentRequest(
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Lecture type is required")
    LectureType type,

    @NotNull(message = "Professor ID is required")
    Long professorId,

    @NotNull(message = "Semester ID is required")
    Long semesterId,

    @NotNull(message = "Course ID is required")
    Long courseId
) {}
