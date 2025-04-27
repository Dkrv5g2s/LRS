package com.example.locker_reservation_system.service;


import com.example.locker_reservation_system.dto.LoginRequest;
import com.example.locker_reservation_system.dto.RegisterRequest;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User register(RegisterRequest request) {
        User user = new User();
        user.setAccountName(request.getAccountName());
        user.setPassword(request.getPassword());  // 密碼應加密處理
        user.setPhoneNumber(request.getPhoneNumber());
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        Optional<User> user = Optional.ofNullable(userRepository.findByAccountName(request.getAccountName()));
        return user.filter(u -> u.getPassword().equals(request.getPassword())).orElse(null);
    }
}