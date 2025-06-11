package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue("ADMIN")
public class Administrator extends User {

    public Administrator() {
        super();
        this.setIsAdmin(true);
    }

    public Administrator(String accountName, String password, String phoneNumber) {
        super(accountName, password, phoneNumber);
        this.setIsAdmin(true);
    }

    public List<User> adminSearchCustomers(String query, UserRepository userRepo) {
        if (!this.getIsAdmin()) {
            throw new RuntimeException("Only administrators can search customers.");
        }
        return userRepo.findByAccountNameContainingIgnoreCase(query).stream()
                .filter(user -> !user.getIsAdmin())
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
        reservationRepo.delete(reservation);
    }
}