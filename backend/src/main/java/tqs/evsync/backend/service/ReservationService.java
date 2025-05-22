package tqs.evsync.backend.service;

import org.springframework.stereotype.Service;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.repository.*;
import tqs.evsync.backend.model.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ChargingStationRepository stationRepo;
    private final ConsumerRepository consumerRepo;
    private final ChargingOutletRepository outletRepo;

    public ReservationService(ReservationRepository r, ConsumerRepository c, ChargingStationRepository s, ChargingOutletRepository o) {
        this.reservationRepo = r;
        this.consumerRepo = c;
        this.stationRepo = s;
        this.outletRepo = o;
    }

    public Reservation createReservation(Long consumerId, Long stationId, Long chargingOutletId, String startTime, Double duration) {
        Optional<Consumer> consumerOpt = consumerRepo.findById(consumerId);
        Optional<ChargingStation> stationOpt = stationRepo.findById(stationId);
        Optional<ChargingOutlet> outletOpt = outletRepo.findById(chargingOutletId);

        if (consumerOpt.isEmpty() || stationOpt.isEmpty() || outletOpt.isEmpty()) {
            throw new IllegalArgumentException("Consumer or Station or Outlet not found.");
        }

        // Check if the reservation time slot is valid
        if (!isReservationTimeSlotValid(stationId, startTime, duration)) {
            throw new IllegalArgumentException("Time slot is not available.");
        }

        Reservation r = new Reservation();
        r.setConsumer(consumerOpt.get());
        r.setStartTime(startTime);
        r.setDuration(duration);
        r.setStatus(ReservationStatus.PENDING);
        r.setStation(stationOpt.get());
        r.setOutlet(outletOpt.get());

        return reservationRepo.save(r);
    }

    public Reservation confirmReservation(Long reservationId) {
        Reservation r = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Not found"));
    
        r.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepo.save(r);
    }
    
    public Reservation cancelReservation(Long reservationId) {
        Reservation r = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Not found"));
    
        r.setStatus(ReservationStatus.CANCELLED);
        return reservationRepo.save(r);
    }

    public Reservation processPayment(Long reservationId, double costPerHour) {
        Reservation r = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Not found"));
    
        Consumer consumer = r.getConsumer();
        double total = r.getDuration() * costPerHour;
    
        if (consumer.getWallet() < total) {
            throw new IllegalStateException("Saldo insuficiente");
        }
    
        consumer.setWallet((int)(consumer.getWallet() - total));
        r.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepo.save(r);
    }

    public boolean isReservationTimeSlotValid(Long stationId, String startTime, Double duration) {
        LocalDateTime requestedStart = LocalDateTime.parse(startTime);
        LocalDateTime requestedEnd = requestedStart.plusMinutes((long)(duration * 60));
        
        return reservationRepo.findAllByStationId(stationId).stream()
            .noneMatch(r -> {
                LocalDateTime existingStart = LocalDateTime.parse(r.getStartTime());
                LocalDateTime existingEnd = existingStart.plusMinutes((long)(r.getDuration() * 60));
                
                return requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart);
            });
    }
    
    
}
