package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.Reservation;
import tqs.evsync.backend.service.ReservationService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // TQS-56: Confirm reservation details (criar reserva)
    @PostMapping
    public ResponseEntity<?> createReservation(
        @RequestParam Long consumerId,
        @RequestParam Long stationId,
        @RequestParam String startTime,
        @RequestParam Double duration
    ) {
        try {
            Reservation reservation = reservationService.createReservation(consumerId, stationId, startTime, duration);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // TQS-57: Receive reservation confirmation (ver estado da reserva)
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        try {
            Reservation r = reservationService.getReservationById(id);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // TQS-56 (opcional): Confirmar reserva
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable Long id) {
        try {
            Reservation confirmed = reservationService.confirmReservation(id);
            return ResponseEntity.ok(confirmed);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(402).body("Pagamento falhou: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao confirmar: " + e.getMessage());
        }
    }

    // TQS-58: Cancelar reserva
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservationService.cancelReservation(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
