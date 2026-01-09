package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.AttendanceToken;
import org.mehlib.marked.dao.entities.Attendance;

/**
 * Service interface for managing attendance verification tokens.
 * Implements a two-step attendance marking flow:
 * 1. Student enters session code + provides location -> receives email with token
 * 2. Student enters token -> attendance is marked
 */
public interface AttendanceTokenService {

    /**
     * Request an attendance verification token.
     * Validates session code, time window, student section, and geolocation.
     * If valid, generates a token and emails it to the student.
     *
     * @param studentId   the ID of the student
     * @param sessionId   the ID of the session
     * @param sessionCode the 6-digit code displayed by professor
     * @param latitude    the student's current latitude
     * @param longitude   the student's current longitude
     * @return the generated token (for logging/debugging, token is also emailed)
     * @throws IllegalArgumentException if student/session not found
     * @throws IllegalStateException    if validation fails (code, time, section, or location)
     */
    AttendanceToken requestToken(
        Long studentId,
        Long sessionId,
        String sessionCode,
        Double latitude,
        Double longitude
    );

    /**
     * Confirm attendance using a verification token.
     * Validates the token and marks the student as present.
     *
     * @param studentId the ID of the student (must match token's student)
     * @param token     the verification token received via email
     * @return the created attendance record
     * @throws IllegalArgumentException if student not found or token doesn't match student
     * @throws IllegalStateException    if token is invalid, expired, or already used
     */
    Attendance confirmAttendance(Long studentId, String token);

    /**
     * Validate geolocation against the configured college location.
     *
     * @param latitude  the latitude to check
     * @param longitude the longitude to check
     * @return true if the location is within the allowed radius
     */
    boolean isWithinAllowedRadius(Double latitude, Double longitude);

    /**
     * Calculate the distance between two points using the Haversine formula.
     *
     * @param lat1 latitude of point 1
     * @param lon1 longitude of point 1
     * @param lat2 latitude of point 2
     * @param lon2 longitude of point 2
     * @return distance in meters
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    /**
     * Check if a valid (unused, not expired) token exists for a student and session.
     *
     * @param studentId the ID of the student
     * @param sessionId the ID of the session
     * @return true if a valid token exists
     */
    boolean hasValidToken(Long studentId, Long sessionId);

    /**
     * Clean up expired and old used tokens.
     * Should be called periodically (e.g., via scheduled task).
     *
     * @return the number of tokens deleted
     */
    int cleanupExpiredTokens();
}
