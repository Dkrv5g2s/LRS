package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStartDateBeforeAndEndDateAfter(Date startDate, Date endDate);

    List<Reservation> findByLockerAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Locker locker, Date endDate, Date startDate);
}