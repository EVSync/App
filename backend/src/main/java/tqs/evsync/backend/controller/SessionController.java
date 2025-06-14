package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.ChargingSession;
import tqs.evsync.backend.service.SessionService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;



    @PostMapping("/start")
    public ResponseEntity<?> startSessionFromReservation(@RequestParam Long reservationId) {
        try {
            ChargingSession session = sessionService.startSessionFromReservation(reservationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable Long id) {
        return sessionService.getSessionById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found."));
    }

    @GetMapping
    public List<ChargingSession> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<?> endSession(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            double energyUsed = Double.parseDouble(payload.get("energyUsed").toString());

            ChargingSession ended = sessionService.endSession(id, energyUsed);
            return ResponseEntity.ok(ended);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSession(@PathVariable Long id) {
        try {
            sessionService.deleteSession(id);
            return ResponseEntity.ok("Session deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found.");
        }
    }
}
