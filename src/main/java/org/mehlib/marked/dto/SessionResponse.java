package org.mehlib.marked.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.SessionType;

public record SessionResponse(
    Long id,
    LocalDateTime date,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    SessionType type,
    String sessionCode,
    Instant codeGeneratedAt,
    String courseName,
    String courseLabel,
    String professorName,
    String lectureType,
    List<String> sectionNames,
    Instant createdOn,
    Instant lastUpdatedOn
) {
    public static SessionResponse fromEntity(Session session) {
        return fromEntity(session, false);
    }

    public static SessionResponse fromEntity(Session session, boolean includeCode) {
        LocalDateTime date = null;
        DayOfWeek dayOfWeek = null;
        if (session.getCalendar() != null) {
            date = session.getCalendar().getDate();
            dayOfWeek = session.getCalendar().getDayOfWeek();
        }

        String courseName = null;
        String courseLabel = null;
        String professorName = null;
        String lectureType = null;
        if (session.getTimeTable() != null
            && session.getTimeTable().getCourseAssignment() != null) {
            var assignment = session.getTimeTable().getCourseAssignment();

            if (assignment.getCourse() != null) {
                courseName = assignment.getCourse().getName();
                courseLabel = assignment.getCourse().getLabel();
            }

            if (assignment.getProfessor() != null) {
                professorName = assignment.getProfessor().getFullName();
            }

            if (assignment.getType() != null) {
                lectureType = assignment.getType().name();
            }
        }

        List<String> sectionNames = session.getSections()
            .stream()
            .map(s -> s.getName())
            .toList();

        return new SessionResponse(
            session.getId(),
            date,
            dayOfWeek,
            session.getStartTime(),
            session.getEndTime(),
            session.getType(),
            includeCode ? session.getSessionCode() : null,
            includeCode ? session.getCodeGeneratedAt() : null,
            courseName,
            courseLabel,
            professorName,
            lectureType,
            sectionNames,
            session.getCreatedOn(),
            session.getLastUpdatedOn()
        );
    }
}
