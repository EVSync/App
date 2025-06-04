package tqs.evsync.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.Consumer;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
    Optional<Consumer> findByEmail(String email);
}
