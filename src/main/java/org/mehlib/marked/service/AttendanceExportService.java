package org.mehlib.marked.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mehlib.marked.dao.entities.*;
import org.mehlib.marked.dao.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for exporting attendance data to Excel format.
 * Provides various export options for professors to download attendance reports.
 */
@Service
@Transactional(readOnly = true)
public class AttendanceExportService {

    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AttendanceExportService(
        SessionRepository sessionRepository,
        AttendanceRepository attendanceRepository,
        StudentRepository studentRepository,
        CourseAssignmentRepository courseAssignmentRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
    }

    /**
     * Export attendance for a single session to Excel.
     */
    public byte[] exportSessionAttendance(Long sessionId) throws IOException {
        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException("Session not found: " + sessionId)
            );

        List<Attendance> attendances = attendanceRepository.findBySession(
            session
        );

        try (
            Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Attendance");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Student ID",
                "Full Name",
                "Email",
                "Status",
                "Marked At",
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add session info at the top
            Sheet infoSheet = workbook.createSheet("Session Info");
            addSessionInfo(infoSheet, session, headerStyle, dataStyle);

            // Add attendance data
            int rowNum = 1;
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowNum++);
                Student student = attendance.getStudent();

                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getFullName());
                row.createCell(2).setCellValue(student.getEmail());
                row
                    .createCell(3)
                    .setCellValue(attendance.getStatus().toString());
                row
                    .createCell(4)
                    .setCellValue(
                        attendance.getCreatedOn() != null
                            ? DATE_FORMATTER.format(
                                  LocalDateTime.ofInstant(
                                      attendance.getCreatedOn(),
                                      java.time.ZoneId.systemDefault()
                                  )
                              )
                            : "N/A"
                    );

                // Apply data style
                for (int i = 0; i < 5; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Export all attendance for a course (all sessions) to Excel.
     */
    public byte[] exportCourseAttendance(Long courseAssignmentId)
        throws IOException {
        CourseAssignment courseAssignment = courseAssignmentRepository
            .findById(courseAssignmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course assignment not found: " + courseAssignmentId
                )
            );

        List<TimeTable> timeTables =
            sessionRepository.findTimeTablesByCourseAssignmentId(
                courseAssignmentId
            );
        List<Session> sessions = sessionRepository.findByCourseAssignmentId(
            courseAssignmentId
        );

        try (
            Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            addCourseSummary(
                summarySheet,
                courseAssignment,
                sessions,
                headerStyle,
                dataStyle
            );

            // Create a sheet for each session
            int sessionNum = 1;
            for (Session session : sessions) {
                String sheetName = "Session " + sessionNum++;
                Sheet sheet = workbook.createSheet(sheetName);

                List<Attendance> attendances =
                    attendanceRepository.findBySession(session);

                // Header
                Row headerRow = sheet.createRow(0);
                String[] headers = {
                    "Student ID",
                    "Full Name",
                    "Status",
                    "Marked At",
                };
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Data
                int rowNum = 1;
                for (Attendance attendance : attendances) {
                    Row row = sheet.createRow(rowNum++);
                    Student student = attendance.getStudent();

                    row.createCell(0).setCellValue(student.getStudentId());
                    row.createCell(1).setCellValue(student.getFullName());
                    row
                        .createCell(2)
                        .setCellValue(attendance.getStatus().toString());
                    row
                        .createCell(3)
                        .setCellValue(
                            attendance.getCreatedOn() != null
                                ? DATE_FORMATTER.format(
                                      LocalDateTime.ofInstant(
                                          attendance.getCreatedOn(),
                                          java.time.ZoneId.systemDefault()
                                      )
                                  )
                                : "N/A"
                        );

                    for (int i = 0; i < 4; i++) {
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Export attendance for a specific student across all sessions of a course.
     */
    public byte[] exportStudentAttendance(
        Long studentId,
        Long courseAssignmentId
    ) throws IOException {
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException("Student not found: " + studentId)
            );

        CourseAssignment courseAssignment = courseAssignmentRepository
            .findById(courseAssignmentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Course assignment not found: " + courseAssignmentId
                )
            );

        List<Session> sessions = sessionRepository.findByCourseAssignmentId(
            courseAssignmentId
        );

        try (
            Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Student Attendance");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Add student info
            Row infoRow1 = sheet.createRow(0);
            infoRow1.createCell(0).setCellValue("Student ID:");
            infoRow1.createCell(1).setCellValue(student.getStudentId());
            infoRow1.getCell(0).setCellStyle(headerStyle);
            infoRow1.getCell(1).setCellStyle(dataStyle);

            Row infoRow2 = sheet.createRow(1);
            infoRow2.createCell(0).setCellValue("Student Name:");
            infoRow2.createCell(1).setCellValue(student.getFullName());
            infoRow2.getCell(0).setCellStyle(headerStyle);
            infoRow2.getCell(1).setCellStyle(dataStyle);

            Row infoRow3 = sheet.createRow(2);
            infoRow3.createCell(0).setCellValue("Course:");
            infoRow3
                .createCell(1)
                .setCellValue(courseAssignment.getCourse().getName());
            infoRow3.getCell(0).setCellStyle(headerStyle);
            infoRow3.getCell(1).setCellStyle(dataStyle);

            // Empty row
            sheet.createRow(3);

            // Header row
            Row headerRow = sheet.createRow(4);
            String[] headers = { "Session Date", "Status", "Marked At" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 5;
            for (Session session : sessions) {
                Student studentEntity = student;
                Attendance attendance = attendanceRepository
                    .findByStudentAndSession(studentEntity, session)
                    .orElse(null);

                Row row = sheet.createRow(rowNum++);
                row
                    .createCell(0)
                    .setCellValue(
                        session.getCalendar() != null
                            ? DATE_FORMATTER.format(
                                  session.getCalendar().getDate()
                              )
                            : "N/A"
                    );

                if (attendance != null) {
                    row
                        .createCell(1)
                        .setCellValue(attendance.getStatus().toString());
                    row
                        .createCell(2)
                        .setCellValue(
                            attendance.getCreatedOn() != null
                                ? DATE_FORMATTER.format(
                                      LocalDateTime.ofInstant(
                                          attendance.getCreatedOn(),
                                          java.time.ZoneId.systemDefault()
                                      )
                                  )
                                : "N/A"
                        );
                } else {
                    row.createCell(1).setCellValue("NOT_MARKED");
                    row.createCell(2).setCellValue("N/A");
                }

                for (int i = 0; i < 3; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void addSessionInfo(
        Sheet sheet,
        Session session,
        CellStyle headerStyle,
        CellStyle dataStyle
    ) {
        int rowNum = 0;

        // Course info
        Row row1 = sheet.createRow(rowNum++);
        row1.createCell(0).setCellValue("Course:");
        row1
            .createCell(1)
            .setCellValue(
                session
                    .getTimeTable()
                    .getCourseAssignment()
                    .getCourse()
                    .getName()
            );
        row1.getCell(0).setCellStyle(headerStyle);
        row1.getCell(1).setCellStyle(dataStyle);

        // Date
        Row row2 = sheet.createRow(rowNum++);
        row2.createCell(0).setCellValue("Date:");
        row2
            .createCell(1)
            .setCellValue(
                session.getCalendar() != null
                    ? DATE_FORMATTER.format(session.getCalendar().getDate())
                    : "N/A"
            );
        row2.getCell(0).setCellStyle(headerStyle);
        row2.getCell(1).setCellStyle(dataStyle);

        // Time
        Row row3 = sheet.createRow(rowNum++);
        row3.createCell(0).setCellValue("Time:");
        row3
            .createCell(1)
            .setCellValue(
                session.getStartTime() + " - " + session.getEndTime()
            );
        row3.getCell(0).setCellStyle(headerStyle);
        row3.getCell(1).setCellStyle(dataStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void addCourseSummary(
        Sheet sheet,
        CourseAssignment courseAssignment,
        List<Session> sessions,
        CellStyle headerStyle,
        CellStyle dataStyle
    ) {
        int rowNum = 0;

        Row row1 = sheet.createRow(rowNum++);
        row1.createCell(0).setCellValue("Course:");
        row1.createCell(1).setCellValue(courseAssignment.getCourse().getName());
        row1.getCell(0).setCellStyle(headerStyle);
        row1.getCell(1).setCellStyle(dataStyle);

        Row row2 = sheet.createRow(rowNum++);
        row2.createCell(0).setCellValue("Professor:");
        row2
            .createCell(1)
            .setCellValue(courseAssignment.getProfessor().getFullName());
        row2.getCell(0).setCellStyle(headerStyle);
        row2.getCell(1).setCellStyle(dataStyle);

        Row row3 = sheet.createRow(rowNum++);
        row3.createCell(0).setCellValue("Total Sessions:");
        row3.createCell(1).setCellValue(sessions.size());
        row3.getCell(0).setCellStyle(headerStyle);
        row3.getCell(1).setCellStyle(dataStyle);

        Row row4 = sheet.createRow(rowNum++);
        row4.createCell(0).setCellValue("Export Date:");
        row4
            .createCell(1)
            .setCellValue(DATE_FORMATTER.format(LocalDateTime.now()));
        row4.getCell(0).setCellStyle(headerStyle);
        row4.getCell(1).setCellStyle(dataStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
