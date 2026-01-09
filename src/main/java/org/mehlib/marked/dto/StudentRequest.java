package org.mehlib.marked.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.mehlib.marked.dao.entities.StudentStatus;

public record StudentRequest(
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Student ID is required")
    @Size(min = 5, max = 50, message = "Student ID must be between 5 and 50 characters")
    String studentId,

    StudentStatus status,

    @NotNull(message = "Institution ID is required")
    Long institutionId,

    @NotNull(message = "Section ID is required")
    Long sectionId
) {}
