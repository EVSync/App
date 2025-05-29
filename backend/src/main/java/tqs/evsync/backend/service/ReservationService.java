package tqs.evsync.backend.service;

import org.springframework.stereotype.Service;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.repository.*;
import tqs.evsync.backend.model.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

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

    public Reservation createReservation(Long consumerId, Long stationId, String startTime, Double duration) {
        Optional<Consumer> consumerOpt = consumerRepo.findById(consumerId);
        Optional<ChargingStation> stationOpt = stationRepo.findById(stationId);
    
        if (consumerOpt.isEmpty() || stationOpt.isEmpty()) {
            throw new IllegalArgumentException("Consumer or Station not found.");
        }
    
        ChargingStation station = stationOpt.get();
        ChargingOutlet selectedOutlet = findAvailableOutlet(station, startTime, duration);
    
        if (selectedOutlet == null) {
            throw new IllegalStateException("No available outlet at the selected time.");
        }
    
        Reservation r = new Reservation();
        r.setConsumer(consumerOpt.get());
        r.setStartTime(startTime);
        r.setDuration(duration);
        r.setStatus(ReservationStatus.PENDING);
        r.setStation(station);
        r.setOutlet(selectedOutlet);
    
        return reservationRepo.save(r);
    }

    private ChargingOutlet findAvailableOutlet(ChargingStation station, String startTime, Double duration) {
        LocalDateTime requestedStart = LocalDateTime.parse(startTime);
        LocalDateTime requestedEnd = requestedStart.plusMinutes((long)(duration * 60));
    
        for (ChargingOutlet outlet : station.getChargingOutlets()) {
            boolean isFree = reservationRepo.findAll().stream()
                .filter(r -> r.getOutlet() != null && r.getOutlet().getId().equals(outlet.getId()))
                .noneMatch(r -> {
                    LocalDateTime existingStart = LocalDateTime.parse(r.getStartTime());
                    LocalDateTime existingEnd = existingStart.plusMinutes((long)(r.getDuration() * 60));
                    return requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart);
                });
    
            if (isFree) {
                return outlet;
            }
        }
    
        return null; 
    }
    
    
    public Reservation getReservationById(Long id) {
        return reservationRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    }
    

    public Reservation confirmReservation(Long reservationId) {
        Reservation r = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Not found"));
    
        ChargingOutlet outlet = r.getOutlet();
        if (outlet == null) {
            throw new IllegalStateException("No outlet associated with this reservation.");
        }
    
        double estimatedCost = r.getDuration() * outlet.getCostPerHour();
        double reservationFee = estimatedCost * 0.2; 
        Consumer consumer = r.getConsumer();

        if (consumer.getWallet() < reservationFee) {
            throw new IllegalStateException("Saldo insuficiente para pagar a taxa de reserva.");
        }
        consumer.setWallet(consumer.getWallet() - reservationFee);
        r.setReservationFee(reservationFee);
        r.setStatus(ReservationStatus.CONFIRMED);

        return reservationRepo.save(r);
    }
    
    
    public Reservation cancelReservation(Long reservationId) {
        Reservation r = reservationRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Not found"));
    
        r.setStatus(ReservationStatus.CANCELLED);
        return reservationRepo.save(r);
    }


    
    
    
}
