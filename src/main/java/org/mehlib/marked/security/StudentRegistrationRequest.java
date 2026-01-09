package org.mehlib.marked.security;

/**
 * Data transfer object for student registration requests.
 */
public class StudentRegistrationRequest {

    private final String email;
    private final String password;
    private final String firstName;
    private final String familyName;
    private final String studentId;

    public StudentRegistrationRequest(
            String email,
            String password,
            String firstName,
            String familyName,
            String studentId) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.familyName = familyName;
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getStudentId() {
        return studentId;
    }
}
