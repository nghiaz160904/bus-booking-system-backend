package com.booking.bookingService.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SeatMapResponse {
    private UUID tripId;
    private List<SeatDto> seats;

    @Data @Builder
    public static class SeatDto {
        private String seatId;
        private String seatCode;
        private String status; // available, locked, occupied
        private BigDecimal price;
        private String type;
        private int deck;
    }
}