package com.example.locker_reservation_system.dto;

import lombok.Data;

@Data
public class LockerStatusResponse {

    private Long lockerId;
    private String site;
    private Integer capacity;
    private Boolean usability;
    private String status;
    private String memo;
}