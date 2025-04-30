package com.example.locker_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity @Data @NoArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String accountName;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String encryptedPassword;            // 加密後

    @Column(nullable = false)
    private String phoneNumber;

    private Boolean isAdmin = false;

    /* ==== 關聯 ==== */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    public User(String accountName, String password, String phoneNumber) {
        this.accountName = accountName;
        this.encryptedPassword = new BCryptPasswordEncoder().encode(password);
        this.phoneNumber = phoneNumber;
    }

    public boolean checkPassword(String password) {
        return new BCryptPasswordEncoder().matches(password, encryptedPassword);
    }

    /* ====== 預約相關行為 ====== */
    /** 預約置物櫃 */
    public Reservation reserve(Locker locker, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) throw new IllegalArgumentException("start > end");
        
        // 檢查置物櫃是否可用
        if (!locker.isAvailable(start, end)) {
            throw new RuntimeException("Locker already reserved in this period");
        }

//        // 標記日期範圍
//        locker.markDateRange(start, end, "occupied");

        // 建立預約
        Reservation r = new Reservation(locker, this, start, end);
        reservations.add(r);
        return r;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + accountName + '\'' +
                ", email='" + phoneNumber + '\'' +
                '}';
    }
}
