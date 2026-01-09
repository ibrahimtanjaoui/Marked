package org.mehlib.marked.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.AttendanceStatus;
import org.mehlib.marked.dao.entities.JustificationStatus;

public record AttendanceResponse(
    Long id,
    Long studentId,
    String studentName,
    String studentEmail,
    Long sessionId,
    LocalDateTime sessionDate,
    LocalTime sessionStartTime,
    LocalTime sessionEndTime,
    String courseName,
    AttendanceStatus status,
    String comment,
    String justificationText,
    JustificationStatus justificationStatus,
    Instant justificationSubmittedAt,
    Instant justificationReviewedAt,
    String reviewedByProfessorName,
    Instant createdOn,
    Instant lastUpdatedOn
) {
    public static AttendanceResponse fromEntity(Attendance attendance) {
        String studentName = null;
        String studentEmail = null;
        Long studentId = null;
        if (attendance.getStudent() != null) {
            studentId = attendance.getStudent().getId();
            studentName = attendance.getStudent().getFullName();
            studentEmail = attendance.getStudent().getEmail();
        }

        Long sessionId = null;
        LocalDateTime sessionDate = null;
        LocalTime sessionStartTime = null;
        LocalTime sessionEndTime = null;
        String courseName = null;
        if (attendance.getSession() != null) {
            sessionId = attendance.getSession().getId();
            sessionStartTime = attendance.getSession().getStartTime();
            sessionEndTime = attendance.getSession().getEndTime();

            if (attendance.getSession().getCalendar() != null) {
                sessionDate = attendance.getSession().getCalendar().getDate();
            }

            if (attendance.getSession().getTimeTable() != null
                && attendance.getSession().getTimeTable().getCourseAssignment() != null
                && attendance.getSession().getTimeTable().getCourseAssignment().getCourse() != null) {
                courseName = attendance.getSession().getTimeTable().getCourseAssignment().getCourse().getName();
            }
        }

        String reviewedByProfessorName = null;
        if (attendance.getJustificationReviewedBy() != null) {
            reviewedByProfessorName = attendance.getJustificationReviewedBy().getFullName();
        }

        return new AttendanceResponse(
            attendance.getId(),
            studentId,
            studentName,
            studentEmail,
            sessionId,
            sessionDate,
            sessionStartTime,
            sessionEndTime,
            courseName,
            attendance.getStatus(),
            attendance.getComment(),
            attendance.getJustificationText(),
            attendance.getJustificationStatus(),
            attendance.getJustificationSubmittedAt(),
            attendance.getJustificationReviewedAt(),
            reviewedByProfessorName,
            attendance.getCreatedOn(),
            attendance.getLastUpdatedOn()
        );
    }
}
