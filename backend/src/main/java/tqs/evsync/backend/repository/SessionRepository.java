package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
