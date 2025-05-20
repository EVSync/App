package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
