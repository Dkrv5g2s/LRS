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
//    @PostMapping
//    @Transactional
//    public Locker create(@RequestBody LockerUpsertRequest req) {
//        Locker l = Locker.create(req.getSite(), req.getCapacity(), req.getUsability());
//        return lockerRepo.save(l);
//    }

    /* ============ 編輯 Locker ============ */
//    @PutMapping("/{id}")
//    @Transactional
//    public Locker update(@PathVariable long id, @RequestBody LockerUpsertRequest req) {
//        Locker l = lockerRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Locker not found"));
//        l.updateBasicInfo(req.getSite(), req.getCapacity(), req.getUsability());
//        return l;
//    }
//
//    /* ============ 調整單日狀態 / 備註 ============ */
//    @PutMapping("/{id}/date-details")
//    @Transactional
//    public void overrideDate(@PathVariable long id,
//                             @RequestBody DateDetailUpdateRequest req) {
//
//        Locker l = lockerRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Locker not found"));
//        l.overrideDateDetail(req.getDate(), req.getStatus(), req.getMemo());
//    }
}
