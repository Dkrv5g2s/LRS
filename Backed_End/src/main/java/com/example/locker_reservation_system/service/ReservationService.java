package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.LockerRepository;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private UserRepository userRepository;

    public Reservation createReservation(ReservationRequest request) {
        Locker locker = lockerRepository.findById(request.getLockerId())
                .orElseThrow(() -> new RuntimeException("Locker not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 檢查是否有人已經預約了該時間段和 locker
        List<Reservation> existingReservations = reservationRepository.findByLockerAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                locker, request.getEndDate(), request.getStartDate());
        if (!existingReservations.isEmpty()) {
            throw new RuntimeException("Locker is already reserved for the requested time period.");
        }

        Reservation reservation = new Reservation();
        reservation.setLocker(locker);
        reservation.setUser(user);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());

        return reservationRepository.save(reservation);
    }
}
