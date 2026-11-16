
package com.lensflow.api.shooting.service;

import com.lensflow.api.client.entity.Client;
import com.lensflow.api.client.repository.ClientRepository;
import com.lensflow.api.exception.ResourceNotFoundException;
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

    public ShootingService(
            ShootingRepository shootingRepository,
            ClientRepository clientRepository
    ) {
        this.shootingRepository = shootingRepository;
        this.clientRepository = clientRepository;
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
        Shooting shooting = findShootingById(id);
        return mapToResponse(shooting);
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

    private void validateTimeRange(ShootingRequest request) {
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La data di fine deve essere successiva alla data di inizio"
            );
        }
    }

    private void mapRequestToEntity(
            ShootingRequest request,
            Shooting shooting
    ) {
        shooting.setTitle(request.getTitle());
        shooting.setCategory(request.getCategory());
        shooting.setStartAt(request.getStartAt());
        shooting.setEndAt(request.getEndAt());
        shooting.setLocation(request.getLocation());
        shooting.setStatus(request.getStatus());
        shooting.setNotes(request.getNotes());
        shooting.setClient(findClientById(request.getClientId()));
    }

    private ShootingResponse mapToResponse(Shooting shooting) {
        Client client = shooting.getClient();

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
                client.getLastName()
        );
    }
}
