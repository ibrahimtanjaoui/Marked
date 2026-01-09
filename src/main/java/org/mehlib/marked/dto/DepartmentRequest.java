package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
    @NotBlank(message = "Department name is required")
    @Size(min = 5, max = 100, message = "Department name must be between 5 and 100 characters")
    String name,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @NotNull(message = "Institution ID is required")
    Long institutionId
) {}
