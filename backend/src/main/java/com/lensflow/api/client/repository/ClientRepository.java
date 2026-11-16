package com.lensflow.api.client.repository;

import com.lensflow.api.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Client> findByIdAndOwnerId(Long id, Long ownerId);
}
