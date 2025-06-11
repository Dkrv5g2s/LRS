package com.example.locker_reservation_system.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Reservation> reservations = new java.util.ArrayList<>();

    public Customer() {
        super();
        this.setIsAdmin(false);
    }

    public Customer(String accountName, String password, String phoneNumber) {
        super(accountName, password, phoneNumber);
        this.setIsAdmin(false);
    }

    public Reservation reserve(Locker locker, LocalDate start, LocalDate end) {
        if (start.isAfter(end))
            throw new IllegalArgumentException("start > end");
        if (!locker.isAvailable(start, end)) {
            throw new RuntimeException("Locker already reserved in this period");
        }
        Reservation r = new Reservation(locker, this, start, end);
        getReservations().add(r);
        return r;
    }

    public void cancelReservation(Long reservationId) {
        Optional<Reservation> reservationOptional = this.getReservations().stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst();

        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();
            if (!reservation.getCustomer().getUserId().equals(this.getUserId())) {
                throw new RuntimeException("Reservation does not belong to this user.");
            }
            reservation.cancel();
            this.getReservations().remove(reservation);
        } else {
            throw new RuntimeException("Reservation with ID " + reservationId + " not found for this user.");
        }
    }

    public Reservation updateReservationDates(Long reservationId, LocalDate newStart, LocalDate newEnd) {
        Optional<Reservation> reservationOptional = this.getReservations().stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst();

        if (reservationOptional.isPresent()) {
            Reservation reservation = reservationOptional.get();
            if (!reservation.getCustomer().getUserId().equals(this.getUserId())) {
                throw new RuntimeException("Reservation does not belong to this user.");
            }
            reservation.reschedule(newStart, newEnd);
            return reservation;
        } else {
            throw new RuntimeException("Reservation with ID " + reservationId + " not found for this user.");
        }
    }
}
