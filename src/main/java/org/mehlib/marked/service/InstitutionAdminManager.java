package org.mehlib.marked.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.*;
import org.mehlib.marked.dao.entities.Class;
import org.mehlib.marked.dao.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InstitutionAdminManager
    extends UserManager<InstitutionAdmin>
    implements InstitutionAdminService
{

    private final InstitutionAdminRepository institutionAdminRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final ClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final TimeTableRepository timeTableRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final CalendarRepository calendarRepository;
    private final SessionRepository sessionRepository;

    public InstitutionAdminManager(
        InstitutionAdminRepository institutionAdminRepository,
        InstitutionRepository institutionRepository,
        DepartmentRepository departmentRepository,
        MajorRepository majorRepository,
        ClassRepository classRepository,
        SectionRepository sectionRepository,
        ProfessorRepository professorRepository,
        StudentRepository studentRepository,
        TimeTableRepository timeTableRepository,
        CourseAssignmentRepository courseAssignmentRepository,
        SemesterRepository semesterRepository,
        CourseRepository courseRepository,
        CalendarRepository calendarRepository,
        SessionRepository sessionRepository
    ) {
        super(institutionAdminRepository);
        this.institutionAdminRepository = institutionAdminRepository;
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.majorRepository = majorRepository;
        this.classRepository = classRepository;
        this.sectionRepository = sectionRepository;
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
        this.timeTableRepository = timeTableRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.calendarRepository = calendarRepository;
        this.sessionRepository = sessionRepository;
    }

    // Institution Admin specific methods
    @Override
    @Transactional(readOnly = true)
    public Optional<InstitutionAdmin> findByEmail(String email) {
        return institutionAdminRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstitutionAdmin> getAdminsByInstitution(Long institutionId) {
        return institutionAdminRepository.findByInstitutionIdOrderByFamilyNameAsc(
            institutionId
        );
    }

    // Department management
    @Override
    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByInstitution(Long institutionId) {
        return departmentRepository.findByInstitutionIdOrderByNameAsc(
            institutionId
        );
    }

    @Override
    public Department createDepartment(
        Long institutionId,
        String name,
        String description
    ) {
        Institution institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution not found with ID: " + institutionId
                )
            );

        if (
            departmentRepository.existsByNameAndInstitutionId(
                name,
                institutionId
            )
        ) {
            throw new IllegalArgumentException(
                "Department with name '" +
                    name +
                    "' already exists in this institution"
            );
        }

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setInstitution(institution);

        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(
        Long departmentId,
        String name,
        String description
    ) {
        Department department = departmentRepository
            .findById(departmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Department not found with ID: " + departmentId
                )
            );

        // Check for duplicate name if name is changed
        if (
            !department.getName().equals(name) &&
            departmentRepository.existsByNameAndInstitutionId(
                name,
                department.getInstitution().getId()
            )
        ) {
            throw new IllegalArgumentException(
                "Department with name '" +
                    name +
                    "' already exists in this institution"
            );
        }

        department.setName(name);
        department.setDescription(description);

        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        Department department = departmentRepository
            .findById(departmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Department not found with ID: " + departmentId
                )
            );
        departmentRepository.delete(department);
    }

    // Major management
    @Override
    @Transactional(readOnly = true)
    public List<Major> getMajorsByInstitution(Long institutionId) {
        return majorRepository.findByInstitutionIdOrderByNameAsc(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Major> getMajorsByDepartment(Long departmentId) {
        return majorRepository.findByDepartmentIdOrderByNameAsc(departmentId);
    }

    @Override
    public Major createMajor(
        Long departmentId,
        String name,
        String description
    ) {
        Department department = departmentRepository
            .findById(departmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Department not found with ID: " + departmentId
                )
            );

        if (majorRepository.existsByNameAndDepartmentId(name, departmentId)) {
            throw new IllegalArgumentException(
                "Major with name '" +
                    name +
                    "' already exists in this department"
            );
        }

        Major major = new Major();
        major.setName(name);
        major.setDescription(description);
        major.setDepartment(department);

        return majorRepository.save(major);
    }

    @Override
    public Major updateMajor(Long majorId, String name, String description) {
        Major major = majorRepository
            .findById(majorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Major not found with ID: " + majorId
                )
            );

        if (
            !major.getName().equals(name) &&
            majorRepository.existsByNameAndDepartmentId(
                name,
                major.getDepartment().getId()
            )
        ) {
            throw new IllegalArgumentException(
                "Major with name '" +
                    name +
                    "' already exists in this department"
            );
        }

        major.setName(name);
        major.setDescription(description);

        return majorRepository.save(major);
    }

    @Override
    public void deleteMajor(Long majorId) {
        Major major = majorRepository
            .findById(majorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Major not found with ID: " + majorId
                )
            );
        majorRepository.delete(major);
    }

    // Class management
    @Override
    @Transactional(readOnly = true)
    public List<Class> getClassesByInstitution(Long institutionId) {
        return classRepository.findByInstitutionIdOrderByNameAsc(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Class> getClassesByMajor(Long majorId) {
        return classRepository.findByMajorIdOrderByNameAsc(majorId);
    }

    @Override
    public Class createClass(
        Long majorId,
        String name,
        String description,
        LocalDate academicYearStart,
        LocalDate academicYearEnd
    ) {
        Major major = majorRepository
            .findById(majorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Major not found with ID: " + majorId
                )
            );

        if (classRepository.existsByNameAndMajorId(name, majorId)) {
            throw new IllegalArgumentException(
                "Class with name '" + name + "' already exists in this major"
            );
        }

        Class academicClass = new Class();
        academicClass.setName(name);
        academicClass.setDescription(description);
        academicClass.setAcademicYearStart(academicYearStart);
        academicClass.setAcademicYearEnd(academicYearEnd);
        academicClass.setMajor(major);

        return classRepository.save(academicClass);
    }

    @Override
    public Class updateClass(
        Long classId,
        String name,
        String description,
        LocalDate academicYearStart,
        LocalDate academicYearEnd
    ) {
        Class academicClass = classRepository
            .findById(classId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Class not found with ID: " + classId
                )
            );

        if (
            !academicClass.getName().equals(name) &&
            classRepository.existsByNameAndMajorId(
                name,
                academicClass.getMajor().getId()
            )
        ) {
            throw new IllegalArgumentException(
                "Class with name '" + name + "' already exists in this major"
            );
        }

        academicClass.setName(name);
        academicClass.setDescription(description);
        academicClass.setAcademicYearStart(academicYearStart);
        academicClass.setAcademicYearEnd(academicYearEnd);

        return classRepository.save(academicClass);
    }

    @Override
    public void deleteClass(Long classId) {
        Class academicClass = classRepository
            .findById(classId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Class not found with ID: " + classId
                )
            );
        classRepository.delete(academicClass);
    }

    // Section management
    @Override
    @Transactional(readOnly = true)
    public List<Section> getSectionsByInstitution(Long institutionId) {
        return sectionRepository.findByInstitutionIdOrderByNameAsc(
            institutionId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Section> getSectionsByClass(Long classId) {
        return sectionRepository.findByAcademicClassIdOrderByNameAsc(classId);
    }

    @Override
    public Section createSection(
        Long classId,
        String name,
        String description
    ) {
        Class academicClass = classRepository
            .findById(classId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Class not found with ID: " + classId
                )
            );

        if (sectionRepository.existsByNameAndAcademicClassId(name, classId)) {
            throw new IllegalArgumentException(
                "Section with name '" + name + "' already exists in this class"
            );
        }

        Section section = new Section();
        section.setName(name);
        section.setDescription(description);
        section.setAcademicClass(academicClass);

        return sectionRepository.save(section);
    }

    @Override
    public Section updateSection(
        Long sectionId,
        String name,
        String description
    ) {
        Section section = sectionRepository
            .findById(sectionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Section not found with ID: " + sectionId
                )
            );

        if (
            !section.getName().equals(name) &&
            sectionRepository.existsByNameAndAcademicClassId(
                name,
                section.getAcademicClass().getId()
            )
        ) {
            throw new IllegalArgumentException(
                "Section with name '" + name + "' already exists in this class"
            );
        }

        section.setName(name);
        section.setDescription(description);

        return sectionRepository.save(section);
    }

    @Override
    public void deleteSection(Long sectionId) {
        Section section = sectionRepository
            .findById(sectionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Section not found with ID: " + sectionId
                )
            );
        sectionRepository.delete(section);
    }

    // Professor management
    @Override
    @Transactional(readOnly = true)
    public List<Professor> getProfessorsByInstitution(Long institutionId) {
        return professorRepository.findByInstitutionIdOrderByFamilyNameAsc(
            institutionId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Professor> getProfessorsByDepartment(Long departmentId) {
        return professorRepository.findByDepartmentIdOrderByFamilyNameAsc(
            departmentId
        );
    }

    @Override
    public Professor createProfessor(
        Long institutionId,
        Long departmentId,
        String firstName,
        String lastName,
        String email,
        String password,
        ProfessorRole role
    ) {
        Institution institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution not found with ID: " + institutionId
                )
            );

        Department department = departmentRepository
            .findById(departmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Department not found with ID: " + departmentId
                )
            );

        if (professorRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                "Professor with email '" + email + "' already exists"
            );
        }

        Professor professor = new Professor();
        professor.setFirstName(firstName);
        professor.setFamilyName(lastName);
        professor.setEmail(email);
        professor.setPasswordHash(password); // TODO: Hash password
        professor.setRole(role);
        professor.setInstitution(institution);
        professor.setDepartment(department);

        return professorRepository.save(professor);
    }

    @Override
    public Professor updateProfessor(
        Long professorId,
        String firstName,
        String lastName,
        String email,
        ProfessorRole role,
        Long departmentId
    ) {
        Professor professor = professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        // Check for duplicate email if email is changed
        if (
            !professor.getEmail().equals(email) &&
            professorRepository.findByEmail(email).isPresent()
        ) {
            throw new IllegalArgumentException(
                "Professor with email '" + email + "' already exists"
            );
        }

        Department department = departmentRepository
            .findById(departmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Department not found with ID: " + departmentId
                )
            );

        professor.setFirstName(firstName);
        professor.setFamilyName(lastName);
        professor.setEmail(email);
        professor.setRole(role);
        professor.setDepartment(department);

        return professorRepository.save(professor);
    }

    @Override
    public void deleteProfessor(Long professorId) {
        Professor professor = professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );
        professorRepository.delete(professor);
    }

    // Student management
    @Override
    @Transactional(readOnly = true)
    public List<Student> getStudentsByInstitution(Long institutionId) {
        return studentRepository.findByInstitutionIdOrderByFamilyNameAsc(
            institutionId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getStudentsBySection(Long sectionId) {
        return studentRepository.findBySectionIdOrderByFamilyNameAsc(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getStudentsByClass(Long classId) {
        return studentRepository.findByClassId(classId);
    }

    @Override
    public Student createStudent(
        Long institutionId,
        Long sectionId,
        String firstName,
        String lastName,
        String email,
        String studentId,
        StudentStatus status
    ) {
        Institution institution = institutionRepository
            .findById(institutionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Institution not found with ID: " + institutionId
                )
            );

        Section section = sectionRepository
            .findById(sectionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Section not found with ID: " + sectionId
                )
            );

        if (studentRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                "Student with email '" + email + "' already exists"
            );
        }

        if (studentRepository.findByStudentId(studentId).isPresent()) {
            throw new IllegalArgumentException(
                "Student with student ID '" + studentId + "' already exists"
            );
        }

        Student student = new Student();
        student.setFirstName(firstName);
        student.setFamilyName(lastName);
        student.setEmail(email);
        student.setStudentId(studentId);
        student.setStatus(status != null ? status : StudentStatus.REGISTERED);
        student.setInstitution(institution);
        student.setSection(section);

        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(
        Long studentId,
        String firstName,
        String lastName,
        String email,
        String studentIdNumber,
        StudentStatus status,
        Long sectionId
    ) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        // Check for duplicate email if email is changed
        if (!student.getEmail().equals(email)) {
            Optional<Student> existingByEmail = studentRepository.findByEmail(
                email
            );
            if (
                existingByEmail.isPresent() &&
                !existingByEmail.get().getId().equals(studentId)
            ) {
                throw new IllegalArgumentException(
                    "Student with email '" + email + "' already exists"
                );
            }
        }

        // Check for duplicate student ID if changed
        if (!student.getStudentId().equals(studentIdNumber)) {
            Optional<Student> existingByStudentId =
                studentRepository.findByStudentId(studentIdNumber);
            if (
                existingByStudentId.isPresent() &&
                !existingByStudentId.get().getId().equals(studentId)
            ) {
                throw new IllegalArgumentException(
                    "Student with student ID '" +
                        studentIdNumber +
                        "' already exists"
                );
            }
        }

        Section section = sectionRepository
            .findById(sectionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Section not found with ID: " + sectionId
                )
            );

        student.setFirstName(firstName);
        student.setFamilyName(lastName);
        student.setEmail(email);
        student.setStudentId(studentIdNumber);
        student.setStatus(status);
        student.setSection(section);

        return studentRepository.save(student);
    }

    @Override
    public void deleteStudent(Long studentId) {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );
        studentRepository.delete(student);
    }

    // TimeTable management
    @Override
    @Transactional(readOnly = true)
    public List<TimeTable> getTimeTablesByInstitution(Long institutionId) {
        return timeTableRepository.findByInstitutionIdOrdered(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeTable> getTimeTablesByCourseAssignment(
        Long courseAssignmentId
    ) {
        return timeTableRepository.findByCourseAssignmentIdOrderByDayOfWeekAscStartTimeAsc(
            courseAssignmentId
        );
    }

    @Override
    public TimeTable createTimeTable(
        Long courseAssignmentId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ) {
        CourseAssignment courseAssignment = courseAssignmentRepository
            .findById(courseAssignmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course assignment not found with ID: " + courseAssignmentId
                )
            );

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException(
                "Start time must be before end time"
            );
        }

        TimeTable timeTable = new TimeTable();
        timeTable.setDayOfWeek(dayOfWeek);
        timeTable.setStartTime(startTime);
        timeTable.setEndTime(endTime);
        timeTable.setCourseAssignment(courseAssignment);

        return timeTableRepository.save(timeTable);
    }

    @Override
    public TimeTable updateTimeTable(
        Long timeTableId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ) {
        TimeTable timeTable = timeTableRepository
            .findById(timeTableId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "TimeTable not found with ID: " + timeTableId
                )
            );

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException(
                "Start time must be before end time"
            );
        }

        timeTable.setDayOfWeek(dayOfWeek);
        timeTable.setStartTime(startTime);
        timeTable.setEndTime(endTime);

        return timeTableRepository.save(timeTable);
    }

    @Override
    public void deleteTimeTable(Long timeTableId) {
        TimeTable timeTable = timeTableRepository
            .findById(timeTableId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "TimeTable not found with ID: " + timeTableId
                )
            );
        timeTableRepository.delete(timeTable);
    }

    // Course Assignment management
    @Override
    @Transactional(readOnly = true)
    public List<CourseAssignment> getCourseAssignmentsByInstitution(
        Long institutionId
    ) {
        return courseAssignmentRepository.findByInstitutionIdOrderByCreatedOnDesc(
            institutionId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseAssignment> getCourseAssignmentsBySemester(
        Long semesterId
    ) {
        return courseAssignmentRepository.findBySemesterIdOrderByCreatedOnDesc(
            semesterId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseAssignment> getCourseAssignmentsByProfessor(
        Long professorId
    ) {
        return courseAssignmentRepository.findByProfessorIdOrderByCreatedOnDesc(
            professorId
        );
    }

    @Override
    public CourseAssignment createCourseAssignment(
        Long professorId,
        Long semesterId,
        Long courseId,
        LectureType type,
        String description
    ) {
        Professor professor = professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        Semester semester = semesterRepository
            .findById(semesterId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Semester not found with ID: " + semesterId
                )
            );

        Course course = courseRepository
            .findById(courseId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course not found with ID: " + courseId
                )
            );

        if (
            courseAssignmentRepository.existsByProfessorIdAndSemesterIdAndCourseId(
                professorId,
                semesterId,
                courseId
            )
        ) {
            throw new IllegalArgumentException(
                "This professor is already assigned to this course in this semester"
            );
        }

        CourseAssignment courseAssignment = new CourseAssignment();
        courseAssignment.setProfessor(professor);
        courseAssignment.setSemester(semester);
        courseAssignment.setCourse(course);
        courseAssignment.setType(type);
        courseAssignment.setDescription(description);

        return courseAssignmentRepository.save(courseAssignment);
    }

    @Override
    public CourseAssignment updateCourseAssignment(
        Long courseAssignmentId,
        Long professorId,
        Long semesterId,
        Long courseId,
        LectureType type,
        String description
    ) {
        CourseAssignment courseAssignment = courseAssignmentRepository
            .findById(courseAssignmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course assignment not found with ID: " + courseAssignmentId
                )
            );

        Professor professor = professorRepository
            .findById(professorId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Professor not found with ID: " + professorId
                )
            );

        Semester semester = semesterRepository
            .findById(semesterId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Semester not found with ID: " + semesterId
                )
            );

        Course course = courseRepository
            .findById(courseId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course not found with ID: " + courseId
                )
            );

        courseAssignment.setProfessor(professor);
        courseAssignment.setSemester(semester);
        courseAssignment.setCourse(course);
        courseAssignment.setType(type);
        courseAssignment.setDescription(description);

        return courseAssignmentRepository.save(courseAssignment);
    }

    @Override
    public void deleteCourseAssignment(Long courseAssignmentId) {
        CourseAssignment courseAssignment = courseAssignmentRepository
            .findById(courseAssignmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course assignment not found with ID: " + courseAssignmentId
                )
            );
        courseAssignmentRepository.delete(courseAssignment);
    }

    // Semester management
    @Override
    @Transactional(readOnly = true)
    public List<Semester> getSemestersByClass(Long classId) {
        return semesterRepository.findByAcademicClassIdOrderByStartDateAsc(
            classId
        );
    }

    @Override
    public Semester createSemester(
        Long classId,
        String name,
        String label,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        Class academicClass = classRepository
            .findById(classId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Class not found with ID: " + classId
                )
            );

        if (semesterRepository.existsByNameAndAcademicClassId(name, classId)) {
            throw new IllegalArgumentException(
                "Semester with name '" + name + "' already exists in this class"
            );
        }

        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException(
                "Start date must be before end date"
            );
        }

        Semester semester = new Semester();
        semester.setName(name);
        semester.setLabel(label);
        semester.setDescription(description);
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);
        semester.setAcademicClass(academicClass);

        return semesterRepository.save(semester);
    }

    @Override
    public Semester updateSemester(
        Long semesterId,
        String name,
        String label,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        Semester semester = semesterRepository
            .findById(semesterId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Semester not found with ID: " + semesterId
                )
            );

        if (
            !semester.getName().equals(name) &&
            semesterRepository.existsByNameAndAcademicClassId(
                name,
                semester.getAcademicClass().getId()
            )
        ) {
            throw new IllegalArgumentException(
                "Semester with name '" + name + "' already exists in this class"
            );
        }

        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException(
                "Start date must be before end date"
            );
        }

        semester.setName(name);
        semester.setLabel(label);
        semester.setDescription(description);
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);

        return semesterRepository.save(semester);
    }

    @Override
    public void deleteSemester(Long semesterId) {
        Semester semester = semesterRepository
            .findById(semesterId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Semester not found with ID: " + semesterId
                )
            );
        semesterRepository.delete(semester);
    }

    // Course management
    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course createCourse(String name, String label, String description) {
        Course course = new Course();
        course.setName(name);
        course.setLabel(label);
        course.setDescription(description);

        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(
        Long courseId,
        String name,
        String label,
        String description
    ) {
        Course course = courseRepository
            .findById(courseId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course not found with ID: " + courseId
                )
            );

        course.setName(name);
        course.setLabel(label);
        course.setDescription(description);

        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = courseRepository
            .findById(courseId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course not found with ID: " + courseId
                )
            );
        courseRepository.delete(course);
    }

    // Calendar management
    @Override
    @Transactional(readOnly = true)
    public List<Calendar> getAllCalendarEntries() {
        return calendarRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Calendar> getCalendarEntriesByDateRange(
        LocalDateTime from,
        LocalDateTime to
    ) {
        return calendarRepository.findByDateRange(from, to);
    }

    @Override
    public Calendar createCalendarEntry(
        DayOfWeek dayOfWeek,
        LocalDateTime date,
        String holidayName,
        DayType dayType
    ) {
        Calendar calendar = new Calendar();
        calendar.setDayOfWeek(dayOfWeek);
        calendar.setDate(date);
        calendar.setHolidayName(holidayName);
        calendar.setDayType(dayType);

        return calendarRepository.save(calendar);
    }

    @Override
    public Calendar updateCalendarEntry(
        Long calendarId,
        DayOfWeek dayOfWeek,
        LocalDateTime date,
        String holidayName,
        DayType dayType
    ) {
        Calendar calendar = calendarRepository
            .findById(calendarId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Calendar entry not found with ID: " + calendarId
                )
            );

        calendar.setDayOfWeek(dayOfWeek);
        calendar.setDate(date);
        calendar.setHolidayName(holidayName);
        calendar.setDayType(dayType);

        return calendarRepository.save(calendar);
    }

    @Override
    public void deleteCalendarEntry(Long calendarId) {
        Calendar calendar = calendarRepository
            .findById(calendarId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Calendar entry not found with ID: " + calendarId
                )
            );
        calendarRepository.delete(calendar);
    }

    // Session management
    @Override
    @Transactional(readOnly = true)
    public List<Session> getSessionsByInstitution(Long institutionId) {
        return sessionRepository.findByInstitutionId(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getSessionsBySection(Long sectionId) {
        return sessionRepository.findBySectionId(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getSessionsByDateRange(
        Long institutionId,
        LocalDateTime from,
        LocalDateTime to
    ) {
        return sessionRepository.findByInstitutionIdAndDateRange(
            institutionId,
            from,
            to
        );
    }

    // Statistics
    @Override
    @Transactional(readOnly = true)
    public long countDepartments(Long institutionId) {
        return departmentRepository.findByInstitutionId(institutionId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProfessors(Long institutionId) {
        return professorRepository.countByInstitutionId(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countStudents(Long institutionId) {
        return studentRepository.countByInstitutionId(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSections(Long institutionId) {
        return sectionRepository.findByInstitutionId(institutionId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countClasses(Long institutionId) {
        return classRepository.findByInstitutionId(institutionId).size();
    }
}
