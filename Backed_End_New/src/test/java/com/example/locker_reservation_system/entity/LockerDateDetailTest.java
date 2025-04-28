package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

class LockerDateDetailTest {

    @Test
    void set_and_get_fields() {
        LockerDateDetail d = new LockerDateDetail();
        d.setDate(LocalDate.of(2025, 1, 1));
        d.setStatus("maintenance");
        d.setMemo("換鎖");

        assertThat(d.getStatus()).isEqualTo("maintenance");
        assertThat(d.getMemo()).contains("換鎖");
    }
}
