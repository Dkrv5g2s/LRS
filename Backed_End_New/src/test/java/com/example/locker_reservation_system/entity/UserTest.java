package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    void password_encryption_and_verify() {
        User u = new User();
        u.setEncryptedPassword("mypw");
        assertThat(u.checkPassword("mypw")).isTrue();
        assertThat(u.checkPassword("wrong")).isFalse();
    }
}
