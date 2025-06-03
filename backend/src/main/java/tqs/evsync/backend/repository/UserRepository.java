package tqs.evsync.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tqs.evsync.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
