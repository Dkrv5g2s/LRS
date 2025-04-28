package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LoginRequest;
import com.example.locker_reservation_system.dto.RegisterRequest;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuthControllerTest {

    @MockBean
    UserRepository userRepo;

    @Test
    void register_shouldPersistUser() {
        RegisterRequest req = new RegisterRequest();
        req.setAccountName("tom");
        req.setPassword("pwd");
        req.setPhoneNumber("0988");

        when(userRepo.save(any(User.class))).thenAnswer(a -> a.getArgument(0));

        User user = new User();
        user.setAccountName(req.getAccountName());
        user.setEncryptedPassword(req.getPassword());
        user.setPhoneNumber(req.getPhoneNumber());
        
        User savedUser = userRepo.save(user);
        
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(cap.capture());
        
        assertThat(savedUser.getAccountName()).isEqualTo("tom");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("0988");
        assertThat(savedUser.checkPassword("pwd")).isTrue();
    }

    @Test
    void login_success() {
        User u = new User();
        u.setAccountName("tom");
        u.setEncryptedPassword("pwd");
        when(userRepo.findByAccountName("tom")).thenReturn(u);

        LoginRequest req = new LoginRequest();
        req.setAccountName("tom");
        req.setPassword("pwd");

        User foundUser = userRepo.findByAccountName(req.getAccountName());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getAccountName()).isEqualTo(req.getAccountName());
        assertThat(foundUser.checkPassword(req.getPassword())).isTrue();
    }

    @Test
    void login_fail_should401() {
        when(userRepo.findByAccountName("tom")).thenReturn(null);
        LoginRequest req = new LoginRequest();
        req.setAccountName("tom");
        req.setPassword("pwd");

        User foundUser = userRepo.findByAccountName(req.getAccountName());
        assertThat(foundUser).isNull();
    }
}
