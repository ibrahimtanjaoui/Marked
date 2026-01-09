package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JustificationRequest(
    @NotBlank(message = "Justification text is required")
    @Size(min = 10, max = 1000, message = "Justification must be between 10 and 1000 characters")
    String justificationText
) {}
