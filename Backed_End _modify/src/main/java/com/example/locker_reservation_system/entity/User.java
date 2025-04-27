package com.example.locker_reservation_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String accountName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    private Boolean isAdmin = false;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void setEncryptedPassword(String rawPassword) {
        this.password = passwordEncoder.encode(rawPassword);
    }

    public boolean checkPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}
