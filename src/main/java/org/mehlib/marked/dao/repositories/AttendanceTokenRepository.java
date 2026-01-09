package org.mehlib.marked.dao.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.AttendanceToken;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceTokenRepository extends JpaRepository<AttendanceToken, Long> {

    /**
     * Find a token by its unique token string.
     */
    Optional<AttendanceToken> findByToken(String token);

    /**
     * Find all tokens for a specific student.
     */
    List<AttendanceToken> findByStudent(Student student);

    /**
     * Find all tokens for a specific session.
     */
    List<AttendanceToken> findBySession(Session session);

    /**
     * Find a token for a specific student and session.
     */
    Optional<AttendanceToken> findByStudentAndSession(Student student, Session session);

    /**
     * Find all unused, non-expired tokens for a student and session.
     */
    @Query("SELECT t FROM AttendanceToken t WHERE t.student = :student AND t.session = :session " +
           "AND t.used = false AND t.expiresAt > :now")
    Optional<AttendanceToken> findValidTokenForStudentAndSession(
        @Param("student") Student student,
        @Param("session") Session session,
        @Param("now") Instant now
    );

    /**
     * Find a valid (unused and not expired) token by its token string.
     */
    @Query("SELECT t FROM AttendanceToken t WHERE t.token = :token " +
           "AND t.used = false AND t.expiresAt > :now")
    Optional<AttendanceToken> findValidToken(
        @Param("token") String token,
        @Param("now") Instant now
    );

    /**
     * Check if a valid token exists for a student and session.
     */
    @Query("SELECT COUNT(t) > 0 FROM AttendanceToken t WHERE t.student = :student " +
           "AND t.session = :session AND t.used = false AND t.expiresAt > :now")
    boolean existsValidTokenForStudentAndSession(
        @Param("student") Student student,
        @Param("session") Session session,
        @Param("now") Instant now
    );

    /**
     * Delete all expired tokens (cleanup).
     */
    @Modifying
    @Query("DELETE FROM AttendanceToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Delete all used tokens older than a certain date (cleanup).
     */
    @Modifying
    @Query("DELETE FROM AttendanceToken t WHERE t.used = true AND t.usedAt < :before")
    int deleteUsedTokensBefore(@Param("before") Instant before);

    /**
     * Count unused tokens for a student (for rate limiting).
     */
    @Query("SELECT COUNT(t) FROM AttendanceToken t WHERE t.student = :student " +
           "AND t.used = false AND t.createdAt > :since")
    long countRecentUnusedTokensForStudent(
        @Param("student") Student student,
        @Param("since") Instant since
    );
}
