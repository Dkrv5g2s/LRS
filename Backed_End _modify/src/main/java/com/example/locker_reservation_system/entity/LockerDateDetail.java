package com.example.locker_reservation_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.Date;

@Entity
@Getter
@Setter
public class LockerDateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lockerDateDetailId;

    @ManyToOne
    @JoinColumn(name = "locker_id", nullable = false)
    @JsonBackReference
    private Locker locker;

    private Date date;
    private String status;
    private String memo;

    // Getters and setters
}
