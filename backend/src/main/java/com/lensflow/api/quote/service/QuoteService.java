
package com.lensflow.api.quote.service;

import com.lensflow.api.client.entity.Client;
import com.lensflow.api.client.repository.ClientRepository;
import com.lensflow.api.exception.ResourceNotFoundException;
import com.lensflow.api.quote.dto.QuoteItemRequest;
import com.lensflow.api.quote.dto.QuoteItemResponse;
import com.lensflow.api.quote.dto.QuoteRequest;
import com.lensflow.api.quote.dto.QuoteResponse;
import com.lensflow.api.quote.entity.Quote;
import com.lensflow.api.quote.entity.QuoteItem;
import com.lensflow.api.quote.repository.QuoteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;

    public QuoteService(
            QuoteRepository quoteRepository,
            ClientRepository clientRepository
    ) {
        this.quoteRepository = quoteRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> getAllQuotes() {
        return quoteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(Long id) {
        return mapToResponse(findQuoteById(id));
    }

    public QuoteResponse createQuote(QuoteRequest request) {
        validateDates(request);

        Quote quote = new Quote();

        quote.setQuoteNumber(generateQuoteNumber());
        mapRequestToEntity(request, quote);

        Quote savedQuote = quoteRepository.saveAndFlush(quote);

        return mapToResponse(savedQuote);
    }

    public QuoteResponse updateQuote(Long id, QuoteRequest request) {
        validateDates(request);

        Quote quote = findQuoteById(id);

        mapRequestToEntity(request, quote);

        Quote updatedQuote = quoteRepository.saveAndFlush(quote);

        return mapToResponse(updatedQuote);
    }

    public void deleteQuote(Long id) {
        Quote quote = findQuoteById(id);
        quoteRepository.delete(quote);
    }

    private Quote findQuoteById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Preventivo non trovato con ID: " + id
                        )
                );
    }

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente non trovato con ID: " + id
                        )
                );
    }

    private void validateDates(QuoteRequest request) {
        if (request.getExpiryDate().isBefore(request.getIssueDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La scadenza non può essere precedente alla data di emissione"
            );
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Il preventivo deve contenere almeno una voce"
            );
        }

        for (QuoteItemRequest item : request.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La quantità deve essere almeno 1"
                );
            }

            if (item.getUnitPrice() == null
                    || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Il prezzo unitario non può essere negativo"
                );
            }
        }
    }

    private String generateQuoteNumber() {
        return "PF-" + Year.now().getValue() + "-"
                + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
    }

    private void mapRequestToEntity(
            QuoteRequest request,
            Quote quote
    ) {
        quote.setTitle(request.getTitle().trim());
        quote.setNotes(request.getNotes());
        quote.setIssueDate(request.getIssueDate());
        quote.setExpiryDate(request.getExpiryDate());
        quote.setStatus(request.getStatus());
        quote.setClient(findClientById(request.getClientId()));

        List<QuoteItem> items = request.getItems()
                .stream()
                .map(this::mapItemToEntity)
                .toList();

        quote.setItems(items);

        BigDecimal total = items.stream()
                .map(QuoteItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        quote.setTotalAmount(total);
    }

    private QuoteItem mapItemToEntity(QuoteItemRequest request) {
        QuoteItem item = new QuoteItem();

        item.setDescription(request.getDescription().trim());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());

        return item;
    }

    private QuoteResponse mapToResponse(Quote quote) {
        Client client = quote.getClient();

        List<QuoteItemResponse> itemResponses = quote.getItems()
                .stream()
                .map(item -> new QuoteItemResponse(
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotal()
                ))
                .toList();

        return new QuoteResponse(
                quote.getId(),
                quote.getQuoteNumber(),
                quote.getTitle(),
                quote.getNotes(),
                quote.getIssueDate(),
                quote.getExpiryDate(),
                quote.getStatus(),
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                itemResponses,
                quote.getTotalAmount(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }
}
