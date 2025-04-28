package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.LockerRepository;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class ReservationControllerTest {

    @MockBean
    ReservationRepository reservationRepo;
    @MockBean
    LockerRepository lockerRepo;
    @MockBean
    UserRepository userRepo;

    /* ===== helper ===== */
    private Locker fakeLocker() {
        Locker l = new Locker();
        l.setLockerId(1L);
        return l;
    }

    private User fakeUser() {
        User u = new User();
        u.setUserId(5L);
        return u;
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

        Reservation r = l.reserve(u, s, e);
        
        assertThat(r.getLocker()).isEqualTo(l);
        assertThat(r.getUser()).isEqualTo(u);
        assertThat(r.getStartDate()).isEqualTo(s);
        assertThat(r.getEndDate()).isEqualTo(e);
        assertThat(r.getBarcode()).isNotBlank();
    }

    /* ===== 取消 ===== */
    @Test
    void cancel_shouldRemoveReservation() {
        // 創建必要的實體
        Locker l = fakeLocker();
        User u = fakeUser();
        LocalDate s = LocalDate.of(2025, 1, 1);
        LocalDate e = LocalDate.of(2025, 1, 2);

        // 創建預約並設置關聯
        Reservation r = new Reservation(l, u, s, e);
        r.setReservationId(10L);

        // 設置 mock 行為
        when(reservationRepo.findById(10L)).thenReturn(Optional.of(r));

        // 執行取消操作
        r.cancel();
        reservationRepo.delete(r);

        // 驗證
        verify(reservationRepo).delete(r);
        assertThat(l.getReservations()).doesNotContain(r);
        assertThat(u.getReservations()).doesNotContain(r);
    }
}
