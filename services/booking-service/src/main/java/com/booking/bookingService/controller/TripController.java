package com.booking.bookingService.controller;

import com.booking.bookingService.dto.SeatMapResponse;
import com.booking.bookingService.dto.TripRequest;
import com.booking.bookingService.dto.TripSearchResponse;
import com.booking.bookingService.dto.TripSearchRequest;
import com.booking.bookingService.service.TripService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<?> createTrip(@Valid @RequestBody TripRequest request) {
        // Map to response DTO logic would be here, for now returning entity
        return new ResponseEntity<>(tripService.createTrip(request), HttpStatus.CREATED);
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<?> updateTrip(@PathVariable UUID tripId, @Valid @RequestBody TripRequest request) {
        return ResponseEntity.ok(tripService.updateTrip(tripId, request));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(@PathVariable UUID tripId) {
        tripService.deleteTrip(tripId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTrips(
        @ModelAttribute TripSearchRequest request
    ) {
        Page<TripSearchResponse> result = tripService.searchTrips(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", result.getContent());
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", request.getPage());
        pagination.put("limit", request.getLimit());
        pagination.put("total", result.getTotalElements());
        pagination.put("totalPages", result.getTotalPages());
        
        response.put("pagination", pagination);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<Map<String, Object>> getTripDetail(@PathVariable UUID tripId) {
        TripSearchResponse trip = tripService.getTripById(tripId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", trip
        ));
    }

    @GetMapping("/{tripId}/seats")
    public ResponseEntity<Map<String, Object>> getSeatMap(@PathVariable UUID tripId) {
        SeatMapResponse seatMap = tripService.getSeatMap(tripId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", seatMap
        ));
    }
}