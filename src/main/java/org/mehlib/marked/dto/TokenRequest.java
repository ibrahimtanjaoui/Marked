package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for requesting an attendance verification token.
 * Contains session info, session code, and geolocation data.
 */
public record TokenRequest(
    @NotNull(message = "Session ID is required")
    Long sessionId,

    @NotBlank(message = "Session code is required")
    @Size(min = 6, max = 6, message = "Session code must be exactly 6 characters")
    String sessionCode,

    @NotNull(message = "Latitude is required. Please enable location services.")
    Double latitude,

    @NotNull(message = "Longitude is required. Please enable location services.")
    Double longitude
) {}
