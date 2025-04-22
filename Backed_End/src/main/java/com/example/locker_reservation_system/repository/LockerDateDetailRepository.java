package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.LockerDateDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface LockerDateDetailRepository extends JpaRepository<LockerDateDetail, Long> {
    List<LockerDateDetail> findByLockerLockerIdAndDate(Long lockerId, Date date);
}