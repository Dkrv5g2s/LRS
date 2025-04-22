package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.Locker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerRepository extends JpaRepository<Locker, Long> {
}