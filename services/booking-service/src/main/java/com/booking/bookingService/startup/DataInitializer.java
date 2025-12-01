package com.booking.bookingService.startup;

import com.booking.bookingService.model.*;
import com.booking.bookingService.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OperatorRepository operatorRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final SeatStatusRepository seatStatusRepository;
    private final ObjectMapper objectMapper; 

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (operatorRepository.count() > 0) {
            log.info("Data already exists. Skipping initialization.");
            return;
        }

        log.info("Initializing data from JSON file...");

        try (InputStream inputStream = new ClassPathResource("data/initial_data.json").getInputStream()) {
            InitialData data = objectMapper.readValue(inputStream, InitialData.class);

            // Maps to temporarily store created objects for relationship mapping
            Map<String, Operator> createdOperators = new HashMap<>();
            Map<String, Bus> createdBuses = new HashMap<>();
            Map<String, Route> createdRoutes = new HashMap<>();

            // 1. Create Operators
            for (OperatorData opData : data.getOperators()) {
                Operator op = Operator.builder()
                        .name(opData.getName())
                        .contactEmail(opData.getEmail())
                        .contactPhone(opData.getPhone())
                        .build();
                operatorRepository.save(op);
                createdOperators.put(opData.getKey(), op);
            }

            // 2. Create Buses & Seats
            for (BusData busData : data.getBuses()) {
                Operator op = createdOperators.get(busData.getOperatorKey());
                if (op == null) continue;

                Bus bus = Bus.builder()
                        .operator(op)
                        .model(busData.getModel())
                        .plateNumber(busData.getPlateNumber())
                        .seatCapacity(busData.getCapacity())
                        .type(busData.getType()) // Map the new type field
                        .build();
                busRepository.save(bus);
                createdBuses.put(busData.getKey(), bus);

                generateSeatsForBus(bus);
            }

            // 3. Create Routes
            for (RouteData routeData : data.getRoutes()) {
                Operator op = createdOperators.get(routeData.getOperatorKey());
                if (op == null) continue;

                Route route = Route.builder()
                        .operator(op)
                        .origin(routeData.getOrigin())
                        .destination(routeData.getDestination())
                        .distanceKm(routeData.getDistance())
                        .estimatedMinutes(routeData.getMinutes())
                        .build();
                routeRepository.save(route);
                createdRoutes.put(routeData.getKey(), route);
            }

            // 4. Create Trips & SeatStatuses
            for (TripData tripData : data.getTrips()) {
                Route route = createdRoutes.get(tripData.getRouteKey());
                Bus bus = createdBuses.get(tripData.getBusKey());
                
                if (route == null || bus == null) {
                    log.warn("Skipping trip due to missing route or bus: {}", tripData);
                    continue;
                }

                LocalDateTime departure = LocalDateTime.parse(tripData.getDate());
                LocalDateTime arrival = departure.plusMinutes(route.getEstimatedMinutes());

                Trip trip = Trip.builder()
                        .route(route)
                        .bus(bus)
                        .operator(bus.getOperator()) // Set the Operator from the Bus
                        .departureTime(departure)
                        .arrivalTime(arrival)
                        .price(tripData.getPrice()) // Use price instead of basePrice
                        .status(Trip.TripStatus.SCHEDULED)
                        .availableSeats(0) // Will be updated after seat generation
                        .build();
                tripRepository.save(trip);

                // Initialize seat statuses and calculate available seats
                int availableSeats = generateSeatStatusesForTrip(trip, bus);
                
                // Update the trip with the correct available seat count
                trip.setAvailableSeats(availableSeats);
                tripRepository.save(trip);
            }

            log.info("Successfully initialized {} trips from JSON.", data.getTrips().size());
        } catch (Exception e) {
            log.error("Failed to initialize data from JSON", e);
            throw e; 
        }
    }

    private void generateSeatsForBus(Bus bus) {
        List<Seat> seats = new ArrayList<>();
        String[] columns = {"A", "B", "C"};
        int rows = bus.getSeatCapacity() / 3; 

        for (int row = 1; row <= rows; row++) {
            for (String col : columns) {
                if (seats.size() >= bus.getSeatCapacity()) break;

                Seat seat = Seat.builder()
                        .bus(bus)
                        .seatCode(col + row)
                        .seatType("sleeper")
                        .deckNumber(1)
                        .build();
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }

    private int generateSeatStatusesForTrip(Trip trip, Bus bus) {
        List<Seat> seats = seatRepository.findByBusId(bus.getId());
        List<SeatStatus> statuses = new ArrayList<>();
        int availableCount = 0;
        
        for (Seat seat : seats) {
            // Random chance for a seat to be booked
            SeatStatus.SeatState state = SeatStatus.SeatState.AVAILABLE;
            if (Math.random() < 0.1) {
                state = SeatStatus.SeatState.BOOKED; 
            } else {
                availableCount++;
            }
            
            SeatStatus status = SeatStatus.builder()
                    .trip(trip)
                    .seat(seat)
                    .state(state)
                    .build();
            statuses.add(status);
        }
        seatStatusRepository.saveAll(statuses);
        return availableCount;
    }

    // --- Inner DTO Classes for JSON Mapping ---
    @Data
    static class InitialData {
        private List<OperatorData> operators;
        private List<BusData> buses;
        private List<RouteData> routes;
        private List<TripData> trips;
    }

    @Data
    static class OperatorData {
        private String key;
        private String name;
        private String email;
        private String phone;
    }

    @Data
    static class BusData {
        private String key;
        private String operatorKey;
        private String model;
        private String plateNumber;
        private int capacity;
        private String type; // Added type field
    }

    @Data
    static class RouteData {
        private String key;
        private String operatorKey;
        private String origin;
        private String destination;
        private int distance;
        private int minutes;
    }

    @Data
    static class TripData {
        private String routeKey;
        private String busKey;
        private String date; 
        private BigDecimal price;
    }
}