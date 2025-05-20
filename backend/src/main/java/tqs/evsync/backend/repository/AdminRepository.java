package tqs.evsync.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.evsync.backend.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}