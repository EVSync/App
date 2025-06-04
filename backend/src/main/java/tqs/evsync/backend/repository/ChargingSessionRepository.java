package tqs.evsync.backend.repository;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tqs.evsync.backend.model.ChargingSession;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession,Long> {
        @Query("SELECT cs FROM ChargingSession cs WHERE cs.reservation.consumer.id = :consumerId ORDER BY cs.startTime DESC")
        List<ChargingSession> chargingHistoryById(@Param("consumerId") Long consumerId);

        List<ChargingSession> findAllByReservation_Consumer(Consumer consumer);


}
