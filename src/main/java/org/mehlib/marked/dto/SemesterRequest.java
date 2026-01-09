package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record SemesterRequest(
    @NotBlank(message = "Semester name is required")
    @Size(
        min = 5,
        max = 100,
        message = "Semester name must be between 5 and 100 characters"
    )
    String name,

    @Size(max = 20, message = "Label cannot exceed 20 characters") String label,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Start date is required") LocalDateTime startDate,

    @NotNull(message = "End date is required") LocalDateTime endDate,

    @NotNull(message = "Class ID is required") Long classId
) {}
