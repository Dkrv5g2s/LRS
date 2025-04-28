package com.example.locker_reservation_system.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Base64;

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
    void testConstructor() {
        Reservation reservation = new Reservation(locker, user, D1, D2);
        assertEquals(locker, reservation.getLocker());
        assertEquals(user, reservation.getUser());
        assertEquals(D1, reservation.getStartDate());
        assertEquals(D2, reservation.getEndDate());
        assertNotNull(reservation.getBarcode());
    }

    @Test
    void testCancel() {
        r.cancel();
        assertTrue(locker.isAvailable(D1, D2));
        assertFalse(user.getReservations().contains(r));
    }

    @Test
    void testReschedule() {
        LocalDate newStartDate = LocalDate.now().plusDays(5);
        LocalDate newEndDate = LocalDate.now().plusDays(7);
        
        // 重新預約
        r.reschedule(newStartDate, newEndDate);
        
        assertEquals(newStartDate, r.getStartDate());
        assertEquals(newEndDate, r.getEndDate());
        assertFalse(locker.isAvailable(newStartDate, newEndDate)); // 新的日期範圍應該被佔用
    }

    @Test
    void testRescheduleConflict() {
        LocalDate newStartDate = LocalDate.now().plusDays(1);
        LocalDate newEndDate = LocalDate.now().plusDays(3);
        
        // 創建另一個預約並標記置物櫃的日期範圍
        Reservation anotherReservation = new Reservation(locker, user, newStartDate, newEndDate);
        locker.markDateRange(newStartDate, newEndDate, "occupied");
        user.getReservations().add(anotherReservation);
        
        // 嘗試重新預約到已被佔用的日期範圍
        assertThrows(RuntimeException.class, () -> {
            r.reschedule(newStartDate, newEndDate);
        });
    }

    @Test
    void testRescheduleInvalidDateRange() {
        assertThrows(IllegalArgumentException.class, () -> r.reschedule(D2, D1));
    }

    @Test
    void testBarcodeGeneration() {
        String barcode = r.getBarcode();
        assertNotNull(barcode);
        assertTrue(barcode.length() > 0);
        // 檢查是否為有效的 Base64 字串
        assertDoesNotThrow(() -> Base64.getDecoder().decode(barcode));
    }

    @Test
    void testToString() {
        String str = r.toString();
        assertTrue(str.contains("reservationId="));
        assertTrue(str.contains("locker="));
        assertTrue(str.contains("user="));
        assertTrue(str.contains("startDate="));
        assertTrue(str.contains("endDate="));
        assertTrue(str.contains("barcode="));
    }
}
