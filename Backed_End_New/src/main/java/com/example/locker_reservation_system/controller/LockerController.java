package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.*;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.repository.LockerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController @RequestMapping("/api/lockers")
public class LockerController {

    @Autowired private LockerRepository lockerRepo;

    /* ============ 新增 Locker ============ */
    @PostMapping
    @Transactional
    public Locker addLocker(@RequestParam long lockerId, @RequestParam int capacity) {
        // 檢查容量是否有效
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        // 查找並檢查 locker 是否存在
        Locker locker = lockerRepo.findById(lockerId)
                .orElseThrow(() -> new IllegalArgumentException("Locker with ID " + lockerId + " not found"));

        // 檢查 locker 是否已經可用
        if (locker.getUsability()) {
            throw new IllegalStateException("Locker is already in use");
        }

        // 設置 locker 為可用並更新容量
        locker.setUsability(true);
        locker.setCapacity(capacity);

        return lockerRepo.save(locker);
    }

    /* ============ 編輯 Locker ============ */
    @PutMapping("/{id}")
    @Transactional
    public Locker updateLocker(@PathVariable long id, @RequestBody LockerUpdateRequest req) {
        // 驗證日期範圍
        if (req.getStartDate() != null && req.getEndDate() != null && 
            req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        Locker locker = lockerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Locker with ID " + id + " not found"));

        // 檢查日期範圍是否可用
        if (req.getStartDate() != null && req.getEndDate() != null) {
            if (!locker.isAvailable(req.getStartDate(), req.getEndDate())) {
                throw new IllegalStateException("Cannot update locker - some dates in the range are already reserved");
            }
        }

        // 更新容量
        if (req.getCapacity() != null && req.getCapacity() > 0) {
            locker.setCapacity(req.getCapacity());
        }

        // 更新日期範圍內的狀態和備註
        if (req.getStartDate() != null && req.getEndDate() != null && 
            (req.getStatus() != null || req.getMemo() != null)) {
            
            // 更新狀態
            if (req.getStatus() != null) {
                locker.markDateRange(req.getStartDate(), req.getEndDate(), req.getStatus());
            }
            
            // 更新備註
            if (req.getMemo() != null) {
                locker.getDateDetails().stream()
                    .filter(detail -> !detail.getDate().isBefore(req.getStartDate()) && 
                                    !detail.getDate().isAfter(req.getEndDate()))
                    .forEach(detail -> detail.setMemo(req.getMemo()));
            }
        }

        return lockerRepo.save(locker);
    }

    /* ============ 刪除 Locker ============ */
    @DeleteMapping("/{id}")
    @Transactional
    public void deleteLocker(@PathVariable long id) {
        Locker locker = lockerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Locker with ID " + id + " not found"));

        // 檢查是否所有日期都是可用的
        boolean allAvailable = locker.getDateDetails().stream()
                .allMatch(detail -> "available".equals(detail.getStatus()));

        if (!allAvailable) {
            throw new IllegalStateException("Cannot delete locker - some dates are still reserved");
        }

        // 軟刪除：將可用性設為 false
        locker.setUsability(false);
        lockerRepo.save(locker);
    }

    /* ============ 查詢 Locker 狀態 ============ */
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
