package com.booking.bookingService.service;

import com.booking.bookingService.dto.SeatTypeRequest;
import com.booking.bookingService.exception.ResourceNotFoundException;
import com.booking.bookingService.model.Operator;
import com.booking.bookingService.model.SeatType;
import com.booking.bookingService.repository.OperatorRepository;
import com.booking.bookingService.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatTypeService {

    private final SeatTypeRepository seatTypeRepository;
    private final OperatorRepository operatorRepository;

    public SeatType createSeatType(SeatTypeRequest request) {
        Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        SeatType seatType = SeatType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .operator(operator)
                .build();

        return seatTypeRepository.save(seatType);
    }

    public List<SeatType> getSeatTypesByOperator(UUID operatorId) {
        return seatTypeRepository.findByOperatorId(operatorId);
    }

    public SeatType getSeatType(UUID id) {
        return seatTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat Type not found"));
    }

    public void deleteSeatType(UUID id) {
        seatTypeRepository.deleteById(id);
    }
}