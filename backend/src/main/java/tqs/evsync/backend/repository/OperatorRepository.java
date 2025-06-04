package tqs.evsync.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.Operator;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByEmail(String email);
}
