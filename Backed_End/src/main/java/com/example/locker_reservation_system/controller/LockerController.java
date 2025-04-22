package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.service.LockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/locker")
public class LockerController {

    @Autowired
    private LockerService lockerService;

    @GetMapping("/status")
    public List<LockerStatusResponse> getLockerStatus(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        System.out.println(date);
        return lockerService.getLockerStatusByDate(date);
    }
}