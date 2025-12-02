package com.booking.bookingService.service;

import com.booking.bookingService.dto.BusRequest;
import com.booking.bookingService.dto.SeatDefinition;
import com.booking.bookingService.exception.ResourceNotFoundException;
import com.booking.bookingService.model.Bus;
import com.booking.bookingService.model.Operator;
import com.booking.bookingService.model.Seat;
import com.booking.bookingService.model.SeatType;
import com.booking.bookingService.repository.BusRepository;
import com.booking.bookingService.repository.OperatorRepository;
import com.booking.bookingService.repository.SeatRepository;
import com.booking.bookingService.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {
    private final BusRepository busRepository;
    private final OperatorRepository operatorRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    public Bus createBus(BusRequest request) {
        Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        Bus bus = Bus.builder()
                .operator(operator)
                .plateNumber(request.getPlateNumber())
                .model(request.getModel())
                .type(request.getType())
                .seatCapacity(request.getSeatCapacity())
                .build();
        return busRepository.save(bus);
    }

    public Bus getBus(UUID id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
    }
    
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public Bus updateBus(UUID id, BusRequest request) {
        Bus bus = getBus(id);
        if (!bus.getOperator().getId().equals(request.getOperatorId())) {
            Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));
            bus.setOperator(operator);
        }
        bus.setPlateNumber(request.getPlateNumber());
        bus.setModel(request.getModel());
        bus.setType(request.getType());
        bus.setSeatCapacity(request.getSeatCapacity());
        return busRepository.save(bus);
    }

    public void deleteBus(UUID id) {
        if (!busRepository.existsById(id)) throw new ResourceNotFoundException("Bus not found");
        busRepository.deleteById(id);
    }

    /**
     * SAVES A FULLY CUSTOM SEAT MAP
     */
    @Transactional
    public List<Seat> saveCustomSeatMap(UUID busId, List<SeatDefinition> seatDefinitions) {
        Bus bus = getBus(busId);

        // Clear existing seats
        List<Seat> existingSeats = seatRepository.findByBusId(busId);
        seatRepository.deleteAll(existingSeats);

        // Map DTOs to Entities
        List<Seat> newSeats = seatDefinitions.stream().map(def -> {
            
            // FIX: Determine the Type Name (use Bus default if SeatDefinition is null)
            String typeName = def.getType() != null ? def.getType() : bus.getType();

            // FIX: Find the actual SeatType Entity
            // If the type doesn't exist for this operator, this will throw an exception.
            // In a real app, you might want to create it on the fly or throw a clearer error.
            SeatType seatTypeEntity = seatTypeRepository
                    .findByNameAndOperatorId(typeName, bus.getOperator().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "SeatType not found: " + typeName + " for Operator: " + bus.getOperator().getName()));

            return Seat.builder()
                .bus(bus)
                .seatCode(def.getSeatCode())
                .gridRow(def.getRow())
                .gridCol(def.getCol())
                .deckNumber(def.getDeck())
                .seatType(seatTypeEntity) // FIX: Set the Entity, not Enum
                .build();
        }).collect(Collectors.toList());

        bus.setSeatCapacity(newSeats.size());
        busRepository.save(bus);

        return seatRepository.saveAll(newSeats);
    }
}