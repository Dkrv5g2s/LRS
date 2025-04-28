package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

class LockerDateDetailTest {
    private final LocalDate DATE = LocalDate.of(2025, 1, 1);

    @Test
    void testConstructor() {
        LockerDateDetail d = new LockerDateDetail();
        assertThat(d.getLockerDateDetailId()).isNull();
        assertThat(d.getLocker()).isNull();
        assertThat(d.getDate()).isNull();
        assertThat(d.getStatus()).isNull();
        assertThat(d.getMemo()).isNull();
    }

    @Test
    void set_and_get_fields() {
        LockerDateDetail d = new LockerDateDetail();
        d.setDate(DATE);
        d.setStatus("maintenance");
        d.setMemo("換鎖");

        assertThat(d.getStatus()).isEqualTo("maintenance");
        assertThat(d.getMemo()).contains("換鎖");
        assertThat(d.getDate()).isEqualTo(DATE);
    }

    @Test
    void set_locker() {
        Locker locker = new Locker();
        locker.setLockerId(1L);
        
        LockerDateDetail d = new LockerDateDetail();
        d.setLocker(locker);
        
        assertThat(d.getLocker()).isEqualTo(locker);
        assertThat(locker.getDateDetails()).contains(d);
    }

    @Test
    void change_status() {
        LockerDateDetail d = new LockerDateDetail();
        d.setStatus("available");
        assertThat(d.getStatus()).isEqualTo("available");
        
        d.setStatus("occupied");
        assertThat(d.getStatus()).isEqualTo("occupied");
    }

    @Test
    void update_memo() {
        LockerDateDetail d = new LockerDateDetail();
        d.setMemo("初始備註");
        assertThat(d.getMemo()).isEqualTo("初始備註");
        
        d.setMemo("更新備註");
        assertThat(d.getMemo()).isEqualTo("更新備註");
    }

    @Test
    void testToString() {
        LockerDateDetail d = new LockerDateDetail();
        d.setLockerDateDetailId(1L);
        d.setDate(DATE);
        d.setStatus("available");
        d.setMemo("測試備註");

        String str = d.toString();
        assertThat(str).contains("lockerDateDetailId=1");
        assertThat(str).contains("date=" + DATE);
        assertThat(str).contains("status='available'");
        assertThat(str).contains("memo='測試備註'");
    }
}
