package com.example.locker_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private Customer customer;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "start_date")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Lob
    @Column(name = "barcode")
    private String barcode;

    public Reservation(Locker locker, Customer customer, LocalDate start, LocalDate end) {
        this.locker = locker;
        this.customer = customer;
        this.startDate = start;
        this.endDate = end;
        regenerateBarcode();
        locker.markDateRangeStatus(start, end, "occupied");
    }

    public void cancel() {
        this.locker.release(this.startDate, this.endDate);
        this.customer.getReservations().remove(this);
    }

    public void regenerateBarcode() {
        String raw = locker.getLockerId() + "-" + customer.getUserId() + "-" + startDate + "-" + endDate + "-" + System.currentTimeMillis();
        this.barcode = com.example.locker_reservation_system.util.BarcodeUtil.generateBase64(raw);
    }

    public void reschedule(LocalDate newStart, LocalDate newEnd) {
        if (newStart.isAfter(newEnd)) throw new IllegalArgumentException("Start date must be before or equal to end date");

        locker.release(startDate, endDate);

        if (!locker.isAvailable(newStart, newEnd)) {
            locker.markDateRangeStatus(startDate, endDate, "occupied");
            throw new RuntimeException("Locker already reserved in new period");
        }

        locker.markDateRangeStatus(newStart, newEnd, "occupied");
        this.startDate = newStart;
        this.endDate = newEnd;
        regenerateBarcode();
    }
}
