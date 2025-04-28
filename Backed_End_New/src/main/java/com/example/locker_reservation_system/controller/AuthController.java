package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LoginRequest;
import com.example.locker_reservation_system.dto.RegisterRequest;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        // 檢查帳號是否已存在
        if (userRepo.findByAccountName(req.getAccountName()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Account name already exists");
        }

        User user = new User();
        user.setAccountName(req.getAccountName());
        user.setEncryptedPassword(req.getPassword());
        user.setPhoneNumber(req.getPhoneNumber());
        return ResponseEntity.ok(userRepo.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userRepo.findByAccountName(req.getAccountName());
        if (user != null && user.checkPassword(req.getPassword()))
            return ResponseEntity.ok(user);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
