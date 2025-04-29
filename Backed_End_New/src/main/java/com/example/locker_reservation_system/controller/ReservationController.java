package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.*;
import com.example.locker_reservation_system.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController @RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired private ReservationRepository reservationRepo;
    @Autowired private LockerRepository      lockerRepo;
    @Autowired private UserRepository        userRepo;

    /* ============ 查詢狀態 ============ */
    @GetMapping("/status")
    public List<LockerStatusResponse> getLockerReservationStatus(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        if (start.isAfter(end)) throw new IllegalArgumentException("start > end");
        return lockerRepo.findAll().stream()
                .map(l -> l.toStatusResponse(start, end))
                .collect(Collectors.toList());
    }

    /* ===== 新增預約 ===== */
    @PostMapping
    @Transactional
    public Reservation reserve(@RequestBody ReservationRequest req) {
        Locker locker = lockerRepo.findById(req.getLockerId())
                .orElseThrow(() -> new RuntimeException("Locker not found"));
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.reserve(locker, req.getStartDate(), req.getEndDate());
    }

    /* ===== 依使用者查詢 ===== */
    @GetMapping("/user/{userId}")
    public List<Reservation> getByUser(@PathVariable Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getReservations();
    }

    /* ===== 修改日期 ===== */
    @PutMapping("/{id}/dates")
    @Transactional
    public Reservation updateReservationDates(@PathVariable Long id,
                                   @RequestParam("newStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newStart,
                                   @RequestParam("newEndDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEnd) {

        Reservation r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.reschedule(newStart, newEnd);
        return r;
    }

    /* ===== 取消 ===== */
    @DeleteMapping("/{id}")
    @Transactional
    public void cancel(@PathVariable Long id) {
        Reservation r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        r.cancel();
        reservationRepo.delete(r);          // 仍需呼叫以產生 delete SQL
    }

    /* ===== 管理員預約 ===== */
    @PostMapping("/admin")
    @Transactional
    public Reservation adminReserve(@RequestBody ReservationRequest req) {
        // 與一般 reserve 相同，只是路徑不同（可再加 @PreAuthorize）
        return reserve(req);
    }
}
