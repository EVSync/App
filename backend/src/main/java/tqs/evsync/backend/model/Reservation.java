package tqs.evsync.backend.model;

import jakarta.persistence.*;

import tqs.evsync.backend.model.enums.ReservationStatus;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startTime;
    private Double duration;
    
    @ManyToOne
    @JoinColumn(name = "station_id")
    private ChargingStation station;


    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;

    public Long getId() {
        return id;
    }

    public String getStartTime() {
        return startTime;
    }

    public Double getDuration() {
        return duration;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
