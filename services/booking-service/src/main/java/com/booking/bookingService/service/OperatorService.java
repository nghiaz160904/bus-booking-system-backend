package com.booking.bookingService.service;

import com.booking.bookingService.dto.OperatorRequest;
import com.booking.bookingService.exception.ResourceNotFoundException;
import com.booking.bookingService.model.Operator;
import com.booking.bookingService.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperatorService {
    private final OperatorRepository operatorRepository;

    public Operator createOperator(OperatorRequest request) {
        Operator operator = Operator.builder()
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .build();
        return operatorRepository.save(operator);
    }

    public List<Operator> getAllOperators() {
        return operatorRepository.findAll();
    }

    public Operator getOperator(UUID id) {
        return operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));
    }

    public Operator updateOperator(UUID id, OperatorRequest request) {
        Operator operator = getOperator(id);
        operator.setName(request.getName());
        operator.setContactEmail(request.getContactEmail());
        operator.setContactPhone(request.getContactPhone());
        return operatorRepository.save(operator);
    }

    public void deleteOperator(UUID id) {
        if (!operatorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Operator not found");
        }
        operatorRepository.deleteById(id);
    }
}