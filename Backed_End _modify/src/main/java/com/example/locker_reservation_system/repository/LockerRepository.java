package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.Locker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {
    // 可以定義其他查詢方法
}
