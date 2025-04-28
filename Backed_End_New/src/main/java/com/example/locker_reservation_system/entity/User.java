package com.example.locker_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Entity @Data
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String accountName;

    @Column(nullable = false)
    @JsonIgnore
    private String password;            // 加密後

    @Column(nullable = false)
    private String phoneNumber;

    private Boolean isAdmin = false;

    /* ==== 關聯 ==== */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    /* ==== 密碼工具 ==== */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void setEncryptedPassword(String raw) { this.password = encoder.encode(raw); }

    public boolean checkPassword(String raw) { return encoder.matches(raw, this.password); }
}
