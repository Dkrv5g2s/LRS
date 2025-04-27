package com.example.locker_reservation_system.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ReservationRequest {
    private long lockerId;
    private long userId;
    private Date startDate;
    private Date endDate;
}