package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.LockerDateDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LockerDateDetailRepository extends JpaRepository<LockerDateDetail, Long> {

    List<LockerDateDetail> findByLockerLockerIdAndDateBetween(
            long lockerId, LocalDate startDate, LocalDate endDate);

    Optional<LockerDateDetail> findByLockerLockerIdAndDate(long lockerId, LocalDate date);
}
