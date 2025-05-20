package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.ChargingOutlet;

public interface ChargingOutletRepository extends JpaRepository<ChargingOutlet, Long> {
}
