package org.mehlib.marked.security;

/**
 * Registration request for professors.
 */
public record ProfessorRegistrationRequest(
        String email,
        String password,
        String firstName,
        String familyName
) {}
