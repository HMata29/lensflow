package com.lensflow.api.auth.repository;

import com.lensflow.api.auth.entity.User;
import com.lensflow.api.auth.entity.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByRole(UserRole role);
}