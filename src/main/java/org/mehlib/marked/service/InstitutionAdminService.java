package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.*;
import org.mehlib.marked.dao.entities.Class;

public interface InstitutionAdminService extends UserService<InstitutionAdmin> {
    // Institution Admin specific methods
    Optional<InstitutionAdmin> findByEmail(String email);

    List<InstitutionAdmin> getAdminsByInstitution(Long institutionId);

    // Department management
    List<Department> getDepartmentsByInstitution(Long institutionId);

    Department createDepartment(
        Long institutionId,
        String name,
        String description
    );

    Department updateDepartment(
        Long departmentId,
        String name,
        String description
    );

    void deleteDepartment(Long departmentId);

    // Major management
    List<Major> getMajorsByInstitution(Long institutionId);

    List<Major> getMajorsByDepartment(Long departmentId);

    Major createMajor(Long departmentId, String name, String description);

    Major updateMajor(Long majorId, String name, String description);

    void deleteMajor(Long majorId);

    // Class management
    List<Class> getClassesByInstitution(Long institutionId);

    List<Class> getClassesByMajor(Long majorId);

    Class createClass(
        Long majorId,
        String name,
        String description,
        java.time.LocalDate academicYearStart,
        java.time.LocalDate academicYearEnd
    );

    Class updateClass(
        Long classId,
        String name,
        String description,
        java.time.LocalDate academicYearStart,
        java.time.LocalDate academicYearEnd
    );

    void deleteClass(Long classId);

    // Section management
    List<Section> getSectionsByInstitution(Long institutionId);

    List<Section> getSectionsByClass(Long classId);

    Section createSection(Long classId, String name, String description);

    Section updateSection(Long sectionId, String name, String description);

    void deleteSection(Long sectionId);

    // Professor management
    List<Professor> getProfessorsByInstitution(Long institutionId);

    List<Professor> getProfessorsByDepartment(Long departmentId);

    Professor createProfessor(
        Long institutionId,
        Long departmentId,
        String firstName,
        String lastName,
        String email,
        String password,
        ProfessorRole role
    );

    Professor updateProfessor(
        Long professorId,
        String firstName,
        String lastName,
        String email,
        ProfessorRole role,
        Long departmentId
    );

    void deleteProfessor(Long professorId);

    // Student management
    List<Student> getStudentsByInstitution(Long institutionId);

    List<Student> getStudentsBySection(Long sectionId);

    List<Student> getStudentsByClass(Long classId);

    Student createStudent(
        Long institutionId,
        Long sectionId,
        String firstName,
        String lastName,
        String email,
        String studentId,
        StudentStatus status
    );

    Student updateStudent(
        Long studentId,
        String firstName,
        String lastName,
        String email,
        String studentIdNumber,
        StudentStatus status,
        Long sectionId
    );

    void deleteStudent(Long studentId);

    // TimeTable management
    List<TimeTable> getTimeTablesByInstitution(Long institutionId);

    List<TimeTable> getTimeTablesByCourseAssignment(Long courseAssignmentId);

    TimeTable createTimeTable(
        Long courseAssignmentId,
        java.time.DayOfWeek dayOfWeek,
        java.time.LocalTime startTime,
        java.time.LocalTime endTime
    );

    TimeTable updateTimeTable(
        Long timeTableId,
        java.time.DayOfWeek dayOfWeek,
        java.time.LocalTime startTime,
        java.time.LocalTime endTime
    );

    void deleteTimeTable(Long timeTableId);

    // Course Assignment management
    List<CourseAssignment> getCourseAssignmentsByInstitution(
        Long institutionId
    );

    List<CourseAssignment> getCourseAssignmentsBySemester(Long semesterId);

    List<CourseAssignment> getCourseAssignmentsByProfessor(Long professorId);

    CourseAssignment createCourseAssignment(
        Long professorId,
        Long semesterId,
        Long courseId,
        LectureType type,
        String description
    );

    CourseAssignment updateCourseAssignment(
        Long courseAssignmentId,
        Long professorId,
        Long semesterId,
        Long courseId,
        LectureType type,
        String description
    );

    void deleteCourseAssignment(Long courseAssignmentId);

    // Semester management
    List<Semester> getSemestersByClass(Long classId);

    Semester createSemester(
        Long classId,
        String name,
        String label,
        String description,
        java.time.LocalDateTime startDate,
        java.time.LocalDateTime endDate
    );

    Semester updateSemester(
        Long semesterId,
        String name,
        String label,
        String description,
        java.time.LocalDateTime startDate,
        java.time.LocalDateTime endDate
    );

    void deleteSemester(Long semesterId);

    // Course management (courses are global but can be viewed)
    List<Course> getAllCourses();

    Course createCourse(String name, String label, String description);

    Course updateCourse(
        Long courseId,
        String name,
        String label,
        String description
    );

    void deleteCourse(Long courseId);

    // Calendar management
    List<Calendar> getAllCalendarEntries();

    List<Calendar> getCalendarEntriesByDateRange(
        java.time.LocalDateTime from,
        java.time.LocalDateTime to
    );

    Calendar createCalendarEntry(
        java.time.DayOfWeek dayOfWeek,
        java.time.LocalDateTime date,
        String holidayName,
        DayType dayType
    );

    Calendar updateCalendarEntry(
        Long calendarId,
        java.time.DayOfWeek dayOfWeek,
        java.time.LocalDateTime date,
        String holidayName,
        DayType dayType
    );

    void deleteCalendarEntry(Long calendarId);

    // Session management (view and manage sessions)
    List<Session> getSessionsByInstitution(Long institutionId);

    List<Session> getSessionsBySection(Long sectionId);

    List<Session> getSessionsByDateRange(
        Long institutionId,
        java.time.LocalDateTime from,
        java.time.LocalDateTime to
    );

    // Statistics
    long countDepartments(Long institutionId);

    long countProfessors(Long institutionId);

    long countStudents(Long institutionId);

    long countSections(Long institutionId);

    long countClasses(Long institutionId);
}
