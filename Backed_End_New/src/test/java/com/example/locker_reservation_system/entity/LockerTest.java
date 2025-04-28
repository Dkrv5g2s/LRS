package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);
        
        user = new User();
        user.setUserId(9L);
        user.setAccountName("test");
        user.setPhoneNumber("1234567890");
    }

    @Test
    void testIsAvailable() {
        assertTrue(locker.isAvailable(D1, D2));
        user.reserve(locker, D1, D2);
        assertFalse(locker.isAvailable(D1, D2));
    }

    @Test
    void testReserveConflict() {
        user.reserve(locker, D1, D2);
        User user2 = new User();
        user2.setUserId(2L);
        user2.setAccountName("test2");
        user2.setPhoneNumber("0987654321");
        
        assertThrows(RuntimeException.class, () -> user2.reserve(locker, D1, D2));
    }

    @Test
    void testRelease() {
        user.reserve(locker, D1, D2);
        assertFalse(locker.isAvailable(D1, D2));
        locker.release(D1, D2);
        assertTrue(locker.isAvailable(D1, D2));
    }

    @Test
    void isAvailable_noRecord() {
        assertThat(locker.isAvailable(D1, D3)).isTrue();
    }

    @Test
    void reserve_and_dateDetailMarked() {
        Reservation r = user.reserve(locker, D1, D2);
        assertThat(r).isNotNull();

        List<String> statuses = locker.getDateDetails().stream()
                .map(LockerDateDetail::getStatus)
                .collect(Collectors.toList());
        assertThat(statuses).containsOnly("occupied");
    }

    @Test
    void reschedule_success() {
        Reservation r = user.reserve(locker, D1, D2);
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
        Reservation r = user.reserve(locker, D1, D2);
        r.cancel();
        assertThat(locker.getDateDetails())
                .allMatch(d -> "available".equals(d.getStatus()));
    }

    @Test
    void testMarkDateRange() {
        locker.markDateRange(D1, D2, "maintenance");
        assertThat(locker.getDateDetails())
                .allMatch(d -> "maintenance".equals(d.getStatus()));
    }

    @Test
    void testToStatusResponse() {
        LockerStatusResponse response = locker.toStatusResponse(D1, D2);
        assertThat(response.getLockerId()).isEqualTo(locker.getLockerId());
        assertThat(response.getSite()).isEqualTo(locker.getSite());
        assertThat(response.getCapacity()).isEqualTo(locker.getCapacity());
        assertThat(response.getUsability()).isEqualTo(locker.getUsability());
        assertThat(response.getStatus()).isEqualTo("available");
        assertThat(response.getMemo()).isEmpty();

        // 添加備註後再次測試
        locker.markDateRange(D1, D1, "maintenance");
        LockerDateDetail detail = locker.getDateDetails().get(0);
        detail.setMemo("維修中");
        
        response = locker.toStatusResponse(D1, D2);
        assertThat(response.getStatus()).isEqualTo("unavailable");
        assertThat(response.getMemo()).contains("維修中");
    }

    @Test
    void testToString() {
        String str = locker.toString();
        assertThat(str).contains("lockerId=" + locker.getLockerId());
        assertThat(str).contains("site='" + locker.getSite() + "'");
        assertThat(str).contains("capacity=" + locker.getCapacity());
        assertThat(str).contains("usability=" + locker.getUsability());
    }
}
