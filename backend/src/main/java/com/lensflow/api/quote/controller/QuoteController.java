
package com.lensflow.api.quote.controller;

import com.lensflow.api.quote.dto.QuoteRequest;
import com.lensflow.api.quote.dto.QuoteResponse;
import com.lensflow.api.quote.service.QuoteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping
    public ResponseEntity<List<QuoteResponse>> getAllQuotes() {
        return ResponseEntity.ok(quoteService.getAllQuotes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getQuoteById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(quoteService.getQuoteById(id));
    }

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(
            @Valid @RequestBody QuoteRequest request
    ) {
        QuoteResponse response = quoteService.createQuote(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponse> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequest request
    ) {
        return ResponseEntity.ok(
                quoteService.updateQuote(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(
            @PathVariable Long id
    ) {
        quoteService.deleteQuote(id);

        return ResponseEntity.noContent().build();
    }
}
