package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for confirming attendance using a verification token.
 * Contains the token received via email.
 */
public record ConfirmTokenRequest(
    @NotBlank(message = "Verification code is required")
    @Size(min = 6, max = 10, message = "Invalid verification code format")
    String token
) {}
