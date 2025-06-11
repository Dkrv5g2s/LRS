package com.example.locker_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String accountName;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String encryptedPassword; // 加密後

    @Column(nullable = false)
    private String phoneNumber;

    private Boolean isAdmin = false;

    public User(String accountName, String password, String phoneNumber) {
        this.accountName = accountName;
        this.encryptedPassword = new BCryptPasswordEncoder().encode(password);
        this.phoneNumber = phoneNumber;
    }

    public boolean checkPassword(String password) {
        return new BCryptPasswordEncoder().matches(password, encryptedPassword);
    }
}
