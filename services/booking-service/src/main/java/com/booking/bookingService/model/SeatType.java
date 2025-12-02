package com.booking.bookingService.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "seat_type")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name; // e.g., "VIP", "Standard", "Sleeper"

    private String description; // e.g., "Leather seats with massage"

    @Column(nullable = false)
    private BigDecimal price; // The default price or surcharge for this type

    // Link to Operator so each company owns their own types
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    @JsonIgnore
    private Operator operator;
}