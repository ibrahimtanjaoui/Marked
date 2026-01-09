package org.mehlib.marked;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.mehlib.marked.dao.entities.*;
import org.mehlib.marked.dao.entities.Class;
import org.mehlib.marked.dao.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ENSAM-C Data Initializer with real data for demo
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(
        DataInitializer.class
    );
    private static final String DEFAULT_PASSWORD = "password123";

    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final ClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final TimeTableRepository timeTableRepository;
    private final CalendarRepository calendarRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        InstitutionRepository institutionRepository,
        DepartmentRepository departmentRepository,
        MajorRepository majorRepository,
        ClassRepository classRepository,
        SectionRepository sectionRepository,
        StudentRepository studentRepository,
        ProfessorRepository professorRepository,
        CourseRepository courseRepository,
        SemesterRepository semesterRepository,
        CourseAssignmentRepository courseAssignmentRepository,
        TimeTableRepository timeTableRepository,
        CalendarRepository calendarRepository,
        SessionRepository sessionRepository,
        AttendanceRepository attendanceRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.majorRepository = majorRepository;
        this.classRepository = classRepository;
        this.sectionRepository = sectionRepository;
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.timeTableRepository = timeTableRepository;
        this.calendarRepository = calendarRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (institutionRepository.count() > 0) {
            log.info("Data already exists, skipping initialization");
            return;
        }

        log.info("=== Starting ENSAM-C Data Initialization ===");
        log.info("Default password: {}", DEFAULT_PASSWORD);

        // 1. Create ENSAM-C Institution
        Institution ensam = new Institution();
        ensam.setName("ENSAM-C");
        ensam.setLatitude(33.5731);
        ensam.setLongitude(-7.5898);
        ensam.setRadiusMeters(10000.0);
        ensam = institutionRepository.save(ensam);
        log.info("Created institution: ENSAM-C");

        // 2. Create Department
        Department informatique = new Department();
        informatique.setName("Informatique");
        informatique.setInstitution(ensam);
        informatique = departmentRepository.save(informatique);

        // 3. Create Major
        Major iagi = new Major();
        iagi.setName("IAGI");
        iagi.setDescription(
            "Ingénierie en Intelligence Artificielle et Génie Informatique"
        );
        iagi.setDepartment(informatique);
        iagi = majorRepository.save(iagi);

        // 4. Create Class
        Class iagiClass = new Class();
        iagiClass.setName("IAGI-2");
        iagiClass.setMajor(iagi);
        iagiClass = classRepository.save(iagiClass);

        // 5. Create Section
        Section section = new Section();
        section.setName("IAGI-2");
        section.setAcademicClass(iagiClass);
        section = sectionRepository.save(section);

        // 6. Create Professors
        Professor badrHirchoua = createProfessor(
            ensam,
            informatique,
            "Badr",
            "Hirchoua",
            "hirchoua.badr@ensam-casa.ma",
            ProfessorRole.FACULTY_MEMBER
        );

        Professor moheyddine = createProfessor(
            ensam,
            informatique,
            "Moheyddine",
            "Moheyddine",
            "moheyddine@ensam-casa.ma",
            ProfessorRole.FACULTY_MEMBER
        );

        Professor azmi = createProfessor(
            ensam,
            informatique,
            "Azmi",
            "Azmi",
            "azmi@ensam-casa.ma",
            ProfessorRole.FACULTY_MEMBER
        );

        log.info("Created {} professors", 3);

        // 7. Create Students (Key students + others)
        List<Student> students = new ArrayList<>();

        // Key students for demo
        students.add(
            createStudent(
                ensam,
                section,
                "Mahdi",
                "Bahous",
                "bahous.mahdi@ensam-casa.ma",
                "ENSAM-IAGI2-0010"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Ibrahim",
                "Tanjaoui",
                "tanjaoui.ibrahim@ensam-casa.ma",
                "ENSAM-IAGI2-0008"
            )
        );

        // Other students
        students.add(
            createStudent(
                ensam,
                section,
                "Ikram",
                "Chahine",
                "chahine.ikram@ensam-casa.ma",
                "ENSAM-IAGI2-0001"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Sami",
                "Karboubi",
                "karboubi.sami@ensam-casa.ma",
                "ENSAM-IAGI2-0002"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Alae",
                "Bouzekraoui",
                "bouzekraoui.alae@ensam-casa.ma",
                "ENSAM-IAGI2-0003"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Aya",
                "Achiban",
                "achiban.aya@ensam-casa.ma",
                "ENSAM-IAGI2-0004"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Nour",
                "Tadili",
                "tadili.nour@ensam-casa.ma",
                "ENSAM-IAGI2-0005"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Kawtar",
                "Sahili",
                "sahili.kawtar@ensam-casa.ma",
                "ENSAM-IAGI2-0006"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Oumaima",
                "Dribi Alaoui",
                "dribialaoui.oumaima@ensam-casa.ma",
                "ENSAM-IAGI2-0007"
            )
        );
        students.add(
            createStudent(
                ensam,
                section,
                "Othmane",
                "Chiguer",
                "chiguer.othmane@ensam-casa.ma",
                "ENSAM-IAGI2-0009"
            )
        );

        log.info("Created {} students", students.size());

        // 8. Create Semester (current semester)
        Semester semester = new Semester();
        semester.setName("Automne 2025-2026");
        semester.setAcademicClass(iagiClass);
        semester.setStartDate(LocalDate.now().minusMonths(2).atStartOfDay());
        semester.setEndDate(LocalDate.now().plusMonths(2).atTime(23, 59, 59));
        semester = semesterRepository.save(semester);

        // 9. Create Courses and Assignments
        Course jee = createCourse(
            "Java EE",
            "JEE",
            "Développement d'applications d'entreprise avec Java EE"
        );
        Course designPatterns = createCourse(
            "Design Patterns",
            "DESIGN-PATTERNS",
            "Patrons de conception logicielle"
        );
        Course cloudComputing = createCourse(
            "Cloud Computing",
            "CLOUD-COMPUTING",
            "Technologies cloud et virtualisation"
        );

        CourseAssignment jeeAssignment = createCourseAssignment(
            jee,
            semester,
            badrHirchoua
        );
        CourseAssignment designPatternsAssignment = createCourseAssignment(
            designPatterns,
            semester,
            moheyddine
        );
        CourseAssignment cloudAssignment = createCourseAssignment(
            cloudComputing,
            semester,
            azmi
        );

        log.info("Created {} courses", 3);

        // 10. Create Calendar entries for test days
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate threeDaysAgo = today.minusDays(3);

        Calendar calToday = createCalendar(today);
        Calendar calYesterday = createCalendar(yesterday);
        Calendar calTwoDaysAgo = createCalendar(twoDaysAgo);
        Calendar calThreeDaysAgo = createCalendar(threeDaysAgo);

        log.info("Created calendar entries");

        // 11. Create Manual Test Sessions (8:00 - 12:00)

        // Session 1: JEE - Three days ago
        Session session1 = createSession(
            calThreeDaysAgo,
            jeeAssignment,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            List.of(section),
            null
        );

        // Session 2: Design Patterns - Two days ago
        Session session2 = createSession(
            calTwoDaysAgo,
            designPatternsAssignment,
            LocalTime.of(8, 30),
            LocalTime.of(10, 30),
            List.of(section),
            null
        );

        // Session 3: Cloud Computing - Yesterday
        Session session3 = createSession(
            calYesterday,
            cloudAssignment,
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            List.of(section),
            null
        );

        // Session 4: JEE - Today (for testing live attendance)
        Session session4 = createSession(
            calToday,
            jeeAssignment,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            List.of(section),
            "ABC123"
        );

        log.info("Created {} manual test sessions", 4);

        // 12. Create Attendance Records for Past Sessions

        // Session 1 attendance (3 days ago - JEE)
        createPastAttendance(session1, students, 0.8); // 80% attendance

        // Session 2 attendance (2 days ago - Design Patterns)
        createPastAttendance(session2, students, 0.85); // 85% attendance

        // Session 3 attendance (yesterday - Cloud Computing)
        createPastAttendance(session3, students, 0.75); // 75% attendance

        // Mahdi missed session 3, add justification
        Student mahdi = students.get(0);
        Attendance mahdiAbsence = attendanceRepository
            .findAll()
            .stream()
            .filter(
                a ->
                    a.getSession().getId().equals(session3.getId()) &&
                    a.getStudent().getId().equals(mahdi.getId()) &&
                    a.getStatus() == AttendanceStatus.ABSENT
            )
            .findFirst()
            .orElse(null);

        if (mahdiAbsence != null) {
            mahdiAbsence.setJustificationText(
                "J'étais malade avec une forte fièvre."
            );
            mahdiAbsence.setJustificationStatus(JustificationStatus.PENDING);
            mahdiAbsence.setJustificationSubmittedAt(
                Instant.now().minusSeconds(3600)
            );
            attendanceRepository.save(mahdiAbsence);
            log.info("Added justification for Mahdi");
        }

        log.info("=== ENSAM-C Data Initialization Completed ===");
        log.info("Login credentials:");
        log.info(
            "  Professor (Badr Hirchoua): hirchoua.badr@ensam-casa.ma / password123"
        );
        log.info(
            "  Student (Mahdi Bahous): bahous.mahdi@ensam-casa.ma / password123"
        );
        log.info(
            "  Student (Ibrahim Tanjaoui): tanjaoui.ibrahim@ensam-casa.ma / password123"
        );
    }

    private Professor createProfessor(
        Institution institution,
        Department department,
        String firstName,
        String familyName,
        String email,
        ProfessorRole role
    ) {
        Professor professor = new Professor();
        professor.setFirstName(firstName);
        professor.setFamilyName(familyName);
        professor.setFullName(firstName + " " + familyName);
        professor.setEmail(email);
        professor.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        professor.setInstitution(institution);
        professor.setDepartment(department);
        professor.setRole(role);
        return professorRepository.save(professor);
    }

    private Student createStudent(
        Institution institution,
        Section section,
        String firstName,
        String familyName,
        String email,
        String studentId
    ) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setFamilyName(familyName);
        student.setFullName(firstName + " " + familyName);
        student.setEmail(email);
        student.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        student.setStudentId(studentId);
        student.setInstitution(institution);
        student.setSection(section);
        student.setStatus(StudentStatus.REGISTERED);
        return studentRepository.save(student);
    }

    private Course createCourse(String name, String label, String description) {
        Course course = new Course();
        course.setName(name);
        course.setLabel(label);
        course.setDescription(description);
        return courseRepository.save(course);
    }

    private CourseAssignment createCourseAssignment(
        Course course,
        Semester semester,
        Professor professor
    ) {
        CourseAssignment assignment = new CourseAssignment();
        assignment.setCourse(course);
        assignment.setSemester(semester);
        assignment.setProfessor(professor);
        assignment.setType(LectureType.LECTURE);
        return courseAssignmentRepository.save(assignment);
    }

    private Calendar createCalendar(LocalDate date) {
        Calendar calendar = new Calendar();
        calendar.setDate(date.atStartOfDay());
        calendar.setDayOfWeek(date.getDayOfWeek());

        if (
            date.getDayOfWeek() == DayOfWeek.SATURDAY ||
            date.getDayOfWeek() == DayOfWeek.SUNDAY
        ) {
            calendar.setDayType(DayType.WEEKEND);
        } else {
            calendar.setDayType(DayType.WORKDAY);
        }

        return calendarRepository.save(calendar);
    }

    private Session createSession(
        Calendar calendar,
        CourseAssignment courseAssignment,
        LocalTime startTime,
        LocalTime endTime,
        List<Section> sections,
        String sessionCode
    ) {
        Session session = new Session();
        session.setCalendar(calendar);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setSessionCode(sessionCode);
        session.setType(SessionType.REGULAR);

        // Create and save TimeTable
        TimeTable timeTable = new TimeTable();
        timeTable.setCourseAssignment(courseAssignment);
        timeTable.setDayOfWeek(calendar.getDate().getDayOfWeek());
        timeTable.setStartTime(startTime);
        timeTable.setEndTime(endTime);
        timeTable = timeTableRepository.save(timeTable);

        session.setTimeTable(timeTable);
        session = sessionRepository.save(session);

        // Link sections
        for (Section section : sections) {
            session.getSections().add(section);
        }
        session = sessionRepository.save(session);

        return session;
    }

    private void createPastAttendance(
        Session session,
        List<Student> students,
        double presenceRate
    ) {
        for (Student student : students) {
            Attendance attendance = new Attendance();
            attendance.setSession(session);
            attendance.setStudent(student);

            // Randomly mark present/absent based on presence rate
            if (Math.random() < presenceRate) {
                attendance.setStatus(AttendanceStatus.PRESENT);
            } else {
                attendance.setStatus(AttendanceStatus.ABSENT);
                attendance.setJustificationStatus(
                    JustificationStatus.NOT_SUBMITTED
                );
            }

            attendanceRepository.save(attendance);
        }
    }
}
