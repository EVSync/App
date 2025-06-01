package tqs.evsync.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.evsync.backend.model.ChargingSession;
import tqs.evsync.backend.service.ChargingSessionService;

@RestController
@RequestMapping("/api/sessions")
public class ChargingSessionController {

    private final ChargingSessionService svc;

    public ChargingSessionController(ChargingSessionService svc) {
        this.svc = svc;
    }

    @PostMapping("/start/{reservationId}")
    public ResponseEntity<ChargingSession> start(
            @PathVariable Long reservationId) {
        ChargingSession session = svc.startSession(reservationId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/stop/{sessionId}")
    public ResponseEntity<ChargingSession> stop(
            @PathVariable Long sessionId) {
        ChargingSession session = svc.stopSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ChargingSession> getSession(
            @PathVariable Long sessionId) {
        ChargingSession session = svc.getSessionById(sessionId);
        return ResponseEntity.ok(session);
    }
}
