package tqs.evsync.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.model.enums.ReservationStatus;
import tqs.evsync.backend.model.enums.OutletStatus;

import tqs.evsync.backend.model.enums.ChargingSessionStatus;
import tqs.evsync.backend.repository.*;

@Service
public class ChargingSessionService {

    private final ChargingSessionRepository sessionRepo;
    private final ReservationRepository    reservationRepo;
    private final ChargingOutletRepository outletRepo;

    public ChargingSessionService(ChargingSessionRepository s,
                                  ReservationRepository    r,
                                  ChargingOutletRepository o) {
        this.sessionRepo     = s;
        this.reservationRepo = r;
        this.outletRepo      = o;
    }

    @Transactional
    public ChargingSession startSession(Long reservationId) {
        // 1) lookup reservation & outlet
        Reservation res = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservação não encontrada"));
        ChargingOutlet outlet = res.getOutlet();
        // 2) mark both reservation & outlet in use
        if (res.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Reserva não confirmada");
        }
        res.setStatus(ReservationStatus.IN_PROGRESS);
        outlet.setStatus(OutletStatus.OCCUPIED);     // <-- you’ll need an enum/field
        reservationRepo.save(res);
        outletRepo.save(outlet);

        // 3) create session
        ChargingSession session = new ChargingSession();
        session.setReservation(res);
        session.setOutlet(outlet);
        session.setStatus(ChargingSessionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        return sessionRepo.save(session);
    }

    @Transactional
    public ChargingSession stopSession(Long sessionId) {
        ChargingSession session = sessionRepo.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada"));

        if (session.getStatus() != ChargingSessionStatus.ACTIVE) {
            throw new IllegalStateException("Sessão não está ativa");
        }

        // 1) stamp end time
        session.setEndTime(LocalDateTime.now());

        // 2) compute based on the reservation’s booked duration
        double hoursBooked     = session.getReservation().getDuration();         // e.g. 1.0
        double costPerHour     = session.getOutlet().getCostPerHour();           // €/h
        double maxPower        = session.getOutlet().getMaxPower();              // kW

        // energy in kWh = power (kW) * hours
        double energyConsumed  = hoursBooked * maxPower;
        double totalCost       = hoursBooked * costPerHour;

        session.setEnergyConsumed(energyConsumed);
        session.setTotalCost(totalCost);
        session.setStatus(ChargingSessionStatus.COMPLETED);

        // 3) free up the outlet
        ChargingOutlet outlet = session.getOutlet();
        outlet.setStatus(OutletStatus.AVAILABLE);
        outletRepo.save(outlet);

        // 4) mark reservation completed
        Reservation res = session.getReservation();
        res.setStatus(ReservationStatus.COMPLETED);
        reservationRepo.save(res);

        return sessionRepo.save(session);
    }
    public ChargingSession getSessionById(Long sessionId) {
        return sessionRepo.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada"));
    }
}
