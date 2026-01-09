package org.mehlib.marked;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.mehlib.marked.dao.entities.*;
import org.mehlib.marked.dao.entities.Class;
import org.mehlib.marked.dao.repositories.*;
import org.mehlib.marked.service.SessionGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the database with ENSAM-C data including institution, students, professors, and timetables.
 * This is the production seeder for ENSAM Casablanca.
 */
// @Component
public class EnsamCSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(
        EnsamCSeeder.class
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
    private final SessionGenerationService sessionGenerationService;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final CalendarRepository calendarRepository;
    private final SessionRepository sessionRepository;

    public EnsamCSeeder(
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
        SessionGenerationService sessionGenerationService,
        PasswordEncoder passwordEncoder,
        AttendanceRepository attendanceRepository,
        CalendarRepository calendarRepository,
        SessionRepository sessionRepository
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
        this.sessionGenerationService = sessionGenerationService;
        this.passwordEncoder = passwordEncoder;
        this.attendanceRepository = attendanceRepository;
        this.calendarRepository = calendarRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Skip if data already exists
        if (institutionRepository.count() > 0) {
            log.info("Data already exists, skipping ENSAM-C seeding");
            return;
        }

        log.info("=== Starting ENSAM-C Data Seeding ===");
        log.info("Default password for all users: {}", DEFAULT_PASSWORD);

        // 1. Create Institution
        Institution ensam = createInstitution();

        // 2. Create Department
        Department informatique = createDepartment(ensam);

        // 3. Create Professors
        List<Professor> professors = createProfessors(ensam, informatique);

        // 4. Create Major
        Major iagi = createMajor(informatique);

        // 5. Create Class
        Class iagiClass = createClass(iagi);

        // 6. Create Section
        Section iagiSection = createSection(iagiClass);

        // 7. Create Students
        createStudents(ensam, iagiSection);

        // 8. Create Semester
        Semester semester = createSemester(iagiClass);

        // 9. Create Courses and Assignments
        List<CourseAssignment> courseAssignments = createCoursesAndAssignments(
            informatique,
            semester,
            professors
        );

        // 10. Create Timetables
        createTimetables(courseAssignments);

        // 11. Generate Sessions from Timetables
        generateSessions(semester);

        log.info("=== ENSAM-C Data Seeding Completed Successfully ===");
    }

    private Institution createInstitution() {
        Institution ensam = new Institution();
        ensam.setName("ENSAM-C");
        ensam.setLatitude(31.6470270);
        ensam.setLongitude(-8.1188489);
        ensam.setRadiusMeters(10000.0);
        ensam = institutionRepository.save(ensam);
        log.info("Created institution: {}", ensam.getName());
        return ensam;
    }

    private Department createDepartment(Institution institution) {
        Department dept = new Department();
        dept.setName("Informatique");
        dept.setInstitution(institution);
        dept = departmentRepository.save(dept);
        log.info("Created department: {}", dept.getName());
        return dept;
    }

    private List<Professor> createProfessors(
        Institution institution,
        Department department
    ) {
        List<Professor> professors = new ArrayList<>();

        // Badr HIRCHOUA - Department Chair
        professors.add(
            createProfessor(
                "Badr",
                "HIRCHOUA",
                "badr.hirchoua@ensam-casa.ma",
                ProfessorRole.DEPARTMENT_CHAIR,
                institution,
                department
            )
        );

        // Youssef CHERGUI - Major Supervisor (MongoDB/NoSQL teacher)
        professors.add(
            createProfessor(
                "Youssef",
                "CHERGUI",
                "youssef.chergui@ensam-casa.ma",
                ProfessorRole.PROGRAM_DIRECTOR,
                institution,
                department
            )
        );

        // Other faculty members
        professors.add(
            createProfessor(
                "Mohammed",
                "HAIN",
                "mohammed.hain@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        professors.add(
            createProfessor(
                "Karim",
                "JADLI",
                "karim.jadli@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        professors.add(
            createProfessor(
                "Hassan",
                "MOHEYDDINE",
                "hassan.moheyddine@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        professors.add(
            createProfessor(
                "Rachid",
                "AZMI",
                "rachid.azmi@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        professors.add(
            createProfessor(
                "Fatima",
                "EL KHAYMA",
                "fatima.elkhayma@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        professors.add(
            createProfessor(
                "Nadia",
                "BASSIM",
                "nadia.bassim@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        professors.add(
            createProfessor(
                "Amine",
                "BENZZINE",
                "amine.benzzine@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        professors.add(
            createProfessor(
                "Youssef",
                "BOUHSISSIN",
                "youssef.bouhsissin@ensam-casa.ma",
                ProfessorRole.FACULTY_MEMBER,
                institution,
                department
            )
        );

        log.info("Created {} professors", professors.size());
        return professors;
    }

    private Professor createProfessor(
        String firstName,
        String familyName,
        String email,
        ProfessorRole role,
        Institution institution,
        Department department
    ) {
        Professor prof = new Professor();
        prof.setFirstName(firstName);
        prof.setFamilyName(familyName);
        prof.setFullName(firstName + " " + familyName);
        prof.setEmail(email);
        prof.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        prof.setRole(role);
        prof.setInstitution(institution);
        prof.setDepartment(department);
        return professorRepository.save(prof);
    }

    private Major createMajor(Department department) {
        Major major = new Major();
        major.setName("IAGI");
        major.setDepartment(department);
        major = majorRepository.save(major);
        log.info("Created major: {}", major.getName());
        return major;
    }

    private Class createClass(Major major) {
        Class clazz = new Class();
        clazz.setName("IAGI-2");
        clazz.setMajor(major);
        clazz = classRepository.save(clazz);
        log.info("Created class: {}", clazz.getName());
        return clazz;
    }

    private Section createSection(Class clazz) {
        Section section = new Section();
        section.setName("IAGI-2");
        section.setAcademicClass(clazz);
        section = sectionRepository.save(section);
        log.info("Created section: {}", section.getName());
        return section;
    }

    private void createStudents(Institution institution, Section section) {
        String[][] studentsData = {
            // Group 1
            { "CHAHINE", "IKRAM" },
            { "KARBOUBI", "SAMI" },
            { "BOUZEKRAOUI", "ALAE" },
            // Group 2
            { "ACHIBAN", "AYA" },
            // Group 3
            { "TADILI", "NOUR" },
            { "SAHILI", "KAWTAR" },
            { "DRIBI ALAOUI", "OUMAIMA" },
            // Group 4
            { "TANJAOUI", "IBRAHIM" },
            { "CHIGUER", "OTHMANE" },
            { "BAHOUS", "MAHDI" },
            // Group 5
            { "EL-MOUAFIK", "FATIMA-EZZAHRA" },
            { "IDYOUSS", "HAFSA" },
            // Group 6
            { "MONDIR", "AYOUB" },
            { "MAHFOUD", "HOUMAD" },
            { "HAKIM", "REDOUANE" },
            // Group 7
            { "SANIR", "MOHAMED TAHA" },
            { "AIT LAHCEN", "AMINE" },
            // Group 8
            { "SEKKAT", "YASSINE" },
            { "MOHAMED ALI", "DAKHA" },
            { "IMCHTKA", "OUSSAMA" },
            // Group 9
            { "FARFARI", "FATIMA-ZAHRAE" },
            { "NOUAM", "IMANE" },
            // Group 10
            { "ECHALH", "MANAL" },
            { "MOUHIBI", "ASSIA" },
            // Group 11
            { "AZHAR", "HIBA" },
            { "BELGAS", "MERIEM" },
            // Group 12
            { "ABOULHAJ", "YASSINE" },
            { "MOGHANDEZ", "ABDELLAH" },
            { "GOURGAIZ", "BRAHIM" },
            // Group 13
            { "MEKYASSI", "MALAK" },
            { "EL-HAMDAOUI", "MAROUANE" },
            // Group 14
            { "AKKRAYE", "CHAYMAE" },
            { "MOUFTAH", "NOUHAILA" },
            { "KHATTAMI", "SALWA" },
            // Group 15
            { "AFANDI", "IMANE" },
            { "KANAS", "OUMAIMA" },
            // Group 16
            { "OUKRATI", "IMAD EDDINE" },
            { "LAHNIN", "YAHIA" },
            // Group 17
            { "RGUIBI", "BOUCHRA" },
            { "KHEIRATI", "ASMAE" },
            { "HAFID", "SALMA" },
            // Group 18
            { "ELHAMILE", "HATIM" },
            { "MAJOUG", "ILYASS" },
            // Group 19
            { "MAHHA", "TAHA" },
            { "HANI", "HIBA" },
            { "RAGHIB", "RABYA" },
            // Group 20
            { "QUAISSE", "MAROUANE" },
            { "ECHCHYOUGUI", "MOHAMED" },
            // Group 21
            { "TAYAR", "ALI" },
            { "NADMI", "ILIAS" },
            // Group 22
            { "ELOUARDI", "MOHAMED" },
            { "BOUCHOUA", "YOUSSEF" },
            // Group 23
            { "SAOUDI", "IMANE" },
            { "NOKRY", "NOUHAILA" },
        };

        int studentNumber = 1;
        for (String[] studentData : studentsData) {
            String familyName = studentData[0];
            String firstName = studentData[1];
            String email = generateEmail(familyName, firstName);
            String studentId = String.format("ENSAM-IAGI2-%04d", studentNumber);

            Student student = new Student();
            student.setFamilyName(familyName);
            student.setFirstName(firstName);
            student.setFullName(firstName + " " + familyName);
            student.setEmail(email);
            student.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            student.setStudentId(studentId);
            student.setStatus(StudentStatus.REGISTERED);
            student.setInstitution(institution);
            student.setSection(section);

            studentRepository.save(student);
            studentNumber++;
        }

        log.info("Created {} students", studentsData.length);
    }

    private String generateEmail(String familyName, String firstName) {
        // Remove hyphens, convert to lowercase, simple format
        String cleanFamily = familyName
            .replace("-", "")
            .replace(" ", "")
            .toLowerCase();
        String cleanFirst = firstName
            .replace("-", "")
            .replace(" ", "")
            .toLowerCase();
        return cleanFamily + "." + cleanFirst + "@ensam-casa.ma";
    }

    private Semester createSemester(Class clazz) {
        LocalDate today = LocalDate.now();
        // Start semester 1 month ago to have historical data
        LocalDate startDate = today.minusMonths(1);
        LocalDate endDate = startDate.plusMonths(4); // 4 month semester

        Semester semester = new Semester();
        semester.setName("Automne 2025-2026");
        semester.setAcademicClass(clazz);
        semester.setStartDate(startDate.atStartOfDay());
        semester.setEndDate(endDate.atTime(23, 59, 59));
        semester = semesterRepository.save(semester);
        log.info(
            "Created semester: {} ({} to {})",
            semester.getName(),
            today,
            endDate
        );
        return semester;
    }

    private List<CourseAssignment> createCoursesAndAssignments(
        Department department,
        Semester semester,
        List<Professor> professors
    ) {
        List<CourseAssignment> assignments = new ArrayList<>();

        // Find professors by family name for assignments
        Professor hain = findProfessor(professors, "HAIN");
        Professor jadli = findProfessor(professors, "JADLI");
        Professor moheyddine = findProfessor(professors, "MOHEYDDINE");
        Professor azmi = findProfessor(professors, "AZMI");
        Professor hirchoua = findProfessor(professors, "HIRCHOUA");
        Professor chergui = findProfessor(professors, "CHERGUI");
        Professor elKhayma = findProfessor(professors, "EL KHAYMA");
        Professor bassim = findProfessor(professors, "BASSIM");
        Professor benzzine = findProfessor(professors, "BENZZINE");
        Professor bouhsissin = findProfessor(professors, "BOUHSISSIN");

        // Create courses and assignments
        assignments.add(
            createCourseAndAssignment(
                "Administration des bases de données",
                "ADMIN-BDD",
                "Administration et gestion des bases de données relationnelles",
                semester,
                hain
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "Design Patterns",
                "DESIGN-PATTERNS",
                "Patrons de conception logicielle",
                semester,
                jadli
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "Cloud Computing / virtualisation",
                "CLOUD-COMPUTING",
                "Technologies de cloud computing et virtualisation",
                semester,
                moheyddine
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "Analyse et fouille de données",
                "DATA-MINING",
                "Analyse et extraction de connaissances à partir de données",
                semester,
                azmi
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "Ingénierie de Réalité Augmentée",
                "REALITE-AUGMENTEE",
                "Développement d'applications de réalité augmentée",
                semester,
                hirchoua
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "Java EE",
                "JEE",
                "Java Enterprise Edition - développement d'applications d'entreprise",
                semester,
                hirchoua
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "Bases de données NOSQL",
                "NOSQL",
                "Bases de données non-relationnelles et MongoDB",
                semester,
                chergui
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "ANGLAIS",
                "ANGLAIS",
                "Cours de langue anglaise",
                semester,
                elKhayma
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "FRANÇAIS",
                "FRANCAIS",
                "Cours de langue française",
                semester,
                bassim
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "WEB GL",
                "WEBGL",
                "WebGL - graphisme 3D sur le web",
                semester,
                benzzine
            )
        );

        assignments.add(
            createCourseAndAssignment(
                "Intelligence artificielle",
                "IA",
                "Introduction à l'intelligence artificielle",
                semester,
                bouhsissin
            )
        );

        log.info("Created {} courses and assignments", assignments.size());
        return assignments;
    }

    private Professor findProfessor(
        List<Professor> professors,
        String familyName
    ) {
        return professors
            .stream()
            .filter(p -> p.getFamilyName().equalsIgnoreCase(familyName))
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException("Professor not found: " + familyName)
            );
    }

    private CourseAssignment createCourseAndAssignment(
        String courseName,
        String courseLabel,
        String description,
        Semester semester,
        Professor professor
    ) {
        Course course = new Course();
        course.setName(courseName);
        course.setLabel(courseLabel);
        course.setDescription(description);
        course = courseRepository.save(course);

        CourseAssignment assignment = new CourseAssignment();
        assignment.setCourse(course);
        assignment.setProfessor(professor);
        assignment.setSemester(semester);
        return courseAssignmentRepository.save(assignment);
    }

    private void createTimetables(List<CourseAssignment> courseAssignments) {
        // Monday - Administration des bases de données
        createTimetable(
            findCourseAssignment(courseAssignments, "ADMIN-BDD"),
            DayOfWeek.MONDAY,
            LocalTime.of(8, 30),
            LocalTime.of(10, 30)
        );
        createTimetable(
            findCourseAssignment(courseAssignments, "ADMIN-BDD"),
            DayOfWeek.MONDAY,
            LocalTime.of(10, 45),
            LocalTime.of(16, 0)
        );

        // Tuesday - Design Patterns
        createTimetable(
            findCourseAssignment(courseAssignments, "DESIGN-PATTERNS"),
            DayOfWeek.TUESDAY,
            LocalTime.of(8, 30),
            LocalTime.of(10, 30)
        );
        createTimetable(
            findCourseAssignment(courseAssignments, "DESIGN-PATTERNS"),
            DayOfWeek.TUESDAY,
            LocalTime.of(10, 45),
            LocalTime.of(16, 0)
        );

        // Tuesday - Analyse et fouille de données
        createTimetable(
            findCourseAssignment(courseAssignments, "DATA-MINING"),
            DayOfWeek.TUESDAY,
            LocalTime.of(14, 0),
            LocalTime.of(16, 0)
        );
        createTimetable(
            findCourseAssignment(courseAssignments, "DATA-MINING"),
            DayOfWeek.TUESDAY,
            LocalTime.of(16, 15),
            LocalTime.of(18, 15)
        );

        // Wednesday - Cloud Computing
        createTimetable(
            findCourseAssignment(courseAssignments, "CLOUD-COMPUTING"),
            DayOfWeek.WEDNESDAY,
            LocalTime.of(8, 30),
            LocalTime.of(10, 30)
        );
        createTimetable(
            findCourseAssignment(courseAssignments, "CLOUD-COMPUTING"),
            DayOfWeek.WEDNESDAY,
            LocalTime.of(10, 45),
            LocalTime.of(16, 0)
        );

        // Thursday - Ingénierie de Réalité Augmentée
        createTimetable(
            findCourseAssignment(courseAssignments, "REALITE-AUGMENTEE"),
            DayOfWeek.THURSDAY,
            LocalTime.of(8, 30),
            LocalTime.of(10, 30)
        );

        // Thursday - JEE
        createTimetable(
            findCourseAssignment(courseAssignments, "JEE"),
            DayOfWeek.THURSDAY,
            LocalTime.of(10, 45),
            LocalTime.of(16, 0)
        );

        // Thursday - ANGLAIS
        createTimetable(
            findCourseAssignment(courseAssignments, "ANGLAIS"),
            DayOfWeek.THURSDAY,
            LocalTime.of(14, 0),
            LocalTime.of(16, 0)
        );

        // Thursday - FRANÇAIS
        createTimetable(
            findCourseAssignment(courseAssignments, "FRANCAIS"),
            DayOfWeek.THURSDAY,
            LocalTime.of(16, 15),
            LocalTime.of(18, 15)
        );

        // Friday - Bases de données NOSQL
        createTimetable(
            findCourseAssignment(courseAssignments, "NOSQL"),
            DayOfWeek.FRIDAY,
            LocalTime.of(8, 30),
            LocalTime.of(10, 30)
        );
        createTimetable(
            findCourseAssignment(courseAssignments, "NOSQL"),
            DayOfWeek.FRIDAY,
            LocalTime.of(10, 45),
            LocalTime.of(16, 0)
        );

        // Friday - WEB GL
        createTimetable(
            findCourseAssignment(courseAssignments, "WEBGL"),
            DayOfWeek.FRIDAY,
            LocalTime.of(14, 0),
            LocalTime.of(16, 0)
        );

        // Friday - Intelligence artificielle
        createTimetable(
            findCourseAssignment(courseAssignments, "IA"),
            DayOfWeek.FRIDAY,
            LocalTime.of(16, 15),
            LocalTime.of(18, 15)
        );

        log.info("Created timetable entries");
    }

    private CourseAssignment findCourseAssignment(
        List<CourseAssignment> assignments,
        String courseLabel
    ) {
        return assignments
            .stream()
            .filter(a -> a.getCourse().getLabel().equals(courseLabel))
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(
                    "Course assignment not found: " + courseLabel
                )
            );
    }

    private void createTimetable(
        CourseAssignment courseAssignment,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ) {
        TimeTable timetable = new TimeTable();
        timetable.setCourseAssignment(courseAssignment);
        timetable.setDayOfWeek(dayOfWeek);
        timetable.setStartTime(startTime);
        timetable.setEndTime(endTime);
        timeTableRepository.save(timetable);
    }

    private void generateSessions(Semester semester) {
        log.info(
            "Generating sessions from timetables for semester: {}",
            semester.getName()
        );

        // Generate calendar entries for the past month until today
        generateCalendarEntries(semester);

        // Generate sessions from timetables
        List<Session> sessions =
            sessionGenerationService.generateSessionsForSemester(
                semester.getId()
            );
        log.info("Generated {} sessions", sessions.size());

        // Generate attendance records for all sessions
        generateAttendanceRecords(sessions, semester);

        log.info(
            "Completed generating sessions with attendance and justifications"
        );
    }

    private void generateCalendarEntries(Semester semester) {
        log.info("Generating calendar entries for the past month...");

        LocalDate startDate = semester.getStartDate().toLocalDate();
        LocalDate today = LocalDate.now();

        int workDayCount = 0;
        int weekendCount = 0;

        for (
            LocalDate date = startDate;
            !date.isAfter(today);
            date = date.plusDays(1)
        ) {
            Calendar calendar = new Calendar();
            calendar.setDate(date.atStartOfDay());

            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (
                dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
            ) {
                calendar.setDayType(DayType.WEEKEND);
                weekendCount++;
            } else {
                calendar.setDayType(DayType.WORKDAY);
                workDayCount++;
            }

            calendarRepository.save(calendar);
        }

        log.info(
            "Created {} calendar entries ({} work days, {} weekend days)",
            workDayCount + weekendCount,
            workDayCount,
            weekendCount
        );
    }

    private void generateAttendanceRecords(
        List<Session> sessions,
        Semester semester
    ) {
        log.info(
            "Generating attendance records for {} sessions...",
            sessions.size()
        );

        // Get all students in the semester's class
        List<Student> students = studentRepository.findByClassId(
            semester.getAcademicClass().getId()
        );

        if (students.isEmpty()) {
            log.warn(
                "No students found for class {}",
                semester.getAcademicClass().getName()
            );
            return;
        }

        // Find our key students
        Student mahdiBahous = students
            .stream()
            .filter(
                s ->
                    "Mahdi".equalsIgnoreCase(s.getFirstName()) &&
                    "Bahous".equalsIgnoreCase(s.getFamilyName())
            )
            .findFirst()
            .orElse(null);

        Student ibrahimTanjaoui = students
            .stream()
            .filter(
                s ->
                    "Ibrahim".equalsIgnoreCase(s.getFirstName()) &&
                    "Tanjaoui".equalsIgnoreCase(s.getFamilyName())
            )
            .findFirst()
            .orElse(null);

        int attendanceCount = 0;
        int absentCount = 0;
        int justificationCount = 0;
        int futureSessionCount = 0;
        int nullCalendarCount = 0;

        // Process only past and current sessions (not future ones)
        LocalDateTime now = LocalDateTime.now();

        log.info("Current time: {}", now);
        log.info(
            "Processing {} sessions for attendance generation",
            sessions.size()
        );

        for (Session session : sessions) {
            if (session.getCalendar() == null) {
                nullCalendarCount++;
                continue;
            }

            if (session.getCalendar().getDate().isAfter(now)) {
                futureSessionCount++;
                continue; // Skip future sessions
            }

            // Get the professor teaching this session
            Professor professor =
                session.getTimeTable() != null &&
                session.getTimeTable().getCourseAssignment() != null
                    ? session
                          .getTimeTable()
                          .getCourseAssignment()
                          .getProfessor()
                    : null;

            boolean isHirchouaSession =
                professor != null &&
                "Hirchoua".equalsIgnoreCase(professor.getFamilyName());

            for (Student student : students) {
                Attendance attendance = new Attendance();
                attendance.setStudent(student);
                attendance.setSession(session);

                // Determine if student is present (85% attendance rate overall)
                // But make Mahdi and Ibrahim have more varied patterns
                boolean isPresent;

                if (student.equals(mahdiBahous)) {
                    // Mahdi: 75% attendance, more absences to showcase justifications
                    isPresent = Math.random() < 0.75;
                } else if (student.equals(ibrahimTanjaoui)) {
                    // Ibrahim: 80% attendance
                    isPresent = Math.random() < 0.80;
                } else {
                    // Other students: 90% attendance
                    isPresent = Math.random() < 0.90;
                }

                if (isPresent) {
                    attendance.setStatus(AttendanceStatus.PRESENT);
                    // Mark as present with timestamp during session
                    LocalDateTime sessionTime =
                        session.getCalendar() != null
                            ? session.getCalendar().getDate()
                            : LocalDateTime.now();
                    if (session.getStartTime() != null) {
                        sessionTime = sessionTime
                            .withHour(session.getStartTime().getHour())
                            .withMinute(session.getStartTime().getMinute());
                        // Add random minutes (0-30) to simulate real check-ins
                        sessionTime = sessionTime.plusMinutes(
                            (long) (Math.random() * 30)
                        );
                    }
                    attendanceCount++;
                } else {
                    attendance.setStatus(AttendanceStatus.ABSENT);
                    absentCount++;

                    // Some absent students submit justifications
                    boolean shouldJustify = false;

                    if (
                        student.equals(mahdiBahous) ||
                        student.equals(ibrahimTanjaoui)
                    ) {
                        // Key students: 70% of absences get justified
                        shouldJustify = Math.random() < 0.70;
                    } else {
                        // Other students: 40% of absences get justified
                        shouldJustify = Math.random() < 0.40;
                    }

                    if (shouldJustify) {
                        String[] justificationReasons = {
                            "J'étais malade avec une forte fièvre et je ne pouvais pas assister au cours.",
                            "J'avais un rendez-vous médical urgent qui ne pouvait pas être reporté.",
                            "Problème de transport - le bus était en panne ce jour-là.",
                            "Urgence familiale - j'ai dû m'occuper d'un membre de ma famille.",
                            "J'ai eu un accident mineur sur le chemin de l'école.",
                            "Je participais à une compétition sportive représentant l'école.",
                            "Rendez-vous à l'administration pour renouveler mes documents.",
                            "Panne de réveil et absence de transport alternatif disponible.",
                        };

                        attendance.setJustificationText(
                            justificationReasons[(int) (Math.random() *
                                justificationReasons.length)]
                        );

                        LocalDateTime sessionDate =
                            session.getCalendar() != null
                                ? session.getCalendar().getDate()
                                : LocalDateTime.now();

                        // Justification submitted 1-2 days after absence
                        attendance.setJustificationSubmittedAt(
                            sessionDate
                                .plusDays((long) (1 + Math.random()))
                                .toInstant(java.time.ZoneOffset.UTC)
                        );

                        // 80% of justifications are reviewed
                        if (Math.random() < 0.80) {
                            // 70% approved, 30% rejected
                            boolean approved = Math.random() < 0.70;
                            attendance.setJustificationStatus(
                                approved
                                    ? JustificationStatus.APPROVED
                                    : JustificationStatus.REJECTED
                            );

                            // Reviewed 1-3 days after submission
                            attendance.setJustificationReviewedAt(
                                sessionDate
                                    .plusDays((long) (2 + Math.random() * 2))
                                    .toInstant(java.time.ZoneOffset.UTC)
                            );

                            // Set reviewer to the professor of this session (especially Hirchoua)
                            if (professor != null) {
                                attendance.setJustificationReviewedBy(
                                    professor
                                );
                            }
                        } else {
                            // Pending review
                            attendance.setJustificationStatus(
                                JustificationStatus.PENDING
                            );
                        }

                        justificationCount++;
                    } else {
                        attendance.setJustificationStatus(
                            JustificationStatus.NOT_SUBMITTED
                        );
                    }
                }

                attendanceRepository.save(attendance);
            }
        }

        log.info("Generated attendance records:");
        log.info("  - {} present marks", attendanceCount);
        log.info("  - {} absences", absentCount);
        log.info("  - {} justifications submitted", justificationCount);
        log.info("  - {} sessions with null calendar", nullCalendarCount);
        log.info("  - {} future sessions skipped", futureSessionCount);
        log.info(
            "  - {} past sessions processed",
            sessions.size() - futureSessionCount - nullCalendarCount
        );
    }
}
