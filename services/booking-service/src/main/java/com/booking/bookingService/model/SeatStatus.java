package com.booking.bookingService.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "seat_status")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Enumerated(EnumType.STRING)
    private SeatState state; // AVAILABLE, LOCKED, BOOKED

    public enum SeatState { AVAILABLE, LOCKED, BOOKED }
}