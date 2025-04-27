package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LoginRequest;
import com.example.locker_reservation_system.dto.RegisterRequest;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setAccountName(request.getAccountName());
        user.setEncryptedPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> user = Optional.ofNullable(userRepository.findByAccountName(request.getAccountName()));

        if (user.isPresent() && user.get().checkPassword(request.getPassword())) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

}
