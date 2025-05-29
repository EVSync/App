package tqs.evsync.backend.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.Session;
import tqs.evsync.backend.model.enums.SessionStatus;
import tqs.evsync.backend.repository.SessionRepository;
import tqs.evsync.backend.service.SessionService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SessionServiceTest {

    // private SessionRepository sessionRepository;
    // private SessionService sessionService;

    // @BeforeEach
    // public void setUp() {
    //     sessionRepository = mock(SessionRepository.class);
    //     sessionService = new SessionService(sessionRepository);
    // }

    // @Test
    // public void testCreateSession() {
    //     Session session = new Session();
    //     session.setStatus(SessionStatus.ACTIVE);

    //     when(sessionRepository.save(session)).thenReturn(session);

    //     Session created = sessionService.createSession(session);

    //     assertNotNull(created);
    //     assertEquals(SessionStatus.ACTIVE, created.getStatus());
    //     verify(sessionRepository, times(1)).save(session);
    // }

    // @Test
    // public void testGetSessionById_Found() {
    //     Session session = new Session();
    //     session.setId(1L);
    //     session.setStatus(SessionStatus.COMPLETED);

    //     when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

    //     Optional<Session> result = sessionService.getSessionById(1L);

    //     assertTrue(result.isPresent());
    //     assertEquals(SessionStatus.COMPLETED, result.get().getStatus());
    //     verify(sessionRepository, times(1)).findById(1L);
    // }

    // @Test
    // public void testGetSessionById_NotFound() {
    //     when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

    //     Optional<Session> result = sessionService.getSessionById(999L);
        
    //     assertFalse(result.isPresent());
    //     verify(sessionRepository, times(1)).findById(999L);
    // }
}
