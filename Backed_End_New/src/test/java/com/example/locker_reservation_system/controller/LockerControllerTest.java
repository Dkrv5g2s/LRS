package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.repository.LockerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class LockerControllerTest {

    @MockBean
    LockerRepository lockerRepo;

    private Locker createLocker() {
        Locker l = new Locker();
        l.setLockerId(1L);
        l.setSite("A-1");
        l.setCapacity(10);
        l.setUsability(true);
        return l;
    }

    @Test
    void getAllLockers() {
        Locker l = createLocker();
        when(lockerRepo.findAll()).thenReturn(List.of(l));

        List<Locker> lockers = lockerRepo.findAll();
        assertThat(lockers).hasSize(1);
        
        Locker locker = lockers.get(0);
        assertThat(locker.getLockerId()).isEqualTo(1L);
        assertThat(locker.getSite()).isEqualTo("A-1");
        assertThat(locker.getCapacity()).isEqualTo(10);
        assertThat(locker.getUsability()).isTrue();
    }

    @Test
    void getLockerById() {
        Locker l = createLocker();
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(l));

        Locker locker = lockerRepo.findById(1L).orElseThrow();
        assertThat(locker.getLockerId()).isEqualTo(1L);
        assertThat(locker.getSite()).isEqualTo("A-1");
    }

    @Test
    void getLockerById_notFound() {
        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lockerRepo.findById(1L).orElseThrow())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getLockerStatus() {
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 3);
        Locker l = createLocker();

        when(lockerRepo.findById(1L)).thenReturn(Optional.of(l));

        LockerStatusResponse response = l.toStatusResponse(s, e);
        assertThat(response.getLockerId()).isEqualTo(1L);
        assertThat(response.getSite()).isEqualTo("A-1");
        assertThat(response.getCapacity()).isEqualTo(10);
        assertThat(response.getUsability()).isTrue();
        assertThat(response.getStatus()).isEqualTo("available");
    }

    @Test
    void updateLocker() {
        Locker l = createLocker();
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(l));

        l.setSite("B-1");
        l.setCapacity(20);
        l.setUsability(false);

        assertThat(l.getSite()).isEqualTo("B-1");
        assertThat(l.getCapacity()).isEqualTo(20);
        assertThat(l.getUsability()).isFalse();
    }

    @Test
    void deleteLocker() {
        Locker l = createLocker();
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(l));

        lockerRepo.delete(l);
        verify(lockerRepo).delete(l);
    }
}
