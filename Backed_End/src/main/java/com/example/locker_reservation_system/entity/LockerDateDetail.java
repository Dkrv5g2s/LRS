package com.example.locker_reservation_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"date", "locker_id"}))
public class LockerDateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lockerDateDetailId;

    @ManyToOne
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @Column(nullable = false)
    private Date date;

    private String status;

    private String memo;
}
