package tqs.evsync.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.evsync.backend.model.Reservation;
import tqs.evsync.backend.model.ChargingSession;
import tqs.evsync.backend.model.enums.ChargingSessionStatus;
import tqs.evsync.backend.model.enums.ReservationStatus;
import tqs.evsync.backend.repository.ChargingSessionRepository;
import tqs.evsync.backend.repository.ReservationRepository;
import tqs.evsync.backend.model.Consumer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private ChargingSessionRepository sessionRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    public SessionService(ChargingSessionRepository sessionRepository,ReservationRepository reservationRepository) {
        this.sessionRepository = sessionRepository;
        this.reservationRepository = reservationRepository;
    }

    public ChargingSession createSession(ChargingSession session) {
        return sessionRepository.save(session);
    }

    public ChargingSession startSessionFromReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found."));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Reservation must be CONFIRMED to start session.");
        }

        LocalDateTime scheduledStart = LocalDateTime.parse(reservation.getStartTime());

        ChargingSession session = new ChargingSession();
        session.setStartTime(scheduledStart);
        session.setStatus(ChargingSessionStatus.ACTIVE);
        session.setReservation(reservation);
        session.setOutlet(reservation.getOutlet());

        return sessionRepository.save(session);
    }


    public ChargingSession endSession(Long sessionId, double energyUsed) {
        ChargingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
    
        LocalDateTime endTime = LocalDateTime.now();
        session.setEndTime(endTime);
        session.setEnergyConsumed(energyUsed);
        session.setStatus(ChargingSessionStatus.COMPLETED);
    
        double durationInHours = (double) java.time.Duration.between(session.getStartTime(), endTime).toMinutes() / 60.0;
        double costPerHour = session.getOutlet().getCostPerHour();
    
        double totalCost = durationInHours * costPerHour;
        session.setTotalCost(totalCost);
    
        Reservation reservation = session.getReservation();
        if (reservation != null) {
            double alreadyPaid = reservation.getReservationFee(); 
            double remainingToPay = totalCost - alreadyPaid;
    
            if (remainingToPay > 0) {
                Consumer consumer = reservation.getConsumer();
    
                if (consumer.getWallet() < remainingToPay) {
                    throw new RuntimeException("Saldo insuficiente para pagar o restante da sessão.");
                }
    
                consumer.setWallet(consumer.getWallet() - remainingToPay);
            }
        }
    
        return sessionRepository.save(session);
    }
    

    public Optional<ChargingSession> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    public List<ChargingSession> getAllSessions(){
        return sessionRepository.findAll();
    }

    public void deleteSession(Long id) {
        ChargingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        sessionRepository.delete(session);
    }
}
