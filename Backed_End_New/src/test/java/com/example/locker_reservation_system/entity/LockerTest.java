package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.dto.LockerUpdateRequest;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class LockerTest {

    private Locker locker;
    private Customer customer;
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
        
        customer = new Customer("test", "password", "1234567890");
        customer.setUserId(9L);
    }

    @Test
    void testIsAvailable() {
        assertTrue(locker.isAvailable(D1, D2));
        customer.reserve(locker, D1, D2);
        assertFalse(locker.isAvailable(D1, D2));
    }

    @Test
    void testReserveConflict() {
        customer.reserve(locker, D1, D2);
        Customer customer2 = new Customer("test2", "password2", "0987654321");
        customer2.setUserId(2L);
        
        assertThrows(RuntimeException.class, () -> customer2.reserve(locker, D1, D2));
    }

    @Test
    void testRelease() {
        customer.reserve(locker, D1, D2);
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
        Reservation r = customer.reserve(locker, D1, D2);
        assertThat(r).isNotNull();

        List<String> statuses = locker.getDateDetails().stream()
                .map(LockerDateDetail::getStatus)
                .collect(Collectors.toList());
        assertThat(statuses).containsOnly("occupied");
    }

    @Test
    void reschedule_success() {
        Reservation r = customer.reserve(locker, D1, D2);
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
        Reservation r = customer.reserve(locker, D1, D2);
        r.cancel();
        assertThat(locker.getDateDetails())
                .allMatch(d -> "available".equals(d.getStatus()));
    }

    @Test
    void testMarkDateRangeStatus() {
        locker.markDateRangeStatus(D1, D2, "maintenance");
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
        locker.markDateRangeStatus(D1, D1, "maintenance");
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

    @Test
    void testAdd() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        
        locker.add(2);
        
        assertThat(locker.getCapacity()).isEqualTo(2);
        assertThat(locker.getUsability()).isTrue();
    }

    @Test
    void testAdd_invalidCapacity() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        
        assertThatThrownBy(() -> locker.add(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Capacity must be greater than 0");
    }

    @Test
    void testAdd_negativeCapacity() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        
        assertThatThrownBy(() -> locker.add(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Capacity must be greater than 0");
    }

    @Test
    void testAdd_alreadyInitialized() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);
        
        locker.add(2);
        
        assertThat(locker.getCapacity()).isEqualTo(2);
        assertThat(locker.getUsability()).isTrue();
    }

    @Test
    void testUpdate() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);

        LockerUpdateRequest req = new LockerUpdateRequest();
        req.setCapacity(2);
        req.setStartDate(D1);
        req.setEndDate(D2);
        req.setStatus("maintenance");
        req.setMemo("Under maintenance");

        locker.update(req);

        assertThat(locker.getCapacity()).isEqualTo(2);
        assertThat(locker.getDateDetails())
            .allMatch(d -> "maintenance".equals(d.getStatus()));
        assertThat(locker.getDateDetails())
            .allMatch(d -> "Under maintenance".equals(d.getMemo()));
    }

    @Test
    void testUpdate_partialUpdate() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);

        // 只更新容量
        LockerUpdateRequest req1 = new LockerUpdateRequest();
        req1.setCapacity(2);
        locker.update(req1);
        assertThat(locker.getCapacity()).isEqualTo(2);

        // 只更新狀態
        LockerUpdateRequest req2 = new LockerUpdateRequest();
        req2.setStartDate(D1);
        req2.setEndDate(D2);
        req2.setStatus("maintenance");
        locker.update(req2);
        assertThat(locker.getDateDetails())
            .allMatch(d -> "maintenance".equals(d.getStatus()));

        // 只更新備註
        LockerUpdateRequest req3 = new LockerUpdateRequest();
        req3.setStartDate(D1);
        req3.setEndDate(D2);
        req3.setMemo("Test memo");
        locker.update(req3);
        assertThat(locker.getDateDetails())
            .allMatch(d -> "Test memo".equals(d.getMemo()));
    }

    @Test
    void testUpdate_invalidCapacity() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);

        LockerUpdateRequest req = new LockerUpdateRequest();
        req.setCapacity(0);

        locker.update(req);
        assertThat(locker.getCapacity()).isEqualTo(1); // 容量不應該被更新
    }

    @Test
    void testUpdate_noDateRange() {
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);

        LockerUpdateRequest req = new LockerUpdateRequest();
        req.setStatus("maintenance");
        req.setMemo("Test memo");

        locker.update(req);
        assertThat(locker.getDateDetails()).isEmpty(); // 不應該有任何日期詳情
    }
}
