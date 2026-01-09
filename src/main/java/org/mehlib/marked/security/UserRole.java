package org.mehlib.marked.security;

/**
 * Enum representing the different user roles in the application.
 * Each role corresponds to a specific type of user entity.
 */
public enum UserRole {
    ROLE_STUDENT("Student"),
    ROLE_PROFESSOR("Professor"),
    ROLE_INSTITUTION_ADMIN("Institution Admin"),
    ROLE_ADMIN("System Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the role name without the ROLE_ prefix.
     * Useful for display purposes.
     */
    public String getSimpleName() {
        return name().replace("ROLE_", "");
    }
}
