package com.booking.bookingService.repository;

import com.booking.bookingService.model.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import Added
import java.util.UUID;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, UUID> {
    List<SeatType> findByOperatorId(UUID operatorId);
    
    // FIX: Add this method to lookup types by name
    Optional<SeatType> findByNameAndOperatorId(String name, UUID operatorId);
}