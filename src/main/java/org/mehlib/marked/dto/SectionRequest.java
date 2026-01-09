package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SectionRequest(
    @NotBlank(message = "Section name is required")
    @Size(min = 5, max = 100, message = "Section name must be between 5 and 100 characters")
    String name,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Class ID is required")
    Long classId
) {}
