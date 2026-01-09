package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record InstitutionRequest(
    @NotBlank(message = "Institution name is required")
    @Size(min = 5, max = 100, message = "Institution name must be between 5 and 100 characters")
    String name,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    LocalDate foundedAt,

    @Size(max = 250, message = "Address cannot exceed 250 characters")
    String address
) {}
