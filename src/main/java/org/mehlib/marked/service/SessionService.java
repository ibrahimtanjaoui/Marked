package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Session;

public interface SessionService {
    Session createSession(Session session);

    Optional<Session> getSession(Long id);

    Optional<Session> getSessionWithDetails(Long id);

    List<Session> getAllSessions();

    Session updateSession(Session session);

    void deleteSession(Session session);

    void deleteSessionById(Long id);

    /**
     * Generate a new 6-digit session code for attendance marking.
     * This code should be displayed by the professor for students to enter.
     *
     * @param sessionId the ID of the session
     * @return the updated session with the new code
     */
    Session generateSessionCode(Long sessionId);

    /**
     * Regenerate the session code (e.g., if compromised).
     *
     * @param sessionId the ID of the session
     * @return the updated session with a new code
     */
    Session regenerateSessionCode(Long sessionId);
}
