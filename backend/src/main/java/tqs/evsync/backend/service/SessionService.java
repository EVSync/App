package tqs.evsync.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.evsync.backend.model.Reservation;
import tqs.evsync.backend.model.Session;
import tqs.evsync.backend.model.enums.ReservationStatus;
import tqs.evsync.backend.model.enums.SessionStatus;
import tqs.evsync.backend.repository.SessionRepository;
import tqs.evsync.backend.repository.ReservationRepository;
import tqs.evsync.backend.model.Consumer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    public SessionService(SessionRepository sessionRepository,ReservationRepository reservationRepository) {
        this.sessionRepository = sessionRepository;
        this.reservationRepository = reservationRepository;
    }

    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    public Session startSessionFromReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found."));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Reservation must be CONFIRMED to start session.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = LocalDateTime.parse(reservation.getStartTime());

        // if (now.isBefore(scheduledStart.minusMinutes(15)) || now.isAfter(scheduledStart.plusMinutes(15))) {
        //     throw new RuntimeException("Session can only be started 15 minutes before/after the scheduled start.");
        // }

        Session session = Session.builder()
            .startTime(now)
            .status(SessionStatus.ACTIVE)
            .reservation(reservation)
            .chargingOutlet(reservation.getOutlet())
            .build();

        return sessionRepository.save(session);
    }


    public Session endSession(Long sessionId, double energyUsed) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
    
        LocalDateTime endTime = LocalDateTime.now();
        session.setEndTime(endTime);
        session.setEnergyConsumed(energyUsed);
        session.setStatus(SessionStatus.COMPLETED);
    
        double durationInHours = (double) java.time.Duration.between(session.getStartTime(), endTime).toMinutes() / 60.0;
        double costPerHour = session.getChargingOutlet().getCostPerHour();
    
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
    

    public Optional<Session> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    public List<Session> getAllSessions(){
        return sessionRepository.findAll();
    }

    public void deleteSession(Long id) {
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        sessionRepository.delete(session);
    }
}
