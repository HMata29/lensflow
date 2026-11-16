
package com.lensflow.api.payment.controller;

import com.lensflow.api.payment.dto.FinancialSummaryResponse;
import com.lensflow.api.payment.dto.PaymentRequest;
import com.lensflow.api.payment.dto.PaymentResponse;
import com.lensflow.api.payment.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/shooting/{shootingId}")
    public List<PaymentResponse> getPaymentsByShooting(
            @PathVariable Long shootingId
    ) {
        return paymentService.getPaymentsByShooting(shootingId);
    }

    @GetMapping("/shooting/{shootingId}/summary")
    public FinancialSummaryResponse getFinancialSummary(
            @PathVariable Long shootingId
    ) {
        return paymentService.getFinancialSummary(shootingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        return paymentService.createPayment(request);
    }

    @PutMapping("/{id}")
    public PaymentResponse updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request
    ) {
        return paymentService.updatePayment(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }
}
