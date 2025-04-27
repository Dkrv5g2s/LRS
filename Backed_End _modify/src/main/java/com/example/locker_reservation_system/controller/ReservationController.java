package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.service.LockerService;
import com.example.locker_reservation_system.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private LockerService lockerService;

    @GetMapping("/status")
    public List<LockerStatusResponse> getLockerReservationStatus(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return lockerService.getLockerStatusByDateRange(startDate, endDate);
    }

    @PostMapping
    public Reservation reserve(@RequestBody ReservationRequest request) {
        return reservationService.reserve(request);
    }

    @GetMapping("/user/{userId}")
    public List<Reservation> getReservationsByUserId(@PathVariable Long userId) {
        return reservationService.getReservationsByUserId(userId);
    }
}


//@PutMapping("/{reservationId}/dates")
//public Reservation updateReservationDates(
//        @PathVariable Long reservationId,
//        @RequestParam("newStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date newStartDate,
//        @RequestParam("newEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date newEndDate) {
//    return reservationService.updateReservationDates(reservationId, newStartDate, newEndDate);
//}
//
//@DeleteMapping("/{reservationId}")
//public void cancelReservation(@PathVariable Long reservationId) {
//    reservationService.cancelReservation(reservationId);
//}
