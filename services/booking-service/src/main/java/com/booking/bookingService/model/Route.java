package com.booking.bookingService.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "route")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private Operator operator;

    private String origin;
    private String destination;
    private int distanceKm;
    private int estimatedMinutes;
}