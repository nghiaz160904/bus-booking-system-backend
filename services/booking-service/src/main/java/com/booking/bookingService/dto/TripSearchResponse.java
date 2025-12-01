package com.booking.bookingService.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TripSearchResponse {
    private UUID tripId;
    private RouteDto route;
    private OperatorDto operator;
    private BusDto bus;
    private ScheduleDto schedule;
    private PricingDto pricing;
    private AvailabilityDto availability;
    private String status;

    @Data @Builder
    public static class RouteDto {
        private String origin;
        private String destination;
        private int durationMinutes;
    }

    @Data @Builder
    public static class OperatorDto {
        private String name;
    }

    @Data @Builder
    public static class BusDto {
        private String model;
        private String type;
    }

    @Data @Builder
    public static class ScheduleDto {
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
    }

    @Data @Builder
    public static class PricingDto {
        private BigDecimal basePrice;
        private String currency;
    }

    @Data @Builder
    public static class AvailabilityDto {
        private int totalSeats;
        private int availableSeats;
    }
}