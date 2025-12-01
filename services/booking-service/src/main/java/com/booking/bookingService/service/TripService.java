package com.booking.bookingService.service;

import com.booking.bookingService.dto.*;
import com.booking.bookingService.model.*;
import com.booking.bookingService.repository.*;
import com.booking.bookingService.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final SeatStatusRepository seatStatusRepository;

    public Page<TripSearchResponse> searchTrips(TripSearchRequest request) {
        // 1. Setup Pagination
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit());

        Specification<Trip> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Joins
            var routeJoin = root.join("route");
            var busJoin = root.join("bus"); 
            var operatorJoin = root.join("operator"); 

            // 1. Origin
            if (request.getOrigin() != null && !request.getOrigin().isEmpty()) {
                String originPattern = "%" + request.getOrigin().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(routeJoin.get("origin")), 
                    originPattern
                ));
            }

            // 2. Destination
            if (request.getDestination() != null && !request.getDestination().isEmpty()) {
                String destPattern = "%" + request.getDestination().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(routeJoin.get("destination")), 
                    destPattern
                ));
            }

            // 3. Date (Specific Day)
            if (request.getDate() != null) {
                LocalDateTime startOfDay = request.getDate().atStartOfDay();
                LocalDateTime endOfDay = request.getDate().atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("departureTime"), startOfDay, endOfDay));
            }

            // 4. Passenger Capacity (using new availableSeats field)
            if (request.getPassengers() != null && request.getPassengers() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("availableSeats"), 
                    request.getPassengers()
                ));
            }

            // 5. Bus Type (using new type field on Bus)
            if (request.getBusType() != null && !request.getBusType().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(busJoin.get("type")), 
                    request.getBusType().toLowerCase()
                ));
            }

            // 6. Price Range (using new price field)
            if (request.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), 
                    request.getMinPrice()
                ));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), 
                    request.getMaxPrice()
                ));
            }

            // 7. Operator Filter (using new direct operator relationship)
            if (request.getOperatorId() != null) {
                predicates.add(criteriaBuilder.equal(
                    operatorJoin.get("id"), 
                    request.getOperatorId()
                ));
            }

            // 8. Departure Time Slots
            if (request.getDepartureTime() != null && request.getDate() != null) {
                LocalDateTime baseDate = request.getDate().atStartOfDay();
                String timeSlot = request.getDepartureTime().toLowerCase();
                
                LocalDateTime startTime = null;
                LocalDateTime endTime = null;

                switch (timeSlot) {
                    case "morning": // 06:00 - 11:59
                        startTime = baseDate.withHour(6);
                        endTime = baseDate.withHour(11).withMinute(59);
                        break;
                    case "afternoon": // 12:00 - 17:59
                        startTime = baseDate.withHour(12);
                        endTime = baseDate.withHour(17).withMinute(59);
                        break;
                    case "evening": // 18:00 - 20:59
                        startTime = baseDate.withHour(18);
                        endTime = baseDate.withHour(20).withMinute(59);
                        break;
                    case "night": // 21:00 - 23:59
                        startTime = baseDate.withHour(21);
                        endTime = baseDate.withHour(23).withMinute(59);
                        break;
                }

                if (startTime != null && endTime != null) {
                    predicates.add(criteriaBuilder.between(
                        root.get("departureTime"), 
                        startTime, 
                        endTime
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return tripRepository.findAll(spec, pageable).map(this::mapToTripResponse);
    }
    
    // --- Get Detail ---
    public TripSearchResponse getTripById(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        return mapToTripResponse(trip);
    }

    // --- Get Seat Map ---
    public SeatMapResponse getSeatMap(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // 1. Get all physical seats on the bus
        List<Seat> physicalSeats = seatRepository.findByBusId(trip.getBus().getId());

        // 2. Get current status of seats for this SPECIFIC trip
        List<SeatStatus> seatStatuses = seatStatusRepository.findByTripId(tripId);

        // Map status by Seat ID for easy lookup
        Map<UUID, SeatStatus.SeatState> statusMap = seatStatuses.stream()
                .collect(Collectors.toMap(
                        s -> s.getSeat().getId(),
                        SeatStatus::getState
                ));

        // 3. Merge data
        List<SeatMapResponse.SeatDto> seatDtos = physicalSeats.stream().map(seat -> {
            String status = statusMap.getOrDefault(seat.getId(), SeatStatus.SeatState.AVAILABLE).name().toLowerCase();
            
            return SeatMapResponse.SeatDto.builder()
                    .seatId(seat.getId().toString())
                    .seatCode(seat.getSeatCode())
                    .type(seat.getSeatType())
                    .deck(seat.getDeckNumber())
                    // FIX: Use 'price' instead of 'basePrice'
                    .price(trip.getPrice()) 
                    .status(status)
                    .build();
        }).collect(Collectors.toList());

        return SeatMapResponse.builder()
                .tripId(tripId)
                .seats(seatDtos)
                .build();
    }

    // --- Helper Mapper ---
    private TripSearchResponse mapToTripResponse(Trip trip) {
        return TripSearchResponse.builder()
                .tripId(trip.getId())
                .status(trip.getStatus().name())
                .route(TripSearchResponse.RouteDto.builder()
                        .origin(trip.getRoute().getOrigin())
                        .destination(trip.getRoute().getDestination())
                        .durationMinutes(trip.getRoute().getEstimatedMinutes())
                        .build())
                .operator(TripSearchResponse.OperatorDto.builder()
                        .name(trip.getOperator().getName())
                        .build())
                .bus(TripSearchResponse.BusDto.builder()
                        .model(trip.getBus().getModel())
                        .type(trip.getBus().getType()) 
                        .build())
                .schedule(TripSearchResponse.ScheduleDto.builder()
                        .departureTime(trip.getDepartureTime())
                        .arrivalTime(trip.getArrivalTime())
                        .build())
                .pricing(TripSearchResponse.PricingDto.builder()
                        .basePrice(trip.getPrice()) 
                        .currency("VND")
                        .build())
                .availability(TripSearchResponse.AvailabilityDto.builder()
                        .totalSeats(trip.getBus().getSeatCapacity())
                        .availableSeats(trip.getAvailableSeats())
                        .build())
                .build();
    }
}