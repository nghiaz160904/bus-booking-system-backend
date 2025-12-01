package com.booking.bookingService.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "seat")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    private String seatCode; // e.g., A1, B2
    private String seatType; // standard, sleeper
    private int deckNumber; // 1 or 2
}