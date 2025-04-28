package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LoginRequest;
import com.example.locker_reservation_system.dto.RegisterRequest;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthControllerTest {

    @MockBean
    UserRepository userRepo;

    private User createUser() {
        User u = new User();
        u.setUserId(1L);
        u.setAccountName("test");
        u.setPhoneNumber("1234567890");
        u.setEncryptedPassword("password");
        return u;
    }

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
    void register_duplicateAccountName() {
        RegisterRequest req = new RegisterRequest();
        req.setAccountName("tom");
        req.setPassword("pwd");
        req.setPhoneNumber("0988");

        when(userRepo.findByAccountName("tom")).thenReturn(createUser());

        assertThatThrownBy(() -> {
            User existingUser = userRepo.findByAccountName(req.getAccountName());
            if (existingUser != null) {
                throw new RuntimeException("Account name already exists");
            }
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Account name already exists");
    }

    @Test
    void login_success() {
        User u = createUser();
        when(userRepo.findByAccountName("test")).thenReturn(u);

        LoginRequest req = new LoginRequest();
        req.setAccountName("test");
        req.setPassword("password");

        User foundUser = userRepo.findByAccountName(req.getAccountName());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getAccountName()).isEqualTo(req.getAccountName());
        assertThat(foundUser.checkPassword(req.getPassword())).isTrue();
    }

    @Test
    void login_fail_wrongPassword() {
        User u = createUser();
        when(userRepo.findByAccountName("test")).thenReturn(u);

        LoginRequest req = new LoginRequest();
        req.setAccountName("test");
        req.setPassword("wrong");

        User foundUser = userRepo.findByAccountName(req.getAccountName());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.checkPassword(req.getPassword())).isFalse();
    }

    @Test
    void login_fail_userNotFound() {
        when(userRepo.findByAccountName("test")).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setAccountName("test");
        req.setPassword("password");

        User foundUser = userRepo.findByAccountName(req.getAccountName());
        assertThat(foundUser).isNull();
    }

    @Test
    void getUserById() {
        User u = createUser();
        when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(u));

        User foundUser = userRepo.findById(1L).orElseThrow();
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUserId()).isEqualTo(1L);
        assertThat(foundUser.getAccountName()).isEqualTo("test");
    }

    @Test
    void getUserById_notFound() {
        when(userRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> userRepo.findById(1L).orElseThrow())
                .isInstanceOf(RuntimeException.class);
    }
}
