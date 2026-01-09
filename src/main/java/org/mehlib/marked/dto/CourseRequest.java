package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(
    @NotBlank(message = "Course name is required")
    @Size(min = 5, max = 100, message = "Course name must be between 5 and 100 characters")
    String name,

    @Size(max = 20, message = "Label cannot exceed 20 characters")
    String label,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description
) {}
