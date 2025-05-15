package tqs.evsync.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reservation")
public class Reservation {
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;
}
