package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.*;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.repository.LockerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/lockers")
public class LockerController {

    @Autowired
    private LockerRepository lockerRepo;

    @PostMapping
    @Transactional
    public Locker addLocker(@RequestParam long lockerId, @RequestParam int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        Locker locker = lockerRepo.findById(lockerId)
                .orElseThrow(() -> new IllegalArgumentException("Locker with ID " + lockerId + " not found"));

        if (locker.getUsability()) {
            throw new IllegalStateException("Locker is already in use");
        }

        locker.add(capacity);

        return lockerRepo.save(locker);
    }

    @PutMapping("/{id}")
    @Transactional
    public Locker updateLocker(@PathVariable long id, @RequestBody LockerUpdateRequest req) {
        if (req.getStartDate() != null && req.getEndDate() != null &&
                req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        Locker locker = lockerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Locker with ID " + id + " not found"));

        locker.update(req);

        return lockerRepo.save(locker);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteLocker(@PathVariable long id) {
        Locker locker = lockerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Locker with ID " + id + " not found"));

        boolean allAvailable = locker.getDateDetails().stream()
                .allMatch(detail -> "available".equals(detail.getStatus()));

        if (!allAvailable) {
            throw new IllegalStateException("Cannot delete locker - some dates are still reserved");
        }

        locker.setUsability(false);
        lockerRepo.save(locker);
    }

    @GetMapping("/{id}/status")
    public LockerStatusResponse getLockerStatus(
            @PathVariable long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        Locker locker = lockerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Locker with ID " + id + " not found"));

        return locker.toStatusResponse(startDate, endDate);
    }
}
