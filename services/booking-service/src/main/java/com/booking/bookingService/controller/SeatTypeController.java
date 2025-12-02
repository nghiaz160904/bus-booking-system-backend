package com.booking.bookingService.controller;

import com.booking.bookingService.dto.SeatTypeRequest;
import com.booking.bookingService.service.SeatTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/seat-types")
@RequiredArgsConstructor
public class SeatTypeController {

    private final SeatTypeService seatTypeService;

    @PostMapping
    public ResponseEntity<?> createSeatType(@Valid @RequestBody SeatTypeRequest request) {
        return new ResponseEntity<>(seatTypeService.createSeatType(request), HttpStatus.CREATED);
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<?> getByOperator(@PathVariable UUID operatorId) {
        return ResponseEntity.ok(seatTypeService.getSeatTypesByOperator(operatorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeatType(@PathVariable UUID id) {
        seatTypeService.deleteSeatType(id);
        return ResponseEntity.noContent().build();
    }
}