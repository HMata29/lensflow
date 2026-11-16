
package com.lensflow.api.client.service;

import com.lensflow.api.client.dto.ClientRequest;
import com.lensflow.api.client.dto.ClientResponse;
import com.lensflow.api.client.entity.Client;
import com.lensflow.api.client.repository.ClientRepository;
import com.lensflow.api.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = findClientById(id);
        return mapToResponse(client);
    }

    public ClientResponse createClient(ClientRequest request) {
        Client client = new Client();

        mapRequestToEntity(request, client);

        Client savedClient = clientRepository.save(client);

        return mapToResponse(savedClient);
    }

    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = findClientById(id);

        mapRequestToEntity(request, client);

        Client updatedClient = clientRepository.saveAndFlush(client);

        return mapToResponse(updatedClient);
    }

    public void deleteClient(Long id) {
        Client client = findClientById(id);
        clientRepository.delete(client);
    }

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente non trovato con ID: " + id
                        )
                );
    }

    private void mapRequestToEntity(ClientRequest request, Client client) {
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompany(request.getCompany());
        client.setNotes(request.getNotes());
    }

    private ClientResponse mapToResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getCompany(),
                client.getNotes(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
