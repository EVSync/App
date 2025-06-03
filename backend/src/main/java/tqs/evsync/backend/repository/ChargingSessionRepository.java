package tqs.evsync.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tqs.evsync.backend.model.ChargingSession;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession,Long> {
        @Query("SELECT cs FROM ChargingSession cs WHERE cs.reservation.consumer.id = :consumerId")
        List<ChargingSession> findAllByConsumerId(@Param("consumerId") Long consumerId);


}
