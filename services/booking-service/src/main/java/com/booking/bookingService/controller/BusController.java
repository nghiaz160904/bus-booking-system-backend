package com.booking.bookingService.controller;

import com.booking.bookingService.dto.BusRequest;
import com.booking.bookingService.dto.BusResponse;
import com.booking.bookingService.dto.SeatDefinition;
import com.booking.bookingService.model.Bus;
import com.booking.bookingService.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/buses")
@RequiredArgsConstructor
public class BusController {
    private final BusService busService;

    @PostMapping
    public ResponseEntity<?> createBus(@Valid @RequestBody BusRequest request) {
        return new ResponseEntity<>(busService.createBus(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BusResponse>> getAllBuses() {
        List<Bus> buses = busService.getAllBuses();
        
        List<BusResponse> response = buses.stream().map(bus -> 
            BusResponse.builder()
                .id(bus.getId())
                .operatorId(bus.getOperator().getId())
                .operatorName(bus.getOperator().getName())
                .plateNumber(bus.getPlateNumber())
                .model(bus.getModel())
                .type(bus.getType())
                .seatCapacity(bus.getSeatCapacity())
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBus(@PathVariable UUID id) {
        return ResponseEntity.ok(busService.getBus(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBus(@PathVariable UUID id, @Valid @RequestBody BusRequest request) {
        return ResponseEntity.ok(busService.updateBus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBus(@PathVariable UUID id) {
        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    // --- Seat Map Management Endpoints ---
    
    @PostMapping("/{id}/seats/custom")
    public ResponseEntity<?> saveCustomSeatMap(
            @PathVariable UUID id, 
            @RequestBody List<SeatDefinition> seatDefinitions
    ) {
        return ResponseEntity.ok(busService.saveCustomSeatMap(id, seatDefinitions));
    }
}