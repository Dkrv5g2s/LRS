package com.example.locker_reservation_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class Locker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lockerId;

    private String site;

    private Integer capacity;

    private Boolean usability;
}