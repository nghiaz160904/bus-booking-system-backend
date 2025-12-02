package com.booking.bookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSearchRequest {
    // Required fields
    private String origin;
    private String destination;
    private LocalDate date;

    // Optional fields with defaults
    @Builder.Default
    private Integer passengers = 1;
    
    private String busType;       // standard | limousine | sleeper
    private String departureTime; // morning | afternoon | evening | night
    
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private String sort;
    
    private UUID operatorId;

    // Pagination
    @Builder.Default
    private Integer page = 1;
    
    @Builder.Default
    private Integer limit = 20;

    
}