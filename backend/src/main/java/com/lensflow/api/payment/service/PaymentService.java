
package com.lensflow.api.payment.service;

import com.lensflow.api.exception.ResourceNotFoundException;
import com.lensflow.api.payment.dto.FinancialSummaryResponse;
import com.lensflow.api.payment.dto.PaymentRequest;
import com.lensflow.api.payment.dto.PaymentResponse;
import com.lensflow.api.payment.entity.FinancialStatus;
import com.lensflow.api.payment.entity.Payment;
import com.lensflow.api.payment.entity.PaymentStatus;
import com.lensflow.api.payment.repository.PaymentRepository;
import com.lensflow.api.shooting.entity.Shooting;
import com.lensflow.api.shooting.repository.ShootingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PaymentRepository paymentRepository;
    private final ShootingRepository shootingRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ShootingRepository shootingRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.shootingRepository = shootingRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        return mapToResponse(findPaymentById(id));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByShooting(Long shootingId) {
        findShootingById(shootingId);

        return paymentRepository
                .findByShootingIdOrderByPaymentDateAsc(shootingId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary(Long shootingId) {
        Shooting shooting = findShootingById(shootingId);

        List<Payment> payments = paymentRepository
                .findByShootingIdOrderByPaymentDateAsc(shootingId);

        BigDecimal totalAmount = shooting.getQuote() != null
                ? shooting.getQuote().getTotalAmount()
                : ZERO;

        BigDecimal paidAmount = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.RECEIVED)
                .map(Payment::getAmount)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal pendingAmount = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal remainingAmount = totalAmount
                .subtract(paidAmount)
                .max(ZERO);

        BigDecimal paidPercentage = calculatePaidPercentage(
                totalAmount,
                paidAmount
        );

        FinancialStatus status = calculateFinancialStatus(
                shooting.getQuote() != null,
                totalAmount,
                paidAmount
        );

        return new FinancialSummaryResponse(
                shooting.getId(),
                shooting.getTitle(),
                shooting.getQuote() != null
                        ? shooting.getQuote().getId()
                        : null,
                shooting.getQuote() != null
                        ? shooting.getQuote().getQuoteNumber()
                        : null,
                totalAmount,
                paidAmount,
                pendingAmount,
                remainingAmount,
                paidPercentage,
                status
        );
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = new Payment();

        mapRequestToEntity(request, payment);

        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        return mapToResponse(savedPayment);
    }

    public PaymentResponse updatePayment(
            Long id,
            PaymentRequest request
    ) {
        Payment payment = findPaymentById(id);

        mapRequestToEntity(request, payment);

        Payment updatedPayment = paymentRepository.saveAndFlush(payment);

        return mapToResponse(updatedPayment);
    }

    public void deletePayment(Long id) {
        Payment payment = findPaymentById(id);
        paymentRepository.delete(payment);
    }

    private BigDecimal calculatePaidPercentage(
            BigDecimal totalAmount,
            BigDecimal paidAmount
    ) {
        if (totalAmount.compareTo(ZERO) <= 0) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return paidAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(totalAmount, 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100));
    }

    private FinancialStatus calculateFinancialStatus(
            boolean hasQuote,
            BigDecimal totalAmount,
            BigDecimal paidAmount
    ) {
        if (!hasQuote) {
            return FinancialStatus.NO_QUOTE;
        }

        if (paidAmount.compareTo(totalAmount) > 0) {
            return FinancialStatus.OVERPAID;
        }

        if (paidAmount.compareTo(totalAmount) == 0) {
            return FinancialStatus.PAID;
        }

        if (paidAmount.compareTo(ZERO) > 0) {
            return FinancialStatus.PARTIALLY_PAID;
        }

        return FinancialStatus.UNPAID;
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pagamento non trovato con ID: " + id
                        )
                );
    }

    private Shooting findShootingById(Long id) {
        return shootingRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shooting non trovato con ID: " + id
                        )
                );
    }

    private void mapRequestToEntity(
            PaymentRequest request,
            Payment payment
    ) {
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setMethod(request.getMethod());
        payment.setType(request.getType());
        payment.setStatus(request.getStatus());
        payment.setNotes(request.getNotes());
        payment.setShooting(findShootingById(request.getShootingId()));
    }

    private PaymentResponse mapToResponse(Payment payment) {
        Shooting shooting = payment.getShooting();

        return new PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getMethod(),
                payment.getType(),
                payment.getStatus(),
                payment.getNotes(),
                shooting.getId(),
                shooting.getTitle(),
                payment.getCreatedAt()
        );
    }
}
