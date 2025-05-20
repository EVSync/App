package tqs.evsync.backend.service;

import org.springframework.stereotype.Service;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.repository.*;
import tqs.evsync.backend.model.enums.ReservationStatus;

import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ChargingStationRepository stationRepo;
    private final ConsumerRepository consumerRepo;

    public ReservationService(ReservationRepository r, ConsumerRepository c, ChargingStationRepository s) {
        this.reservationRepo = r;
        this.consumerRepo = c;
        this.stationRepo = s;
    }

    public Reservation createReservation(Long consumerId, Long stationId, String startTime, Double duration) {
        Optional<Consumer> consumerOpt = consumerRepo.findById(consumerId);
        Optional<ChargingStation> stationOpt = stationRepo.findById(stationId);

        if (consumerOpt.isEmpty() || stationOpt.isEmpty()) {
            throw new IllegalArgumentException("Consumer or Station not found.");
        }

        Reservation r = new Reservation();
        r.setConsumer(consumerOpt.get());
        r.setStartTime(startTime);
        r.setDuration(duration);
        r.setStatus(ReservationStatus.PENDING);

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
    
    
}
