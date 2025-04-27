package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private LockerService lockerService;
    @Autowired private LockerDateDetailService lockerDateDetailService;
    @Autowired private UserRepository userRepository;

    /* 新增預約 */
    public Reservation reserve(ReservationRequest request) {
        Locker locker = lockerService.findLockerById(request.getLockerId())
                .orElseThrow(() -> new RuntimeException("Locker not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 衝突檢查
        List<Reservation> conflict = reservationRepository
                .findByLockerAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        locker, request.getEndDate(), request.getStartDate());

        if (!conflict.isEmpty()) throw new RuntimeException("Locker already reserved in this period");

        Reservation reservation = new Reservation();
        reservation.setLocker(locker);
        reservation.setUser(user);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());

        Reservation saved = reservationRepository.save(reservation);

        lockerDateDetailService.updateLockerDateDetails(
                locker, request.getStartDate(), request.getEndDate(), "occupied");

        return saved;
    }

    public Reservation updateReservationDates(Long id, LocalDate newStart, LocalDate newEnd) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        Locker locker = r.getLocker();

        // 原區段設為可用
        lockerDateDetailService.updateLockerDateDetails(locker, r.getStartDate(), r.getEndDate(), "available");

        r.setStartDate(newStart);
        r.setEndDate(newEnd);
        Reservation updated = reservationRepository.save(r);

        // 新區段設為佔用
        lockerDateDetailService.updateLockerDateDetails(locker, newStart, newEnd, "occupied");
        return updated;
    }

    public void cancelReservation(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        lockerDateDetailService.updateLockerDateDetails(r.getLocker(), r.getStartDate(), r.getEndDate(), "available");
        reservationRepository.delete(r);
    }

    public List<Reservation> getReservationsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByUser(user);
    }
}
