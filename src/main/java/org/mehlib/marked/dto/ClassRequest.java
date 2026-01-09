package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ClassRequest(
    @NotBlank(message = "Class name is required")
    @Size(min = 2, max = 100, message = "Class name must be between 2 and 100 characters")
    String name,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    LocalDate academicYearStart,

    LocalDate academicYearEnd,

    @NotNull(message = "Major ID is required")
    Long majorId
) {}
