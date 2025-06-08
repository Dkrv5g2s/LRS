// repository/UserRepository.java
package com.example.locker_reservation_system.repository;

import com.example.locker_reservation_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByAccountName(String accountName);
    List<User> findByAccountNameContainingIgnoreCase(String accountName);
}
