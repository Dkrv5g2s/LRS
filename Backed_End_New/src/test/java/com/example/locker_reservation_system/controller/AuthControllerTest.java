package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LoginRequest;
import com.example.locker_reservation_system.dto.RegisterRequest;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User createUser() {
        return new User("test", "password", "1234567890");
    }

    @Test
    void register_shouldPersistUser() {
        RegisterRequest req = new RegisterRequest();
        req.setAccountName("tom");
        req.setPassword("pwd");
        req.setPhoneNumber("0988");

        User user = new User(req.getAccountName(), req.getPassword(), req.getPhoneNumber());

        when(userRepo.findByAccountName("tom")).thenReturn(null);
        when(userRepo.save(any(User.class))).thenReturn(user);

        ResponseEntity<?> result = authController.register(req);
        
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isInstanceOf(User.class);
        User savedUser = (User) result.getBody();
        assertThat(savedUser.getAccountName()).isEqualTo("tom");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("0988");
        assertThat(savedUser.checkPassword("pwd")).isTrue();

        verify(userRepo).findByAccountName("tom");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void register_duplicateAccountName() {
        RegisterRequest req = new RegisterRequest();
        req.setAccountName("tom");
        req.setPassword("pwd");
        req.setPhoneNumber("0988");

        when(userRepo.findByAccountName("tom")).thenReturn(createUser());

        ResponseEntity<?> result = authController.register(req);
        
        assertThat(result.getStatusCode().value()).isEqualTo(400);
        assertThat(result.getBody()).isEqualTo("Account name already exists");

        verify(userRepo).findByAccountName("tom");
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void login_success() {
        User u = createUser();
        when(userRepo.findByAccountName("test")).thenReturn(u);

        LoginRequest req = new LoginRequest();
        req.setAccountName("test");
        req.setPassword("password");

        ResponseEntity<?> result = authController.login(req);
        
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isInstanceOf(User.class);
        User user = (User) result.getBody();
        assertThat(user.getAccountName()).isEqualTo("test");

        verify(userRepo).findByAccountName("test");
    }

    @Test
    void login_fail_wrongPassword() {
        User u = createUser();
        when(userRepo.findByAccountName("test")).thenReturn(u);

        LoginRequest req = new LoginRequest();
        req.setAccountName("test");
        req.setPassword("wrong");

        ResponseEntity<?> result = authController.login(req);
        
        assertThat(result.getStatusCode().value()).isEqualTo(401);
        assertThat(result.getBody()).isEqualTo("Invalid credentials");

        verify(userRepo).findByAccountName("test");
    }

    @Test
    void login_fail_userNotFound() {
        when(userRepo.findByAccountName("test")).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setAccountName("test");
        req.setPassword("password");

        ResponseEntity<?> result = authController.login(req);
        
        assertThat(result.getStatusCode().value()).isEqualTo(401);
        assertThat(result.getBody()).isEqualTo("Invalid credentials");

        verify(userRepo).findByAccountName("test");
    }
}
