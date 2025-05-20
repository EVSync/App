package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.ChargingStation;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
}
