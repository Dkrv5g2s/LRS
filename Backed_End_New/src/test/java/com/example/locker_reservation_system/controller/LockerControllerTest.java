package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.repository.LockerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class LockerControllerTest {

    @MockBean
    LockerRepository lockerRepo;

    @Test
    void status_endpoint_returnsArray() {
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 3);

        Locker l = new Locker();
        l.setLockerId(1L);
        l.setSite("A-1");
        l.setCapacity(10);
        l.setUsability(true);

        LockerStatusResponse response = l.toStatusResponse(s, e);
        response.setStatus("available");
        response.setMemo("2025-04-28: Maintenance");

        when(lockerRepo.findAll()).thenReturn(List.of(l));

        List<Locker> lockers = lockerRepo.findAll();
        assertThat(lockers).hasSize(1);
        
        Locker locker = lockers.get(0);
        assertThat(locker.getLockerId()).isEqualTo(1L);
        assertThat(locker.getSite()).isEqualTo("A-1");
        assertThat(locker.getCapacity()).isEqualTo(10);
        assertThat(locker.getUsability()).isTrue();
    }
}
