package tqs.evsync.backend.model;

import tqs.evsync.backend.model.enums.SessionStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private double energyConsumed; // em kWh

    private double totalCost;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;


    @ManyToOne
    @JoinColumn(name = "outlet_id")
    private ChargingOutlet chargingOutlet;
}
