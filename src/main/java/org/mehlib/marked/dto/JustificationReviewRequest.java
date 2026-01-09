package org.mehlib.marked.dto;

import jakarta.validation.constraints.Size;

public record JustificationReviewRequest(
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason
) {}
