// repository/ReservationRepository.java
package com.example.locker_reservation_system.repository;
import com.example.locker_reservation_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReservationRepository extends JpaRepository<Reservation, Long> { }
