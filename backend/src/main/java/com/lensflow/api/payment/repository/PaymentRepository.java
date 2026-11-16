package com.lensflow.api.payment.repository;

import com.lensflow.api.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByShootingOwnerIdOrderByPaymentDateAsc(Long ownerId);
    Optional<Payment> findByIdAndShootingOwnerId(Long id, Long ownerId);
    List<Payment> findByShootingIdAndShootingOwnerIdOrderByPaymentDateAsc(Long shootingId, Long ownerId);
}
