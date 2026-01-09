package org.mehlib.marked.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.entities.AttendanceStatus;
import org.mehlib.marked.dao.entities.AttendanceToken;
import org.mehlib.marked.dao.entities.Section;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.SessionType;
import org.mehlib.marked.dao.entities.Student;
import org.mehlib.marked.dao.repositories.AttendanceRepository;
import org.mehlib.marked.dao.repositories.AttendanceTokenRepository;
import org.mehlib.marked.dao.repositories.SessionRepository;
import org.mehlib.marked.dao.repositories.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AttendanceTokenService.
 * Handles the two-step attendance verification flow with geolocation and email tokens.
 */
@Service
@Transactional
public class AttendanceTokenManager implements AttendanceTokenService {

    private static final Logger log = LoggerFactory.getLogger(
        AttendanceTokenManager.class
    );
    private static final int TIME_WINDOW_MINUTES_BEFORE = 5;
    private static final String TOKEN_CHARS =
        "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TOKEN_LENGTH = 8;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private final AttendanceTokenRepository tokenRepository;
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final MailService mailService;
    private final SecureRandom secureRandom;

    @Value("${app.attendance.token-expiry-minutes:10}")
    private int tokenExpiryMinutes;

    public AttendanceTokenManager(
        AttendanceTokenRepository tokenRepository,
        StudentRepository studentRepository,
        SessionRepository sessionRepository,
        AttendanceRepository attendanceRepository,
        MailService mailService
    ) {
        this.tokenRepository = tokenRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.mailService = mailService;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public AttendanceToken requestToken(
        Long studentId,
        Long sessionId,
        String sessionCode,
        Double latitude,
        Double longitude
    ) {
        log.info(
            "Token request for student {} session {} at location ({}, {})",
            studentId,
            sessionId,
            latitude,
            longitude
        );

        // Fetch entities
        Student student = studentRepository
            .findById(studentId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Student not found with ID: " + studentId
                )
            );

        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Session not found with ID: " + sessionId
                )
            );

        // Validate session is not cancelled
        if (session.getType() == SessionType.CANCELLED) {
            throw new IllegalStateException(
                "Cannot mark attendance for a cancelled session"
            );
        }

        // Validate student's section is part of this session
        validateStudentSectionInSession(student, session);

        // Validate session code
        validateSessionCode(session, sessionCode);

        // Validate time window
        validateTimeWindow(session);

        // Validate geolocation
        if (latitude == null || longitude == null) {
            throw new IllegalStateException(
                "Location is required. Please enable location services."
            );
        }

        // Get institution location from student's institution
        var institution = student.getInstitution();
        if (institution == null) {
            throw new IllegalStateException(
                "Student is not associated with any institution"
            );
        }

        // Validate geolocation against institution location
        if (
            !isWithinRadius(
                latitude,
                longitude,
                institution.getLatitude(),
                institution.getLongitude(),
                institution.getRadiusMeters()
            )
        ) {
            double distance = calculateDistance(
                latitude,
                longitude,
                institution.getLatitude(),
                institution.getLongitude()
            );
            log.warn(
                "Student {} attempted to mark attendance from outside allowed radius. Distance: {} meters",
                studentId,
                distance
            );
            throw new IllegalStateException(
                String.format(
                    "You must be within %.0f meters of the campus to mark attendance. " +
                        "Your current distance is approximately %.0f meters.",
                    institution.getRadiusMeters(),
                    distance
                )
            );
        }

        // Check if there's already a valid token for this student/session
        Instant now = Instant.now();
        if (
            tokenRepository.existsValidTokenForStudentAndSession(
                student,
                session,
                now
            )
        ) {
            log.info(
                "Valid token already exists for student {} session {}",
                studentId,
                sessionId
            );
            throw new IllegalStateException(
                "A verification code has already been sent to your email. " +
                    "Please check your inbox or wait for it to expire before requesting a new one."
            );
        }

        // Generate unique token
        String tokenString = generateUniqueToken();

        // Create token entity
        Instant expiresAt = now.plus(tokenExpiryMinutes, ChronoUnit.MINUTES);
        AttendanceToken token = new AttendanceToken(
            tokenString,
            student,
            session,
            sessionCode,
            latitude,
            longitude,
            now,
            expiresAt
        );

        token = tokenRepository.save(token);
        log.info(
            "Created attendance token {} for student {} session {}",
            tokenString,
            studentId,
            sessionId
        );

        // Build session info for email
        String sessionInfo = buildSessionInfo(session);

        // Send email with token
        try {
            mailService.sendAttendanceToken(
                student.getEmail(),
                student.getFirstName() + " " + student.getFamilyName(),
                tokenString,
                sessionInfo,
                tokenExpiryMinutes
            );
        } catch (Exception e) {
            log.error(
                "Failed to send token email to {}: {}",
                student.getEmail(),
                e.getMessage()
            );
            // Still return the token even if email fails (for testing/debugging)
            // In production, you might want to rollback or handle differently
        }

        return token;
    }

    @Override
    public Attendance confirmAttendance(Long studentId, String tokenString) {
        log.info(
            "Confirming attendance for student {} with token {}",
            studentId,
            tokenString
        );

        if (tokenString == null || tokenString.isBlank()) {
            throw new IllegalArgumentException("Verification code is required");
        }

        // Normalize token (uppercase, trim)
        tokenString = tokenString.trim().toUpperCase();

        // Find and validate token
        Instant now = Instant.now();
        AttendanceToken token = tokenRepository
            .findValidToken(tokenString, now)
            .orElseThrow(() ->
                new IllegalStateException(
                    "Invalid or expired verification code. Please request a new one."
                )
            );

        // Verify token belongs to the requesting student
        if (!token.getStudent().getId().equals(studentId)) {
            log.warn(
                "Student {} attempted to use token belonging to student {}",
                studentId,
                token.getStudent().getId()
            );
            throw new IllegalArgumentException(
                "This verification code was not issued to you."
            );
        }

        // Mark token as used
        token.markAsUsed();
        tokenRepository.save(token);

        // Create or update attendance record
        Student student = token.getStudent();
        Session session = token.getSession();

        Attendance attendance = attendanceRepository
            .findByStudentAndSession(student, session)
            .orElseGet(() -> {
                Attendance newAttendance = new Attendance();
                newAttendance.setStudent(student);
                newAttendance.setSession(session);
                return newAttendance;
            });

        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance = attendanceRepository.save(attendance);

        log.info(
            "Attendance marked successfully for student {} session {}",
            studentId,
            session.getId()
        );

        return attendance;
    }

    @Override
    public boolean isWithinAllowedRadius(Double latitude, Double longitude) {
        // This method is now deprecated in favor of isWithinRadius with institution params
        // Keeping for interface compatibility, returns true (no check)
        return latitude != null && longitude != null;
    }

    /**
     * Check if a location is within a given radius of a target location.
     */
    private boolean isWithinRadius(
        double latitude,
        double longitude,
        double targetLatitude,
        double targetLongitude,
        double radiusMeters
    ) {
        double distance = calculateDistance(
            latitude,
            longitude,
            targetLatitude,
            targetLongitude
        );
        return distance <= radiusMeters;
    }

    @Override
    public double calculateDistance(
        double lat1,
        double lon1,
        double lat2,
        double lon2
    ) {
        // Haversine formula
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) *
            Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasValidToken(Long studentId, Long sessionId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        Session session = sessionRepository.findById(sessionId).orElse(null);

        if (student == null || session == null) {
            return false;
        }

        return tokenRepository.existsValidTokenForStudentAndSession(
            student,
            session,
            Instant.now()
        );
    }

    @Override
    public int cleanupExpiredTokens() {
        Instant now = Instant.now();
        Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);

        int expiredDeleted = tokenRepository.deleteExpiredTokens(now);
        int usedDeleted = tokenRepository.deleteUsedTokensBefore(oneWeekAgo);

        log.info(
            "Token cleanup: deleted {} expired and {} old used tokens",
            expiredDeleted,
            usedDeleted
        );

        return expiredDeleted + usedDeleted;
    }

    // ============ Private Helper Methods ============

    private String generateUniqueToken() {
        String token;
        int attempts = 0;
        do {
            token = generateRandomToken();
            attempts++;
            if (attempts > 10) {
                throw new IllegalStateException(
                    "Failed to generate unique token after 10 attempts"
                );
            }
        } while (tokenRepository.findByToken(token).isPresent());

        return token;
    }

    private String generateRandomToken() {
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int index = secureRandom.nextInt(TOKEN_CHARS.length());
            sb.append(TOKEN_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private void validateStudentSectionInSession(
        Student student,
        Session session
    ) {
        Section studentSection = student.getSection();
        if (studentSection == null) {
            throw new IllegalStateException(
                "Student is not assigned to any section"
            );
        }

        boolean sectionInSession = session
            .getSections()
            .stream()
            .anyMatch(s -> s.getId().equals(studentSection.getId()));

        if (!sectionInSession) {
            throw new IllegalStateException(
                "Student's section is not scheduled for this session"
            );
        }
    }

    private void validateSessionCode(Session session, String providedCode) {
        String expectedCode = session.getSessionCode();

        if (expectedCode == null || expectedCode.isBlank()) {
            throw new IllegalStateException(
                "Session code has not been generated for this session"
            );
        }

        if (providedCode == null || providedCode.isBlank()) {
            throw new IllegalArgumentException("Session code is required");
        }

        if (!expectedCode.equals(providedCode.trim())) {
            throw new IllegalStateException("Invalid session code");
        }
    }

    private void validateTimeWindow(Session session) {
        LocalTime sessionStart = session.getStartTime();
        LocalTime sessionEnd = session.getEndTime();

        if (sessionStart == null || sessionEnd == null) {
            throw new IllegalStateException("Session times are not configured");
        }

        if (
            session.getCalendar() == null ||
            session.getCalendar().getDate() == null
        ) {
            throw new IllegalStateException("Session date is not configured");
        }

        LocalDate sessionDate = session.getCalendar().getDate().toLocalDate();
        LocalDateTime windowStart = LocalDateTime.of(
            sessionDate,
            sessionStart.minusMinutes(TIME_WINDOW_MINUTES_BEFORE)
        );
        LocalDateTime windowEnd = LocalDateTime.of(sessionDate, sessionEnd);

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(windowStart)) {
            throw new IllegalStateException(
                "Attendance marking has not started yet. Session starts at " +
                    sessionStart
            );
        }

        if (now.isAfter(windowEnd)) {
            throw new IllegalStateException(
                "Attendance marking window has closed. Session ended at " +
                    sessionEnd
            );
        }
    }

    private String buildSessionInfo(Session session) {
        StringBuilder sb = new StringBuilder();

        // Course name
        if (
            session.getTimeTable() != null &&
            session.getTimeTable().getCourseAssignment() != null &&
            session.getTimeTable().getCourseAssignment().getCourse() != null
        ) {
            sb.append(
                session
                    .getTimeTable()
                    .getCourseAssignment()
                    .getCourse()
                    .getName()
            );
        } else {
            sb.append("Session");
        }

        // Date
        if (
            session.getCalendar() != null &&
            session.getCalendar().getDate() != null
        ) {
            sb
                .append(" - ")
                .append(session.getCalendar().getDate().toLocalDate());
        }

        // Time
        if (session.getStartTime() != null && session.getEndTime() != null) {
            sb
                .append(" ")
                .append(session.getStartTime())
                .append("-")
                .append(session.getEndTime());
        }

        return sb.toString();
    }
}
