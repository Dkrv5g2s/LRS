package com.example.locker_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.locker_reservation_system.repository.LockerRepository;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;

@Entity
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String accountName;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String encryptedPassword; // 加密後

    @Column(nullable = false)
    private String phoneNumber;

    private Boolean isAdmin = false;

    /* ==== 關聯 ==== */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    public User(String accountName, String password, String phoneNumber) {
        this.accountName = accountName;
        this.encryptedPassword = new BCryptPasswordEncoder().encode(password);
        this.phoneNumber = phoneNumber;
    }

    public boolean checkPassword(String password) {
        return new BCryptPasswordEncoder().matches(password, encryptedPassword);
    }

    // ========== User's Own Reservation Actions ========== //

    /** 預約置物櫃 */
    public Reservation reserve(Locker locker, LocalDate start, LocalDate end) {
        if (start.isAfter(end))
            throw new IllegalArgumentException("start > end");

        // 檢查置物櫃是否可用
        if (!locker.isAvailable(start, end)) {
            throw new RuntimeException("Locker already reserved in this period");
        }

        // 建立預約
        Reservation r = new Reservation(locker, this, start, end);
        reservations.add(r);
        return r;
    }

    /** 取消指定ID的預約 */
    public void cancelReservation(Long reservationId) {
        Optional<Reservation> reservationOptional = this.reservations.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst();

        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();
            // Make sure the reservation is owned by this user before canceling
            if (!reservation.getUser().getUserId().equals(this.userId)) {
                throw new RuntimeException("Reservation does not belong to this user.");
            }
            reservation.cancel(); // 调用Reservation实体的方法取消
            this.reservations.remove(reservation);
        } else {
            throw new RuntimeException("Reservation with ID " + reservationId + " not found for this user.");
        }
    }

    /** 更新指定ID預約的日期 */
    public Reservation updateReservationDates(Long reservationId, LocalDate newStart, LocalDate newEnd) {
        Optional<Reservation> reservationOptional = this.reservations.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst();

        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();
            // Make sure the reservation is owned by this user before updating
            if (!reservation.getUser().getUserId().equals(this.userId)) {
                throw new RuntimeException("Reservation does not belong to this user.");
            }
            reservation.reschedule(newStart, newEnd); // 调用Reservation实体的方法重新安排日期
            return reservation;
        } else {
            throw new RuntimeException("Reservation with ID " + reservationId + " not found for this user.");
        }
    }

    // ========== Administrator's Actions on Other Users' Data ========== //

    public List<User> adminSearchCustomers(String query, UserRepository userRepo) {
        if (!this.getIsAdmin()) {
            throw new RuntimeException("Only administrators can search customers.");
        }
        return userRepo.findByAccountNameContainingIgnoreCase(query).stream()
                .filter(user -> !user.getIsAdmin()) // Filter out administrators
                .collect(Collectors.toList());
    }

    public Reservation adminReserveLockerForUser(User targetUser, Locker locker, LocalDate startDate,
            LocalDate endDate) {
        if (!this.getIsAdmin()) {
            throw new RuntimeException("Only administrators can reserve lockers for other users.");
        }

        return targetUser.reserve(locker, startDate, endDate);
    }

    public List<Reservation> adminGetReservationsForUser(User targetUser) {
        if (!this.getIsAdmin()) {
            throw new RuntimeException("Only administrators can view other users' reservations.");
        }

        return targetUser.getReservations();
    }

    public Reservation adminUpdateUserReservationDates(Reservation reservation, LocalDate newStart, LocalDate newEnd) {
        if (!this.getIsAdmin()) {
            throw new RuntimeException("Only administrators can update other users' reservations.");
        }

        User targetUser = reservation.getUser();
        return targetUser.updateReservationDates(reservation.getId(), newStart, newEnd);
    }

    public void adminCancelUserReservation(Reservation reservation, ReservationRepository reservationRepo) {
        if (!this.getIsAdmin()) {
            throw new RuntimeException("Only administrators can cancel other users' reservations.");
        }

        User targetUser = reservation.getUser();
        targetUser.cancelReservation(reservation.getId());
        reservationRepo.delete(reservation); // Delete from the database
    }

}
