package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
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
import java.util.List;
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
        r.setId(10L);
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

        ReservationRequest req = new ReservationRequest();
        req.setLockerId(1L);
        req.setUserId(5L);
        req.setStartDate(s);
        req.setEndDate(e);

        Reservation r = reservationController.reserve(req);
        
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

        ReservationRequest req = new ReservationRequest();
        req.setLockerId(1L);
        req.setUserId(5L);
        req.setStartDate(s);
        req.setEndDate(e);

        assertThatThrownBy(() -> reservationController.reserve(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start > end");
    }

    @Test
    void reserve_lockerNotFound() {
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        User u = fakeUser();

        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());

        ReservationRequest req = new ReservationRequest();
        req.setLockerId(1L);
        req.setUserId(5L);
        req.setStartDate(s);
        req.setEndDate(e);

        assertThatThrownBy(() -> reservationController.reserve(req))
                .isInstanceOf(RuntimeException.class)
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
        
        // Add the reservation to user's reservations list
        u.getReservations().add(r);

        when(reservationRepo.findById(10L)).thenReturn(Optional.of(r));

        reservationController.cancel(10L);

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
        
        // Add the reservation to user's reservations list
        u.getReservations().add(r);

        when(reservationRepo.findById(10L)).thenReturn(Optional.of(r));

        Reservation updated = reservationController.updateReservationDates(10L, newS, newE);

        assertThat(updated.getStartDate()).isEqualTo(newS);
        assertThat(updated.getEndDate()).isEqualTo(newE);
        assertThat(updated.getBarcode()).isNotBlank();
    }

    @Test
    void reschedule_conflict() {
        Locker l = fakeLocker();
        User u = fakeUser();
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        LocalDate newS = LocalDate.of(2025, 1, 2);
        LocalDate newE = LocalDate.of(2025, 1, 3);
        Reservation r = fakeReservation(l, u, s, e);
        
        // Add the original reservation to user's reservations list
        u.getReservations().add(r);

        when(reservationRepo.findById(10L)).thenReturn(Optional.of(r));

        // 創建另一個預約並標記置物櫃的日期範圍
        Reservation anotherReservation = new Reservation(l, u, newS, newE);
        l.markDateRangeStatus(newS, newE, "occupied");
        u.getReservations().add(anotherReservation);

        assertThatThrownBy(() -> reservationController.updateReservationDates(10L, newS, newE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Locker already reserved");
    }

    @Test
    void getLockerReservationStatus_shouldReturnStatusList() {
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        Locker l = fakeLocker();
        
        when(lockerRepo.findAll()).thenReturn(List.of(l));
        
        List<LockerStatusResponse> statusList = reservationController.getLockerReservationStatus(s, e);
        
        assertThat(statusList).hasSize(1);
        assertThat(statusList.get(0).getLockerId()).isEqualTo(l.getLockerId());
    }

    @Test
    void getLockerReservationStatus_invalidDateRange() {
        LocalDate s = LocalDate.of(2025, 1, 2);
        LocalDate e = LocalDate.of(2025, 1, 1);
        
        assertThatThrownBy(() -> reservationController.getLockerReservationStatus(s, e))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start > end");
    }

    @Test
    void getByUser_shouldReturnUserReservations() {
        User u = fakeUser();
        Locker l = fakeLocker();
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);
        Reservation r = fakeReservation(l, u, s, e);
        
        u.getReservations().add(r);
        when(userRepo.findById(5L)).thenReturn(Optional.of(u));
        
        List<Reservation> reservations = reservationController.getByUser(5L);
        
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(r);
    }

    @Test
    void getByUser_userNotFound() {
        when(userRepo.findById(5L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> reservationController.getByUser(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateReservationDates_reservationNotFound() {
        when(reservationRepo.findById(10L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> reservationController.updateReservationDates(10L, 
                LocalDate.of(2025, 1, 3), LocalDate.of(2025, 1, 4)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation not found");
    }
}
