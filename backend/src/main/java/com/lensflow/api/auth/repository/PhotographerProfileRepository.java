package com.lensflow.api.auth.repository;

import com.lensflow.api.auth.entity.PhotographerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhotographerProfileRepository
        extends JpaRepository<PhotographerProfile, Long> {

    Optional<PhotographerProfile> findByUserId(Long userId);
}