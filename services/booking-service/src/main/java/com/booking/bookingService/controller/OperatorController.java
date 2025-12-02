package com.booking.bookingService.controller;

import com.booking.bookingService.dto.OperatorRequest;
import com.booking.bookingService.service.OperatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/operators")
@RequiredArgsConstructor
public class OperatorController {
    private final OperatorService operatorService;

    @PostMapping
    public ResponseEntity<?> createOperator(@Valid @RequestBody OperatorRequest request) {
        return new ResponseEntity<>(operatorService.createOperator(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getAllOperators() {
        return ResponseEntity.ok(operatorService.getAllOperators());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOperator(@PathVariable UUID id) {
        return ResponseEntity.ok(operatorService.getOperator(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOperator(@PathVariable UUID id, @Valid @RequestBody OperatorRequest request) {
        return ResponseEntity.ok(operatorService.updateOperator(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOperator(@PathVariable UUID id) {
        operatorService.deleteOperator(id);
        return ResponseEntity.noContent().build();
    }
}