package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class ReservationTest {
    private static final LocalDate D1 = LocalDate.of(2024, 1, 1);
    private static final LocalDate D2 = LocalDate.of(2024, 1, 3);
    private static final LocalDate D3 = LocalDate.of(2024, 1, 5);

    private Locker locker;
    private User user;
    private Reservation r;

    @BeforeEach
    void setUp() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);

        user = new User();
        user.setUserId(1L);
        user.setAccountName("test");
        user.setPhoneNumber("1234567890");

        r = user.reserve(locker, D1, D2);
    }

    @Test
    void testCancel() {
        r.cancel();
        assertTrue(locker.isAvailable(D1, D2));
        assertFalse(user.getReservations().contains(r));
        assertFalse(locker.getReservations().contains(r));
    }

    @Test
    void testReschedule() {
        r.reschedule(D2, D3);
        assertTrue(locker.isAvailable(D1, D2));
        assertFalse(locker.isAvailable(D2, D3));
        assertEquals(D2, r.getStartDate());
        assertEquals(D3, r.getEndDate());
    }

    @Test
    void testRescheduleConflict() {
        User user2 = new User();
        user2.setUserId(2L);
        user2.setAccountName("test2");
        user2.setPhoneNumber("0987654321");
        user2.reserve(locker, D2, D3);

        assertThrows(RuntimeException.class, () -> r.reschedule(D2, D3));
        assertEquals(D1, r.getStartDate());
        assertEquals(D2, r.getEndDate());
    }
}
