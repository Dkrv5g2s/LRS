package com.example.locker_reservation_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locker")
public class LockerController {
//    @Autowired
//    private LockerService lockerService;
//
//    @GetMapping("/status")
//    public List<LockerStatusResponse> getLockerStatus(
//            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
//            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
//        return lockerService.getLockerStatusByDateRange(startDate, endDate);
//    }
}