package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.entity.Administrator;
import com.example.locker_reservation_system.repository.LockerRepository;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations/admin")
public class ReservationManageController {

        @Autowired
        private ReservationRepository reservationRepo;
        @Autowired
        private LockerRepository lockerRepo;
        @Autowired
        private UserRepository userRepo;

        /* ===== 查詢用戶 ===== */
        @GetMapping("/users/search")
        public List<User> searchUsers(@RequestParam String query, @RequestParam Long adminUserId) {
                User user = userRepo.findById(adminUserId)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));
                if (!(user instanceof Administrator)) {
                        throw new RuntimeException("User is not an administrator");
                }
                Administrator adminUser = (Administrator) user;
                return adminUser.adminSearchCustomers(query, userRepo);
        }

        /* ===== 新增預約 ===== */
        @PostMapping
        @Transactional
        public Reservation reserveForUser(@RequestBody ReservationRequest req, @RequestParam Long adminUserId) {
                User user = userRepo.findById(adminUserId)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));
                if (!(user instanceof Administrator)) {
                        throw new RuntimeException("User is not an administrator");
                }
                Administrator adminUser = (Administrator) user;

                User targetUser = userRepo.findById(req.getUserId())
                                .orElseThrow(() -> new RuntimeException("Target User not found"));
                Locker locker = lockerRepo.findById(req.getLockerId())
                                .orElseThrow(() -> new RuntimeException("Locker not found"));

                return adminUser.adminReserveLockerForUser(
                                targetUser,
                                locker,
                                req.getStartDate(),
                                req.getEndDate());
        }

        /* ===== 依使用者查詢 ===== */
        @GetMapping("/{userId}")
        public List<Reservation> getReservationsForUser(@PathVariable Long userId, @RequestParam Long adminUserId) {
                User user = userRepo.findById(adminUserId)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));
                if (!(user instanceof Administrator)) {
                        throw new RuntimeException("User is not an administrator");
                }
                Administrator adminUser = (Administrator) user;

                User targetUser = userRepo.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Target User not found"));

                return adminUser.adminGetReservationsForUser(targetUser);
        }

        /* ===== 修改日期 ===== */
        @PutMapping("/{id}/dates")
        @Transactional
        public Reservation updateReservationDateForUser(@PathVariable Long id,
                        @RequestParam("newStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newStart,
                        @RequestParam("newEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEnd,
                        @RequestParam Long adminUserId) {
                User user = userRepo.findById(adminUserId)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));
                if (!(user instanceof Administrator)) {
                        throw new RuntimeException("User is not an administrator");
                }
                Administrator adminUser = (Administrator) user;

                Reservation reservation = reservationRepo.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));

                return adminUser.adminUpdateUserReservationDates(reservation, newStart, newEnd);
        }

        /* ===== 取消 ===== */
        @DeleteMapping("/{id}")
        @Transactional
        public void cancelReservationForUser(@PathVariable Long id, @RequestParam Long adminUserId) {
                User user = userRepo.findById(adminUserId)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));
                if (!(user instanceof Administrator)) {
                        throw new RuntimeException("User is not an administrator");
                }
                Administrator adminUser = (Administrator) user;

                Reservation reservation = reservationRepo.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));

                adminUser.adminCancelUserReservation(reservation, reservationRepo);
        }
}
