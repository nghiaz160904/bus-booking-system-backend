package com.booking.bookingService.repository;

import com.booking.bookingService.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {
}