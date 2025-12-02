package com.booking.bookingService.service;

import com.booking.bookingService.dto.RouteRequest;
import com.booking.bookingService.exception.ResourceNotFoundException;
import com.booking.bookingService.model.Operator;
import com.booking.bookingService.model.Route;
import com.booking.bookingService.repository.OperatorRepository;
import com.booking.bookingService.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final OperatorRepository operatorRepository;

    public Route createRoute(RouteRequest request) {
        Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        Route route = Route.builder()
                .operator(operator)
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .distanceKm(request.getDistanceKm())
                .estimatedMinutes(request.getEstimatedMinutes())
                .build();
        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRoute(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
    }

    public Route updateRoute(UUID id, RouteRequest request) {
        Route route = getRoute(id);
        // If operator changes
        if (!route.getOperator().getId().equals(request.getOperatorId())) {
             Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));
             route.setOperator(operator);
        }
        route.setOrigin(request.getOrigin());
        route.setDestination(request.getDestination());
        route.setDistanceKm(request.getDistanceKm());
        route.setEstimatedMinutes(request.getEstimatedMinutes());
        return routeRepository.save(route);
    }

    public void deleteRoute(UUID id) {
        if (!routeRepository.existsById(id)) throw new ResourceNotFoundException("Route not found");
        routeRepository.deleteById(id);
    }
}