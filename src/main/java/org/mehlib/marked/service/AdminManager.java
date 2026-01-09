package org.mehlib.marked.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Admin;
import org.mehlib.marked.dao.entities.Institution;
import org.mehlib.marked.dao.entities.InstitutionAdmin;
import org.mehlib.marked.dao.repositories.AdminRepository;
import org.mehlib.marked.dao.repositories.InstitutionAdminRepository;
import org.mehlib.marked.dao.repositories.InstitutionRepository;
import org.mehlib.marked.dao.repositories.ProfessorRepository;
import org.mehlib.marked.dao.repositories.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminManager extends UserManager<Admin> implements AdminService {

    private final AdminRepository adminRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionAdminRepository institutionAdminRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;

    public AdminManager(
        AdminRepository adminRepository,
        InstitutionRepository institutionRepository,
        InstitutionAdminRepository institutionAdminRepository,
        ProfessorRepository professorRepository,
        StudentRepository studentRepository
    ) {
        super(adminRepository);
        this.adminRepository = adminRepository;
        this.institutionRepository = institutionRepository;
        this.institutionAdminRepository = institutionAdminRepository;
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
    }

    // Admin specific methods
    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    // Institution management
    @Override
    @Transactional(readOnly = true)
    public List<Institution> getAllInstitutions() {
        return institutionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Institution> getInstitution(Long institutionId) {
        return institutionRepository.findById(institutionId);
    }

    @Override
    public Institution createInstitution(
        String name,
        String description,
        LocalDate foundedAt,
        String address
    ) {
        Institution institution = new Institution();
        institution.setName(name);
        institution.setDescription(description);
        institution.setFoundedAt(foundedAt);
        institution.setAddress(address);

        return institutionRepository.save(institution);
    }

    @Override
    public Institution updateInstitution(
        Long institutionId,
        String name,
        String description,
        LocalDate foundedAt,
        String address
    ) {
        Institution institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution not found with ID: " + institutionId
                )
            );

        institution.setName(name);
        institution.setDescription(description);
        institution.setFoundedAt(foundedAt);
        institution.setAddress(address);

        return institutionRepository.save(institution);
    }

    @Override
    public void deleteInstitution(Long institutionId) {
        Institution institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution not found with ID: " + institutionId
                )
            );
        institutionRepository.delete(institution);
    }

    // Institution Admin management
    @Override
    @Transactional(readOnly = true)
    public List<InstitutionAdmin> getAllInstitutionAdmins() {
        return institutionAdminRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstitutionAdmin> getInstitutionAdminsByInstitution(
        Long institutionId
    ) {
        return institutionAdminRepository.findByInstitutionIdOrderByFamilyNameAsc(
            institutionId
        );
    }

    @Override
    public InstitutionAdmin createInstitutionAdmin(
        Long institutionId,
        String firstName,
        String lastName,
        String email,
        String password
    ) {
        Institution institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution not found with ID: " + institutionId
                )
            );

        if (institutionAdminRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                "Institution Admin with email '" + email + "' already exists"
            );
        }

        InstitutionAdmin admin = new InstitutionAdmin();
        admin.setFirstName(firstName);
        admin.setFamilyName(lastName);
        admin.setEmail(email);
        admin.setPasswordHash(password); // TODO: Hash password
        admin.setInstitution(institution);

        return institutionAdminRepository.save(admin);
    }

    @Override
    public InstitutionAdmin updateInstitutionAdmin(
        Long institutionAdminId,
        String firstName,
        String lastName,
        String email,
        Long institutionId
    ) {
        InstitutionAdmin admin = institutionAdminRepository
            .findById(institutionAdminId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution Admin not found with ID: " + institutionAdminId
                )
            );

        // Check for duplicate email if email is changed
        if (!admin.getEmail().equals(email)) {
            Optional<InstitutionAdmin> existingByEmail =
                institutionAdminRepository.findByEmail(email);
            if (
                existingByEmail.isPresent() &&
                !existingByEmail.get().getId().equals(institutionAdminId)
            ) {
                throw new IllegalArgumentException(
                    "Institution Admin with email '" +
                        email +
                        "' already exists"
                );
            }
        }

        Institution institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution not found with ID: " + institutionId
                )
            );

        admin.setFirstName(firstName);
        admin.setFamilyName(lastName);
        admin.setEmail(email);
        admin.setInstitution(institution);

        return institutionAdminRepository.save(admin);
    }

    @Override
    public void deleteInstitutionAdmin(Long institutionAdminId) {
        InstitutionAdmin admin = institutionAdminRepository
            .findById(institutionAdminId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution Admin not found with ID: " + institutionAdminId
                )
            );
        institutionAdminRepository.delete(admin);
    }

    // Statistics
    @Override
    @Transactional(readOnly = true)
    public long countInstitutions() {
        return institutionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countInstitutionAdmins() {
        return institutionAdminRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalProfessors() {
        return professorRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalStudents() {
        return studentRepository.count();
    }
}
