package com.lensflow.api.payment.repository;

import com.lensflow.api.payment.entity.Payment;
import com.lensflow.api.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByShootingIdOrderByPaymentDateAsc(Long shootingId);

    List<Payment> findByShootingIdAndStatusOrderByPaymentDateAsc(
            Long shootingId,
            PaymentStatus status
    );
}