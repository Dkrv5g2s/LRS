package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.LockerDateDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LockerDateDetailRepository extends JpaRepository<LockerDateDetail, Long> {
    @Query("SELECT ldd FROM LockerDateDetail ldd WHERE ldd.locker.lockerId = :lockerId AND ldd.date BETWEEN :startDate AND :endDate")
    List<LockerDateDetail> findByLockerIdAndDateBetween(@Param("lockerId") Long lockerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    Optional<LockerDateDetail> findByLockerAndDate(Locker locker, Date currentDate);

    List<LockerDateDetail> findByLockerAndDateBetween(Locker locker, Date startDate, Date endDate);

    List<LockerDateDetail> findByLockerLockerIdAndDateBetween(long lockerId, Date startDate, Date endDate);

    Optional<LockerDateDetail> findByLockerLockerIdAndDate(long lockerId, Date date);
}

