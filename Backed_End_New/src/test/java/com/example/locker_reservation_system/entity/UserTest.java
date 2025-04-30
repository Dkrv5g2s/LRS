package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

class UserTest {
    private User user;
    private Locker locker;
    private final LocalDate D1 = LocalDate.of(2024, 1, 1);
    private final LocalDate D2 = LocalDate.of(2024, 1, 3);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setAccountName("test");
        user.setPhoneNumber("1234567890");
        user.setEncryptedPassword(encoder.encode("mypw"));

        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);
    }

    @Test
    void password_encryption_and_verify() {
        assertThat(user.checkPassword("mypw")).isTrue();
        assertThat(user.checkPassword("wrong")).isFalse();
    }

    @Test
    void reserve_locker() {
        Reservation r = user.reserve(locker, D1, D2);
        assertThat(r).isNotNull();
        assertThat(user.getReservations()).contains(r);
        assertThat(r.getUser()).isEqualTo(user);
        assertThat(r.getLocker()).isEqualTo(locker);
    }

    @Test
    void reserve_invalid_date_range() {
        assertThrows(IllegalArgumentException.class, () -> user.reserve(locker, D2, D1));
    }

    @Test
    void reserve_unavailable_locker() {
        User user2 = new User();
        user2.setUserId(2L);
        user2.setAccountName("test2");
        user2.setPhoneNumber("0987654321");
        user2.reserve(locker, D1, D2);

        assertThrows(RuntimeException.class, () -> user.reserve(locker, D1, D2));
    }
}
