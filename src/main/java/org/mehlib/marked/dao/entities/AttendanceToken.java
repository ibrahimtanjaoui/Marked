package org.mehlib.marked.dao.entities;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing a single-use attendance verification token.
 * This token is sent to the student's email after they enter the teacher's session code
 * and their location is verified. The token must be used to complete attendance marking.
 */
@Entity
@Table(name = "attendance_tokens", indexes = {
    @Index(name = "idx_attendance_token_token", columnList = "token", unique = true),
    @Index(name = "idx_attendance_token_student_session", columnList = "student_id, session_id")
})
public class AttendanceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique token string sent to the student's email.
     * This is a secure random string that the student must enter to confirm attendance.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /**
     * The student this token was issued to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * The session this token is valid for.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    /**
     * The session code that was validated when this token was created.
     * Stored for audit purposes.
     */
    @Column(name = "validated_session_code", nullable = false, length = 10)
    private String validatedSessionCode;

    /**
     * The latitude from which the student requested the token.
     */
    @Column(name = "request_latitude")
    private Double requestLatitude;

    /**
     * The longitude from which the student requested the token.
     */
    @Column(name = "request_longitude")
    private Double requestLongitude;

    /**
     * When the token was created.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * When the token expires.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Whether this token has been used.
     */
    @Column(nullable = false)
    private boolean used = false;

    /**
     * When the token was used (null if not used).
     */
    @Column(name = "used_at")
    private Instant usedAt;

    // Default constructor for JPA
    public AttendanceToken() {
    }

    // Constructor for creating a new token
    public AttendanceToken(String token, Student student, Session session,
                           String validatedSessionCode, Double latitude, Double longitude,
                           Instant createdAt, Instant expiresAt) {
        this.token = token;
        this.student = student;
        this.session = session;
        this.validatedSessionCode = validatedSessionCode;
        this.requestLatitude = latitude;
        this.requestLongitude = longitude;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    /**
     * Check if this token is valid (not used and not expired).
     */
    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    /**
     * Mark this token as used.
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getValidatedSessionCode() {
        return validatedSessionCode;
    }

    public void setValidatedSessionCode(String validatedSessionCode) {
        this.validatedSessionCode = validatedSessionCode;
    }

    public Double getRequestLatitude() {
        return requestLatitude;
    }

    public void setRequestLatitude(Double requestLatitude) {
        this.requestLatitude = requestLatitude;
    }

    public Double getRequestLongitude() {
        return requestLongitude;
    }

    public void setRequestLongitude(Double requestLongitude) {
        this.requestLongitude = requestLongitude;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }
}
