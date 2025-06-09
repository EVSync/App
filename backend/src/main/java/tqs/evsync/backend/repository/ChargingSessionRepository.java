package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.ChargingSession;

public interface ChargingSessionRepository 
        extends JpaRepository<ChargingSession,Long> {
}