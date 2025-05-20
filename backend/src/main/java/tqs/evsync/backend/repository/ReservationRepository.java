package tqs.evsync.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByStationId(Long stationId);
}
