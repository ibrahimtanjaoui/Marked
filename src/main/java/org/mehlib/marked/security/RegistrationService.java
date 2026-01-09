package org.mehlib.marked.security;

import org.mehlib.marked.dao.entities.Professor;
import org.mehlib.marked.dao.entities.ProfessorRole;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.dao.entities.StudentStatus;
import org.mehlib.marked.dao.repositories.ProfessorRepository;
import org.mehlib.marked.dao.repositories.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user registration.
 * Supports registration for Students and Professors with BCrypt password hashing.
 */
@Service
public class RegistrationService {

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public RegistrationService(
        StudentRepository studentRepository,
        ProfessorRepository professorRepository,
        PasswordEncoder passwordEncoder,
        CustomUserDetailsService userDetailsService
    ) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Registers a new student account.
     *
     * @param request the registration request containing student details
     * @return the created Student entity
     * @throws RegistrationException if registration fails
     */
    @Transactional
    public Student registerStudent(StudentRegistrationRequest request)
        throws RegistrationException {
        // Validate email is not already taken
        if (userDetailsService.emailExists(request.getEmail())) {
            throw new RegistrationException("Email is already registered");
        }

        // Validate student ID is not already taken
        if (
            studentRepository
                .findByStudentId(request.getStudentId())
                .isPresent()
        ) {
            throw new RegistrationException("Student ID is already registered");
        }

        // Create new student
        Student student = new Student();
        student.setEmail(request.getEmail().toLowerCase().trim());
        student.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        student.setFirstName(request.getFirstName().trim());
        student.setFamilyName(request.getFamilyName().trim());
        student.setFullName(
            request.getFirstName().trim() + " " + request.getFamilyName().trim()
        );
        student.setStudentId(request.getStudentId().trim());
        student.setStatus(StudentStatus.REGISTERED);

        return studentRepository.save(student);
    }

    /**
     * Registers a new professor account.
     *
     * @param request the registration request containing professor details
     * @return the created Professor entity
     * @throws RegistrationException if registration fails
     */
    @Transactional
    public Professor registerProfessor(ProfessorRegistrationRequest request)
        throws RegistrationException {
        // Validate email is not already taken
        if (userDetailsService.emailExists(request.email())) {
            throw new RegistrationException("Email is already registered");
        }

        // Create new professor
        Professor professor = new Professor();
        professor.setEmail(request.email().toLowerCase().trim());
        professor.setPasswordHash(passwordEncoder.encode(request.password()));
        professor.setFirstName(request.firstName().trim());
        professor.setFamilyName(request.familyName().trim());
        professor.setFullName(
            request.firstName().trim() + " " + request.familyName().trim()
        );
        professor.setRole(ProfessorRole.FACULTY_MEMBER);

        return professorRepository.save(professor);
    }

    /**
     * Encodes a password using BCrypt.
     * Useful for updating passwords or for administrative purposes.
     *
     * @param rawPassword the plain text password
     * @return the BCrypt encoded password hash
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Validates if a raw password matches an encoded password.
     *
     * @param rawPassword the plain text password
     * @param encodedPassword the BCrypt encoded password hash
     * @return true if passwords match
     */
    public boolean validatePassword(
        String rawPassword,
        String encodedPassword
    ) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
