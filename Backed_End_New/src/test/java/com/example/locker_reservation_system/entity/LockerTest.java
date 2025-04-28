package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class LockerTest {

    private Locker locker;
    private User user;
    private final LocalDate D1 = LocalDate.of(2025, 1, 1);
    private final LocalDate D2 = LocalDate.of(2025, 1, 2);
    private final LocalDate D3 = LocalDate.of(2025, 1, 3);
    private final LocalDate D4 = LocalDate.of(2025, 1, 4);

    @BeforeEach
    void init() {
        locker = new Locker();
        locker.setLockerId(1L);
        user = new User();
        user.setUserId(9L);
    }

    @Test
    void isAvailable_noRecord() {
        assertThat(locker.isAvailable(D1, D3)).isTrue();
    }

    @Test
    void reserve_and_dateDetailMarked() {
        Reservation r = locker.reserve(user, D1, D2);
        assertThat(r).isNotNull();

        List<String> statuses = locker.getDateDetails().stream()
                .map(LockerDateDetail::getStatus)
                .collect(Collectors.toList());
        assertThat(statuses).containsOnly("occupied");
    }

    @Test
    void reschedule_success() {
        Reservation r = locker.reserve(user, D1, D2);
        r.reschedule(D3, D4);
        assertThat(r.getStartDate()).isEqualTo(D3);
        assertThat(locker.getDateDetails()
                .stream()
                .filter(d -> d.getDate().equals(D3))
                .findFirst().get().getStatus())
                .isEqualTo("occupied");
    }

    @Test
    void cancel_shouldBecomeAvailable() {
        Reservation r = locker.reserve(user, D1, D2);
        r.cancel();
        assertThat(locker.getDateDetails())
                .allMatch(d -> "available".equals(d.getStatus()));
    }
}
