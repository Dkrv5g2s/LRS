package com.example.locker_reservation_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;

@Entity
@Getter
@Setter
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lockerId;

    private String site;

    private int capacity;

    private boolean usability;

    @OneToMany(mappedBy = "locker", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<LockerDateDetail> dateDetails;
}
