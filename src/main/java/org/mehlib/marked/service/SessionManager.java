package org.mehlib.marked.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Session;
import org.mehlib.marked.dao.repositories.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SessionManager implements SessionService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SessionRepository sessionRepository;

    public SessionManager(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> getSession(Long id) {
        return sessionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> getSessionWithDetails(Long id) {
        return sessionRepository.findByIdWithDetails(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Session updateSession(Session session) {
        if (session.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update session without an ID"
            );
        }
        return sessionRepository.save(session);
    }

    @Override
    public void deleteSession(Session session) {
        sessionRepository.delete(session);
    }

    @Override
    public void deleteSessionById(Long id) {
        sessionRepository.deleteById(id);
    }

    @Override
    public Session generateSessionCode(Long sessionId) {
        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Session not found with ID: " + sessionId
                )
            );

        // Only generate if no code exists
        if (session.getSessionCode() == null) {
            session.setSessionCode(generateRandomCode());
            session.setCodeGeneratedAt(Instant.now());
            return sessionRepository.save(session);
        }

        return session;
    }

    @Override
    public Session regenerateSessionCode(Long sessionId) {
        Session session = sessionRepository
            .findById(sessionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Session not found with ID: " + sessionId
                )
            );

        session.setSessionCode(generateRandomCode());
        session.setCodeGeneratedAt(Instant.now());
        return sessionRepository.save(session);
    }

    private String generateRandomCode() {
        int code = RANDOM.nextInt(900000) + 100000; // 6-digit code: 100000-999999
        return String.valueOf(code);
    }
}
