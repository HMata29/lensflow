
package com.lensflow.api.shooting.repository;

import com.lensflow.api.shooting.entity.Shooting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShootingRepository extends JpaRepository<Shooting, Long> {
}
