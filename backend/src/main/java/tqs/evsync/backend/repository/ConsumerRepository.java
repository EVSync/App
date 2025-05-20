package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.Consumer;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
}
