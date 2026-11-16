
package com.lensflow.api.shooting.service;

import com.lensflow.api.client.entity.Client;
import com.lensflow.api.client.repository.ClientRepository;
import com.lensflow.api.exception.ResourceNotFoundException;
import com.lensflow.api.quote.entity.Quote;
import com.lensflow.api.quote.entity.QuoteStatus;
import com.lensflow.api.quote.repository.QuoteRepository;
import com.lensflow.api.shooting.dto.ShootingRequest;
import com.lensflow.api.shooting.dto.ShootingResponse;
import com.lensflow.api.shooting.entity.Shooting;
import com.lensflow.api.shooting.repository.ShootingRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ShootingService {

    private final ShootingRepository shootingRepository;
    private final ClientRepository clientRepository;
    private final QuoteRepository quoteRepository;

    public ShootingService(
            ShootingRepository shootingRepository,
            ClientRepository clientRepository,
            QuoteRepository quoteRepository
    ) {
        this.shootingRepository = shootingRepository;
        this.clientRepository = clientRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional(readOnly = true)
    public List<ShootingResponse> getAllShootings() {
        return shootingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShootingResponse getShootingById(Long id) {
        return mapToResponse(findShootingById(id));
    }

    public ShootingResponse createShooting(ShootingRequest request) {
        validateTimeRange(request);

        Shooting shooting = new Shooting();

        mapRequestToEntity(request, shooting);

        Shooting savedShooting = shootingRepository.saveAndFlush(shooting);

        return mapToResponse(savedShooting);
    }

    public ShootingResponse updateShooting(
            Long id,
            ShootingRequest request
    ) {
        validateTimeRange(request);

        Shooting shooting = findShootingById(id);

        mapRequestToEntity(request, shooting);

        Shooting updatedShooting = shootingRepository.saveAndFlush(shooting);

        return mapToResponse(updatedShooting);
    }

    public void deleteShooting(Long id) {
        Shooting shooting = findShootingById(id);
        shootingRepository.delete(shooting);
    }

    private Shooting findShootingById(Long id) {
        return shootingRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shooting non trovato con ID: " + id
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

    private Quote findQuoteById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Preventivo non trovato con ID: " + id
                        )
                );
    }

    private void validateTimeRange(ShootingRequest request) {
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La data di fine deve essere successiva alla data di inizio"
            );
        }
    }

    private Quote findAndValidateQuote(Long quoteId, Client client, Long shootingId) {
        if (quoteId == null) {
            return null;
        }

        Quote quote = findQuoteById(quoteId);

        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "È possibile associare solamente un preventivo accettato"
            );
        }

        if (!quote.getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Il preventivo deve appartenere allo stesso cliente dello shooting"
            );
        }

        shootingRepository.findByQuoteId(quoteId)
                .filter(existingShooting ->
                        !existingShooting.getId().equals(shootingId)
                )
                .ifPresent(existingShooting -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Il preventivo è già associato a un altro shooting"
                    );
                });

        return quote;
    }

    private void mapRequestToEntity(
            ShootingRequest request,
            Shooting shooting
    ) {
        Client client = findClientById(request.getClientId());

        shooting.setTitle(request.getTitle());
        shooting.setCategory(request.getCategory());
        shooting.setStartAt(request.getStartAt());
        shooting.setEndAt(request.getEndAt());
        shooting.setLocation(request.getLocation());
        shooting.setStatus(request.getStatus());
        shooting.setNotes(request.getNotes());
        shooting.setClient(client);

        shooting.setQuote(
                findAndValidateQuote(
                        request.getQuoteId(),
                        client,
                        shooting.getId()
                )
        );
    }

    private ShootingResponse mapToResponse(Shooting shooting) {
        Client client = shooting.getClient();
        Quote quote = shooting.getQuote();

        return new ShootingResponse(
                shooting.getId(),
                shooting.getTitle(),
                shooting.getCategory(),
                shooting.getStartAt(),
                shooting.getEndAt(),
                shooting.getLocation(),
                shooting.getStatus(),
                shooting.getNotes(),
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                quote != null ? quote.getId() : null,
                quote != null ? quote.getQuoteNumber() : null,
                quote != null ? quote.getTotalAmount() : null
        );
    }
}
