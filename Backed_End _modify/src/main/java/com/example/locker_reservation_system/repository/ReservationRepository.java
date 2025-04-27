package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStartDateBeforeAndEndDateAfter(Date startDate, Date endDate);

    List<Reservation> findByLockerAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Locker locker, Date endDate, Date startDate);

    List<Reservation> findByUser(User user);
}