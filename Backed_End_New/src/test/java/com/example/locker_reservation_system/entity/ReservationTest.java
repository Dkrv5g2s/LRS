package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class ReservationTest {

    private Locker locker;
    private User user;
    private Reservation r;
    private final LocalDate D1 = LocalDate.of(2025, 1, 1);
    private final LocalDate D2 = LocalDate.of(2025, 1, 2);

    @BeforeEach
    void setup() {
        locker = new Locker();
        locker.setLockerId(2L);
        user = new User();
        user.setUserId(3L);
        r = locker.reserve(user, D1, D2);
    }

    @Test
    void barcode_shouldGenerate() {
        assertThat(r.getBarcode()).isNotBlank();
    }

    @Test
    void cancel_shouldDetach() {
        r.cancel();
        assertThat(user.getReservations()).isEmpty();
        assertThat(locker.getReservations()).isEmpty();
    }
}
