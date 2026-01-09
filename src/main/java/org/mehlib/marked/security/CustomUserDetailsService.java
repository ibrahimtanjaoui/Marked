package org.mehlib.marked.security;

import org.mehlib.marked.dao.entities.Admin;
import org.mehlib.marked.dao.entities.InstitutionAdmin;
import org.mehlib.marked.dao.entities.Professor;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.dao.repositories.AdminRepository;
import org.mehlib.marked.dao.repositories.InstitutionAdminRepository;
import org.mehlib.marked.dao.repositories.ProfessorRepository;
import org.mehlib.marked.dao.repositories.StudentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Custom UserDetailsService implementation that loads users from the database.
 * Searches across all user types (Student, Professor, InstitutionAdmin, Admin)
 * to find a matching user by email.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final InstitutionAdminRepository institutionAdminRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(
            StudentRepository studentRepository,
            ProfessorRepository professorRepository,
            InstitutionAdminRepository institutionAdminRepository,
            AdminRepository adminRepository) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.institutionAdminRepository = institutionAdminRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        String normalizedEmail = email.toLowerCase().trim();

        // Check Admin first (highest privilege)
        Optional<Admin> admin = adminRepository.findByEmail(normalizedEmail);
        if (admin.isPresent()) {
            return new CustomUserDetails(admin.get(), UserRole.ROLE_ADMIN);
        }

        // Check Institution Admin
        Optional<InstitutionAdmin> institutionAdmin = institutionAdminRepository.findByEmail(normalizedEmail);
        if (institutionAdmin.isPresent()) {
            return new CustomUserDetails(institutionAdmin.get(), UserRole.ROLE_INSTITUTION_ADMIN);
        }

        // Check Professor
        Optional<Professor> professor = professorRepository.findByEmail(normalizedEmail);
        if (professor.isPresent()) {
            return new CustomUserDetails(professor.get(), UserRole.ROLE_PROFESSOR);
        }

        // Check Student
        Optional<Student> student = studentRepository.findByEmail(normalizedEmail);
        if (student.isPresent()) {
            return new CustomUserDetails(student.get(), UserRole.ROLE_STUDENT);
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    /**
     * Checks if an email is already registered in any user table.
     *
     * @param email the email to check
     * @return true if the email exists in any user table
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String normalizedEmail = email.toLowerCase().trim();

        return adminRepository.findByEmail(normalizedEmail).isPresent()
                || institutionAdminRepository.findByEmail(normalizedEmail).isPresent()
                || professorRepository.findByEmail(normalizedEmail).isPresent()
                || studentRepository.findByEmail(normalizedEmail).isPresent();
    }

    /**
     * Finds the user ID and role for a given email.
     * Useful for redirecting users to their dashboard after login.
     *
     * @param email the user's email
     * @return UserInfo containing id and role, or null if not found
     */
    @Transactional(readOnly = true)
    public UserInfo findUserInfo(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String normalizedEmail = email.toLowerCase().trim();

        Optional<Admin> admin = adminRepository.findByEmail(normalizedEmail);
        if (admin.isPresent()) {
            return new UserInfo(admin.get().getId(), UserRole.ROLE_ADMIN);
        }

        Optional<InstitutionAdmin> institutionAdmin = institutionAdminRepository.findByEmail(normalizedEmail);
        if (institutionAdmin.isPresent()) {
            return new UserInfo(institutionAdmin.get().getId(), UserRole.ROLE_INSTITUTION_ADMIN);
        }

        Optional<Professor> professor = professorRepository.findByEmail(normalizedEmail);
        if (professor.isPresent()) {
            return new UserInfo(professor.get().getId(), UserRole.ROLE_PROFESSOR);
        }

        Optional<Student> student = studentRepository.findByEmail(normalizedEmail);
        if (student.isPresent()) {
            return new UserInfo(student.get().getId(), UserRole.ROLE_STUDENT);
        }

        return null;
    }

    /**
     * Simple record to hold user ID and role information.
     */
    public record UserInfo(Long id, UserRole role) {}
}
