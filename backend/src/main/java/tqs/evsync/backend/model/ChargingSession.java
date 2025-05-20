package tqs.evsync.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.Duration;
import tqs.evsync.backend.model.enums.ChargingSessionStatus;

public class ChargingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Reservation reservation;

    @ManyToOne
    private ChargingOutlet outlet;

    @Enumerated(EnumType.STRING)
    private ChargingSessionStatus status;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double energyConsumed;
    private double totalCost;

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getEnergyConsumed() {
        return energyConsumed;
    }

    public double getTotalCost() {
        if (startTime == null || endTime == null) {
            return 0.0;
        }
        
        long seconds = Duration.between(startTime, endTime).getSeconds();
        double hours = seconds / 3600.0;
        return hours * outlet.getCostPerHour();
    }

    public ChargingOutlet getOutlet() {
        return outlet;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setEnergyConsumed(double energyConsumed) {
        this.energyConsumed = energyConsumed;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public void calculateTotalCost(double costPerKWh) {
        this.totalCost = this.energyConsumed * costPerKWh;
    }

    public void setOutlet(ChargingOutlet outlet) {
        this.outlet = outlet;
    }
}
