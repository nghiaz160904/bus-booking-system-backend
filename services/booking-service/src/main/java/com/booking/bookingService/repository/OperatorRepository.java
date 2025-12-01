package com.booking.bookingService.repository;

import com.booking.bookingService.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, UUID> {
    // Có thể thêm method tìm kiếm theo tên nếu cần
    List<Operator> findByName(String name);
}