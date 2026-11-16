package com.lensflow.api.payment.service;

import com.lensflow.api.exception.ResourceNotFoundException;
import com.lensflow.api.payment.dto.PaymentRequest;
import com.lensflow.api.payment.dto.PaymentResponse;
import com.lensflow.api.payment.entity.Payment;
import com.lensflow.api.payment.entity.PaymentStatus;
import com.lensflow.api.payment.repository.PaymentRepository;
import com.lensflow.api.quote.entity.Quote;
import com.lensflow.api.quote.entity.QuoteStatus;
import com.lensflow.api.shooting.entity.Shooting;
import com.lensflow.api.shooting.repository.ShootingRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PaymentService {

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

    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = new Payment();

        mapRequestToEntity(request, payment);

        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        return mapToResponse(savedPayment);
    }

    public PaymentResponse updatePayment(Long id, PaymentRequest request) {
        Payment payment = findPaymentById(id);

        mapRequestToEntity(request, payment);

        Payment updatedPayment = paymentRepository.saveAndFlush(payment);

        return mapToResponse(updatedPayment);
    }

    public void deletePayment(Long id) {
        Payment payment = findPaymentById(id);
        paymentRepository.delete(payment);
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
        Shooting shooting = findShootingById(request.getShootingId());

        Quote quote = shooting.getQuote();

        if (quote == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lo shooting non ha un preventivo associato"
            );
        }

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Il preventivo associato non è accettato"
            );
        }

        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'importo deve essere maggiore di zero"
            );
        }

        validateReceivedAmount(
                shooting,
                request,
                payment.getId(),
                quote.getTotalAmount()
        );

        payment.setShooting(shooting);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setMethod(request.getMethod());
        payment.setType(request.getType());
        payment.setStatus(request.getStatus());
        payment.setNotes(request.getNotes());
    }

    private void validateReceivedAmount(
            Shooting shooting,
            PaymentRequest request,
            Long currentPaymentId,
            BigDecimal quoteTotal
    ) {
        if (request.getStatus() != PaymentStatus.RECEIVED) {
            return;
        }

        BigDecimal alreadyReceived = paymentRepository
                .findByShooting_Id(shooting.getId())
                .stream()
                .filter(payment ->
                        payment.getStatus() == PaymentStatus.RECEIVED
                )
                .filter(payment ->
                        currentPaymentId == null
                                || !payment.getId().equals(currentPaymentId)
                )
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = alreadyReceived.add(request.getAmount());

        if (newTotal.compareTo(quoteTotal) > 0) {
            BigDecimal remaining = quoteTotal.subtract(alreadyReceived);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Importo superiore al residuo disponibile: "
                            + remaining
            );
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        Shooting shooting = payment.getShooting();
        Quote quote = shooting.getQuote();

        return new PaymentResponse(
                payment.getId(),
                shooting.getId(),
                shooting.getTitle(),
                shooting.getClient().getFirstName(),
                shooting.getClient().getLastName(),
                quote.getTotalAmount(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getMethod(),
                payment.getType(),
                payment.getStatus(),
                payment.getNotes()
        );
    }
}