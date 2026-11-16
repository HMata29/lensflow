package com.lensflow.api.auth.repository;

import com.lensflow.api.auth.entity.AppModule;
import com.lensflow.api.auth.entity.UserModule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserModuleRepository
        extends JpaRepository<UserModule, Long> {

    List<UserModule> findAllByUserId(Long userId);

    Optional<UserModule> findByUserIdAndModule(
            Long userId,
            AppModule module
    );
}