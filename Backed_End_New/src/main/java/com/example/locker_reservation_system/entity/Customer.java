package com.example.locker_reservation_system.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import java.time.LocalDate;
import java.util.Optional;

@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Reservation> reservations = new java.util.ArrayList<>();

    public java.util.List<Reservation> getReservations() {
        return reservations;
    }

    public Customer() {
        super();
        this.setIsAdmin(false);
    }

    public Customer(String accountName, String password, String phoneNumber) {
        super(accountName, password, phoneNumber);
        this.setIsAdmin(false);
    }

    /** 預約置物櫃 */
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

    /** 取消指定ID的預約 */
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

    /** 更新指定ID預約的日期 */
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