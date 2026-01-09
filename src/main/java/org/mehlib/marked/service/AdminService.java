package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Admin;
import org.mehlib.marked.dao.entities.Institution;
import org.mehlib.marked.dao.entities.InstitutionAdmin;

public interface AdminService extends UserService<Admin> {
    // Admin specific methods
    Optional<Admin> findByEmail(String email);

    // Institution management
    List<Institution> getAllInstitutions();

    Optional<Institution> getInstitution(Long institutionId);

    Institution createInstitution(
        String name,
        String description,
        java.time.LocalDate foundedAt,
        String address
    );

    Institution updateInstitution(
        Long institutionId,
        String name,
        String description,
        java.time.LocalDate foundedAt,
        String address
    );

    void deleteInstitution(Long institutionId);

    // Institution Admin management
    List<InstitutionAdmin> getAllInstitutionAdmins();

    List<InstitutionAdmin> getInstitutionAdminsByInstitution(
        Long institutionId
    );

    InstitutionAdmin createInstitutionAdmin(
        Long institutionId,
        String firstName,
        String lastName,
        String email,
        String password
    );

    InstitutionAdmin updateInstitutionAdmin(
        Long institutionAdminId,
        String firstName,
        String lastName,
        String email,
        Long institutionId
    );

    void deleteInstitutionAdmin(Long institutionAdminId);

    // Statistics
    long countInstitutions();

    long countInstitutionAdmins();

    long countTotalProfessors();

    long countTotalStudents();
}
