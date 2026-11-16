package com.lensflow.api.shooting.repository;

import com.lensflow.api.shooting.entity.Shooting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShootingRepository extends JpaRepository<Shooting, Long> {
    List<Shooting> findAllByOwnerIdOrderByStartAtAsc(Long ownerId);
    Optional<Shooting> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<Shooting> findByQuoteIdAndOwnerId(Long quoteId, Long ownerId);
}
