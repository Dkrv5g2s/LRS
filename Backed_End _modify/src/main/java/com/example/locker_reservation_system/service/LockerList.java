package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.LockerDateDetail;
import com.example.locker_reservation_system.repository.LockerRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.List;

@Service
public class LockerList {

    @Autowired
    private LockerRepository lockerRepository;


    private List<Locker> lockers;

    @PostConstruct
    public void init() {
        this.lockers = lockerRepository.findAll();
        lockers.forEach(locker -> {
            // 確保每個 Locker 的 dateDetails 都已初始化
            locker.getDateDetails().size(); // 使用 size() 強制加載

            // 打印 Locker 信息
            System.out.println("Locker ID: " + locker.getLockerId());
            System.out.println("Site: " + locker.getSite());
            System.out.println("Capacity: " + locker.getCapacity());
            System.out.println("Usability: " + locker.isUsability());
            System.out.println("Date Details:");

            // 打印每個 LockerDateDetail 信息
            for (LockerDateDetail detail : locker.getDateDetails()) {
                System.out.println("  Detail ID: " + detail.getLockerDateDetailId());
                System.out.println("  Date: " + detail.getDate());
                System.out.println("  Status: " + detail.getStatus());
                System.out.println("  Memo: " + detail.getMemo());
            }
            System.out.println("---------------");
        });
    }

    public List<Locker> getLockers() {
        return lockers;
    }
}
