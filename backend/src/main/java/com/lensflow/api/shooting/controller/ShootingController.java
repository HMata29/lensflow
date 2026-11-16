
package com.lensflow.api.shooting.controller;

import com.lensflow.api.shooting.dto.ShootingRequest;
import com.lensflow.api.shooting.dto.ShootingResponse;
import com.lensflow.api.shooting.service.ShootingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shootings")
public class ShootingController {

    private final ShootingService shootingService;

    public ShootingController(ShootingService shootingService) {
        this.shootingService = shootingService;
    }

    @GetMapping
    public ResponseEntity<List<ShootingResponse>> getAllShootings() {
        return ResponseEntity.ok(shootingService.getAllShootings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShootingResponse> getShootingById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                shootingService.getShootingById(id)
        );
    }

    @PostMapping
    public ResponseEntity<ShootingResponse> createShooting(
            @Valid @RequestBody ShootingRequest request
    ) {
        ShootingResponse response =
                shootingService.createShooting(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShootingResponse> updateShooting(
            @PathVariable Long id,
            @Valid @RequestBody ShootingRequest request
    ) {
        return ResponseEntity.ok(
                shootingService.updateShooting(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShooting(
            @PathVariable Long id
    ) {
        shootingService.deleteShooting(id);

        return ResponseEntity.noContent().build();
    }
}
