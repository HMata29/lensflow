package com.lensflow.api.payment.repository;

import com.lensflow.api.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByShooting_Id(Long shootingId);
}