package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.service.LockerService;
import com.example.locker_reservation_system.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired private ReservationService reservationService;
    @Autowired private LockerService lockerService;

    /* 查詢指定日期區段每個 locker 的狀態 */
    @GetMapping("/status")
    public List<LockerStatusResponse> getLockerReservationStatus(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return lockerService.getLockerStatusByDateRange(startDate, endDate);
    }

    /* 預約 */
    @PostMapping
    public Reservation reserve(@RequestBody ReservationRequest request) {
        return reservationService.reserve(request);
    }

    /* 依使用者查詢 */
    @GetMapping("/user/{userId}")
    public List<Reservation> getReservationsByUserId(@PathVariable Long userId) {
        return reservationService.getReservationsByUserId(userId);
    }

    /* 修改預約日期 */
    @PutMapping("/{reservationId}/dates")
    public Reservation updateReservationDates(
            @PathVariable Long reservationId,
            @RequestParam("newStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newStartDate,
            @RequestParam("newEndDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate) {

        return reservationService.updateReservationDates(reservationId, newStartDate, newEndDate);
    }

    /* 取消預約 */
    @DeleteMapping("/{reservationId}")
    public void cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
    }
}
