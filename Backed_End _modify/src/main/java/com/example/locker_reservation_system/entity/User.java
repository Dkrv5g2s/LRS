package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.repository.ReservationRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reservation> userReservationList;



    @PostConstruct
    public void init() {


    }

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void setEncryptedPassword(String rawPassword) {
        this.password = passwordEncoder.encode(rawPassword);
    }

    public boolean checkPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}
