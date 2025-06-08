package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.LockerRepository;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController @RequestMapping("/api/reservations/admin")
public class ReservationManageController {

    @Autowired private ReservationRepository reservationRepo;
    @Autowired private LockerRepository      lockerRepo;
    @Autowired private UserRepository        userRepo;

    /* ===== 查詢用戶 ===== */
    @GetMapping("/users/search")
    public List<User> searchUsers(@RequestParam String query) {
        return userRepo.findByAccountNameContainingIgnoreCase(query);
    }

    /* ===== 新增預約 ===== */
    @PostMapping
    @Transactional
    public Reservation reserveByAdmin(@RequestBody ReservationRequest req) {
        // 驗證被預約用戶
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 驗證置物櫃
        Locker locker = lockerRepo.findById(req.getLockerId())
                .orElseThrow(() -> new RuntimeException("Locker not found"));

        // 創建預約 (利用 User 實體的 reserve 方法，符合 OOAD)
        return user.reserve(locker, req.getStartDate(), req.getEndDate());
    }

    /* ===== 依使用者查詢 ===== */
    @GetMapping("/{userId}")
    public List<Reservation> getReservationsByUserId(@PathVariable Long userId) {
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
}
