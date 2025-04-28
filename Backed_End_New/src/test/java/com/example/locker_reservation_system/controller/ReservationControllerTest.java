package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.LockerRepository;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    ReservationRepository reservationRepo;
    @Mock
    LockerRepository lockerRepo;
    @Mock
    UserRepository userRepo;

    @InjectMocks
    ReservationController reservationController;

    /* ===== helper ===== */
    private Locker fakeLocker() {
        Locker l = new Locker();
        l.setLockerId(1L);
        l.setSite("A");
        l.setCapacity(1);
        l.setUsability(true);
        return l;
    }

    private User fakeUser() {
        User u = new User();
        u.setUserId(5L);
        u.setAccountName("test");
        u.setPhoneNumber("1234567890");
        return u;
    }

    private Reservation fakeReservation(Locker l, User u, LocalDate s, LocalDate e) {
        Reservation r = new Reservation(l, u, s, e);
        r.setReservationId(10L);
        return r;
    }

    @BeforeEach
    void setUp() {
        // 初始化測試數據
    }

    /* ===== 新增預約 ===== */
    @Test
    void reserve_shouldCreateReservation() {
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);

        Locker l = fakeLocker();
        User u = fakeUser();

        when(lockerRepo.findById(1L)).thenReturn(Optional.of(l));
        when(userRepo.findById(5L)).thenReturn(Optional.of(u));

        Reservation r = u.reserve(l, s, e);
        
        assertThat(r.getLocker()).isEqualTo(l);
        assertThat(r.getUser()).isEqualTo(u);
        assertThat(r.getStartDate()).isEqualTo(s);
        assertThat(r.getEndDate()).isEqualTo(e);
        assertThat(r.getBarcode()).isNotBlank();
    }

    @Test
    void reserve_invalidDateRange() {
        LocalDate s = LocalDate.of(2025, 1, 2);
        LocalDate e = LocalDate.of(2025, 1, 1);

        Locker l = fakeLocker();
        User u = fakeUser();

        when(lockerRepo.findById(1L)).thenReturn(Optional.of(l));
        when(userRepo.findById(5L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> u.reserve(l, s, e))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start > end");
    }

    @Test
    void reserve_lockerNotFound() {
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        User u = fakeUser();

        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());
        when(userRepo.findById(5L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> {
            Locker locker = lockerRepo.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Locker not found"));
            u.reserve(locker, s, e);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Locker not found");
    }

    /* ===== 取消 ===== */
    @Test
    void cancel_shouldRemoveReservation() {
        Locker l = fakeLocker();
        User u = fakeUser();
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        Reservation r = fakeReservation(l, u, s, e);

        when(reservationRepo.findById(10L)).thenReturn(Optional.of(r));

        r.cancel();
        reservationRepo.delete(r);

        verify(reservationRepo).delete(r);
        assertThat(u.getReservations()).doesNotContain(r);
    }

    @Test
    void cancel_reservationNotFound() {
        when(reservationRepo.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            Reservation r = reservationRepo.findById(10L)
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));
            r.cancel();
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Reservation not found");
    }

    /* ===== 重新排程 ===== */
    @Test
    void reschedule_success() {
        Locker l = fakeLocker();
        User u = fakeUser();
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        LocalDate newS = LocalDate.of(2025, 1, 3);
        LocalDate newE = LocalDate.of(2025, 1, 4);
        Reservation r = fakeReservation(l, u, s, e);

        when(reservationRepo.findById(10L)).thenReturn(Optional.of(r));

        r.reschedule(newS, newE);

        assertThat(r.getStartDate()).isEqualTo(newS);
        assertThat(r.getEndDate()).isEqualTo(newE);
        assertThat(r.getBarcode()).isNotBlank();
    }

    @Test
    void reschedule_conflict() {
        Locker l = fakeLocker();
        User u = fakeUser();
        User u2 = new User();
        u2.setUserId(6L);
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        LocalDate newS = LocalDate.of(2025, 1, 2);
        LocalDate newE = LocalDate.of(2025, 1, 3);
        Reservation r = fakeReservation(l, u, s, e);
        Reservation r2 = u2.reserve(l, newS, newE);

        when(reservationRepo.findById(10L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> r.reschedule(newS, newE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Locker already reserved");
    }
}
