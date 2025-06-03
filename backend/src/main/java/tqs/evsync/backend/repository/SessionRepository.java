package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.ChargingSession;

public interface SessionRepository extends JpaRepository<ChargingSession, Long> {
}
