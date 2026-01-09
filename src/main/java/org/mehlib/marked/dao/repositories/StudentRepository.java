package org.mehlib.marked.dao.repositories;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.dao.entities.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    Optional<Student> findByStudentId(String studentId);

    List<Student> findByInstitutionId(Long institutionId);

    List<Student> findByInstitutionIdOrderByFamilyNameAsc(Long institutionId);

    List<Student> findBySectionId(Long sectionId);

    List<Student> findBySectionIdOrderByFamilyNameAsc(Long sectionId);

    List<Student> findByInstitutionIdAndStatus(
        Long institutionId,
        StudentStatus status
    );

    List<Student> findBySectionIdAndStatus(
        Long sectionId,
        StudentStatus status
    );

    boolean existsByEmailAndInstitutionId(String email, Long institutionId);

    boolean existsByStudentIdAndInstitutionId(
        String studentId,
        Long institutionId
    );

    long countByInstitutionId(Long institutionId);

    long countBySectionId(Long sectionId);

    long countByInstitutionIdAndStatus(
        Long institutionId,
        StudentStatus status
    );

    @Query(
        "SELECT s FROM Student s WHERE s.section.academicClass.id = :classId"
    )
    List<Student> findByClassId(@Param("classId") Long classId);

    @Query(
        "SELECT s FROM Student s WHERE s.section.academicClass.major.id = :majorId"
    )
    List<Student> findByMajorId(@Param("majorId") Long majorId);

    @Query(
        "SELECT s FROM Student s WHERE s.section.academicClass.major.department.id = :departmentId"
    )
    List<Student> findByDepartmentId(@Param("departmentId") Long departmentId);
}
