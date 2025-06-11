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
    private Customer customer;
    private Reservation r;

    @BeforeEach
    void setUp() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);

        customer = new Customer("test", "password", "1234567890");
        customer.setUserId(1L);

        r = customer.reserve(locker, D1, D2);
    }

    @Test
    void testConstructor() {
        Reservation reservation = new Reservation(locker, customer, D1, D2);
        assertEquals(locker, reservation.getLocker());
        assertEquals(customer, reservation.getCustomer());
        assertEquals(D1, reservation.getStartDate());
        assertEquals(D2, reservation.getEndDate());
        assertNotNull(reservation.getBarcode());
    }

    @Test
    void testCancel() {
        assertTrue(customer.getReservations().contains(r));
        r.cancel();
        assertTrue(locker.isAvailable(D1, D2));
        assertFalse(customer.getReservations().contains(r));
    }

    @Test
    void testReschedule() {
        LocalDate newStartDate = LocalDate.now().plusDays(5);
        LocalDate newEndDate = LocalDate.now().plusDays(7);
        r.reschedule(newStartDate, newEndDate);
        assertEquals(newStartDate, r.getStartDate());
        assertEquals(newEndDate, r.getEndDate());
        assertFalse(locker.isAvailable(newStartDate, newEndDate));
    }

    @Test
    void testRescheduleConflict() {
        LocalDate newStartDate = LocalDate.now().plusDays(1);
        LocalDate newEndDate = LocalDate.now().plusDays(3);
        Reservation anotherReservation = new Reservation(locker, customer, newStartDate, newEndDate);
        locker.markDateRangeStatus(newStartDate, newEndDate, "occupied");
        customer.getReservations().add(anotherReservation);
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
        assertDoesNotThrow(() -> Base64.getDecoder().decode(barcode));
    }

    @Test
    void testRegenerateBarcode() {
        String originalBarcode = r.getBarcode();
        r.regenerateBarcode();
        assertNotNull(r.getBarcode());
        assertNotEquals(originalBarcode, r.getBarcode());
    }

    @Test
    void testReschedule_conflict() {
        Customer customer2 = new Customer("test2", "password2", "0987654321");
        customer2.setUserId(2L);
        Reservation conflictReservation = new Reservation(locker, customer2, D3, D3);
        assertThrows(RuntimeException.class, () -> r.reschedule(D3, D3));
        assertEquals(D1, r.getStartDate());
        assertEquals(D2, r.getEndDate());
    }

    @Test
    void testReschedule_invalidDateRange() {
        assertThrows(IllegalArgumentException.class, () -> r.reschedule(D2, D1));
        assertEquals(D1, r.getStartDate());
        assertEquals(D2, r.getEndDate());
    }
}
