package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.dto.LockerUpdateRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.repository.LockerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LockerControllerTest {

    @Mock
    private LockerRepository lockerRepo;

    @InjectMocks
    private LockerController controller;

    private Locker locker;
    private final LocalDate D1 = LocalDate.of(2025, 1, 1);
    private final LocalDate D2 = LocalDate.of(2025, 1, 2);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(false);
    }

    @Test
    void addLocker_shouldAddLocker() {
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));
        when(lockerRepo.save(any(Locker.class))).thenReturn(locker);

        Locker result = controller.addLocker(1L, 2);

        verify(lockerRepo).findById(1L);
        verify(lockerRepo).save(locker);
        assertThat(result.getCapacity()).isEqualTo(2);
        assertThat(result.getUsability()).isTrue();
    }

    @Test
    void addLocker_invalidCapacity() {
        assertThatThrownBy(() -> controller.addLocker(1L, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Capacity must be greater than 0");
    }

    @Test
    void addLocker_lockerNotFound() {
        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.addLocker(1L, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Locker with ID 1 not found");
    }

    @Test
    void addLocker_alreadyInUse() {
        locker.setUsability(true);
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));

        assertThatThrownBy(() -> controller.addLocker(1L, 2))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Locker is already in use");
    }

    @Test
    void updateLocker_shouldUpdateLocker() {
        LockerUpdateRequest req = new LockerUpdateRequest();
        req.setCapacity(3);
        req.setStartDate(D1);
        req.setEndDate(D2);
        req.setStatus("maintenance");
        req.setMemo("Under maintenance");

        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));
        when(lockerRepo.save(any(Locker.class))).thenReturn(locker);

        Locker result = controller.updateLocker(1L, req);

        verify(lockerRepo).findById(1L);
        verify(lockerRepo).save(locker);
        assertThat(result.getCapacity()).isEqualTo(3);
    }

    @Test
    void updateLocker_invalidDateRange() {
        LockerUpdateRequest req = new LockerUpdateRequest();
        req.setStartDate(D2);
        req.setEndDate(D1);

        assertThatThrownBy(() -> controller.updateLocker(1L, req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start date must be before or equal to end date");
    }

    @Test
    void updateLocker_lockerNotFound() {
        LockerUpdateRequest req = new LockerUpdateRequest();
        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateLocker(1L, req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Locker with ID 1 not found");
    }

    @Test
    void deleteLocker_shouldDeleteLocker() {
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));
        when(lockerRepo.save(any(Locker.class))).thenReturn(locker);

        controller.deleteLocker(1L);

        verify(lockerRepo).findById(1L);
        verify(lockerRepo).save(locker);
        assertThat(locker.getUsability()).isFalse();
    }

    @Test
    void deleteLocker_lockerNotFound() {
        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.deleteLocker(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Locker with ID 1 not found");
    }

    @Test
    void deleteLocker_hasReservations() {
        locker.setUsability(true);
        locker.markDateRangeStatus(D1, D2, "occupied");
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));

        assertThatThrownBy(() -> controller.deleteLocker(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot delete locker - some dates are still reserved");
    }

    @Test
    void getLockerStatus_shouldReturnStatus() {
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));

        LockerStatusResponse response = controller.getLockerStatus(1L, D1, D2);

        verify(lockerRepo).findById(1L);
        assertThat(response).isNotNull();
        assertThat(response.getLockerId()).isEqualTo(1L);
    }

    @Test
    void getLockerStatus_invalidDateRange() {
        assertThatThrownBy(() -> controller.getLockerStatus(1L, D2, D1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start date must be before or equal to end date");
    }

    @Test
    void getLockerStatus_lockerNotFound() {
        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getLockerStatus(1L, D1, D2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Locker with ID 1 not found");
    }
}
