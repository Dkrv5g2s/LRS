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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationManageControllerTest {

    @Mock
    private ReservationRepository reservationRepo;
    @Mock
    private LockerRepository lockerRepo;
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ReservationManageController controller;

    private User adminUser;
    private User targetUser;
    private Locker locker;
    private Reservation reservation;
    private static final LocalDate D1 = LocalDate.of(2024, 1, 1);
    private static final LocalDate D2 = LocalDate.of(2024, 1, 3);

    @BeforeEach
    void setUp() {
        // Setup admin user
        adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setAccountName("admin");
        adminUser.setIsAdmin(true);

        // Setup target user
        targetUser = new User();
        targetUser.setUserId(2L);
        targetUser.setAccountName("user");
        targetUser.setIsAdmin(false);

        // Setup locker
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);
    }

    @Test
    void searchUsers_shouldReturnNonAdminUsers() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepo.findByAccountNameContainingIgnoreCase("user")).thenReturn(List.of(targetUser));

        List<User> result = controller.searchUsers("user", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(targetUser);
        verify(userRepo).findById(1L);
        verify(userRepo).findByAccountNameContainingIgnoreCase("user");
    }

    @Test
    void searchUsers_adminNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.searchUsers("user", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin user not found");
    }

    @Test
    void reserveForUser_shouldCreateReservation() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepo.findById(2L)).thenReturn(Optional.of(targetUser));
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));

        ReservationRequest req = new ReservationRequest();
        req.setLockerId(1L);
        req.setUserId(2L);
        req.setStartDate(D1);
        req.setEndDate(D2);

        Reservation result = controller.reserveForUser(req, 1L);

        assertThat(result.getLocker()).isEqualTo(locker);
        assertThat(result.getUser()).isEqualTo(targetUser);
        assertThat(result.getStartDate()).isEqualTo(D1);
        assertThat(result.getEndDate()).isEqualTo(D2);
        assertThat(result.getBarcode()).isNotNull();
        verify(userRepo).findById(1L);
        verify(userRepo).findById(2L);
        verify(lockerRepo).findById(1L);
    }

    @Test
    void getReservationsForUser_shouldReturnUserReservations() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepo.findById(2L)).thenReturn(Optional.of(targetUser));
        targetUser.getReservations().add(reservation);

        List<Reservation> result = controller.getReservationsForUser(2L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reservation);
        verify(userRepo).findById(1L);
        verify(userRepo).findById(2L);
    }

    @Test
    void updateReservationDateForUser_shouldUpdateDates() {
        // Create a reservation for testing
        Reservation testReservation = new Reservation(locker, targetUser, D1, D2);
        testReservation.setId(1L);
        targetUser.getReservations().add(testReservation);
        
        when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(testReservation));
        LocalDate newStart = LocalDate.of(2024, 1, 4);
        LocalDate newEnd = LocalDate.of(2024, 1, 5);

        Reservation result = controller.updateReservationDateForUser(1L, newStart, newEnd, 1L);

        assertThat(result.getStartDate()).isEqualTo(newStart);
        assertThat(result.getEndDate()).isEqualTo(newEnd);
        assertThat(result.getBarcode()).isNotNull();
        verify(userRepo).findById(1L);
        verify(reservationRepo).findById(1L);
    }

    @Test
    void cancelReservationForUser_shouldCancelReservation() {
        // Create a reservation for testing
        Reservation testReservation = new Reservation(locker, targetUser, D1, D2);
        testReservation.setId(1L);
        targetUser.getReservations().add(testReservation); // Add reservation to user's list
        
        when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(testReservation));

        controller.cancelReservationForUser(1L, 1L);

        verify(userRepo).findById(1L);
        verify(reservationRepo).findById(1L);
        verify(reservationRepo).delete(testReservation);
        assertThat(targetUser.getReservations()).doesNotContain(testReservation);
    }

    @Test
    void cancelReservationForUser_reservationNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
        when(reservationRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.cancelReservationForUser(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation not found");
    }
} 