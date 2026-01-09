package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MarkAttendanceRequest(
    @NotNull(message = "Session ID is required")
    Long sessionId,

    @NotBlank(message = "Session code is required")
    @Size(min = 6, max = 6, message = "Session code must be 6 digits")
    String sessionCode
) {}
