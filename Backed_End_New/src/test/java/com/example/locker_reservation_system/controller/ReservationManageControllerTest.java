package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Administrator;
import com.example.locker_reservation_system.entity.Customer;
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
    private UserRepository userRepo;
    @Mock
    private LockerRepository lockerRepo;
    @Mock
    private ReservationRepository reservationRepo;

    @InjectMocks
    private ReservationManageController controller;

    private Administrator admin;
    private Customer customer;
    private Locker locker;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        admin = new Administrator("admin", "adminpw", "0987654321");
        admin.setUserId(1L);
        
        customer = new Customer("customer", "customerpw", "1234567890");
        customer.setUserId(2L);
        
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);
        
        // 創建測試用的預約，但不將其添加到用戶的預約列表中，也不標記置物櫃的日期
        LocalDate startDate = LocalDate.now().plusDays(10);  // 使用較遠的日期
        LocalDate endDate = startDate.plusDays(1);
        reservation = new Reservation(locker, customer, startDate, endDate);
        reservation.setId(1L);
    }

    @Test
    void searchUsers_shouldReturnNonAdminUsers() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findByAccountNameContainingIgnoreCase("customer")).thenReturn(List.of(customer));

        List<User> result = controller.searchUsers("customer", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(customer);
        verify(userRepo).findByAccountNameContainingIgnoreCase("customer");
    }

    @Test
    void searchUsers_adminNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.searchUsers("customer", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin user not found");
    }

    @Test
    void reserveForUser_shouldCreateReservation() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(customer));
        when(lockerRepo.findById(1L)).thenReturn(Optional.of(locker));

        assertThat(locker.isAvailable(startDate, endDate)).isTrue();

        ReservationRequest request = new ReservationRequest();
        request.setLockerId(1L);
        request.setUserId(2L);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        Reservation result = controller.reserveForUser(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getLocker()).isEqualTo(locker);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getBarcode()).isNotBlank();
    }

    @Test
    void getReservationsForUser_shouldReturnUserReservations() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(customer));
        customer.getReservations().add(reservation);

        List<Reservation> result = controller.getReservationsForUser(2L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reservation);
    }

    @Test
    void updateReservationDateForUser_shouldUpdateDates() {
        LocalDate newStartDate = LocalDate.now().plusDays(2);
        LocalDate newEndDate = newStartDate.plusDays(1);

        // 確保預約被添加到用戶的預約列表中
        customer.getReservations().add(reservation);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(reservation));

        // 確保置物櫃在新日期範圍內是可用的
        assertThat(locker.isAvailable(newStartDate, newEndDate)).isTrue();

        Reservation result = controller.updateReservationDateForUser(1L, newStartDate, newEndDate, 1L);

        assertThat(result.getStartDate()).isEqualTo(newStartDate);
        assertThat(result.getEndDate()).isEqualTo(newEndDate);
        assertThat(result.getBarcode()).isNotBlank();
    }

    @Test
    void cancelReservationForUser_shouldCancelReservation() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(reservation));
        customer.getReservations().add(reservation);

        controller.cancelReservationForUser(1L, 1L);

        verify(reservationRepo).delete(reservation);
        assertThat(customer.getReservations()).doesNotContain(reservation);
    }

    @Test
    void reserveForUser_adminNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest();
        request.setLockerId(1L);
        request.setUserId(2L);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> controller.reserveForUser(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin user not found");
    }

    @Test
    void reserveForUser_customerNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest();
        request.setLockerId(1L);
        request.setUserId(2L);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> controller.reserveForUser(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Target User not found");
    }

    @Test
    void reserveForUser_lockerNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(customer));
        when(lockerRepo.findById(1L)).thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest();
        request.setLockerId(1L);
        request.setUserId(2L);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> controller.reserveForUser(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Locker not found");
    }

    @Test
    void getReservationsForUser_adminNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getReservationsForUser(2L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin user not found");
    }

    @Test
    void getReservationsForUser_customerNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getReservationsForUser(2L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Target User not found");
    }

    @Test
    void updateReservationDateForUser_adminNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateReservationDateForUser(1L, 
                LocalDate.now(), LocalDate.now().plusDays(1), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin user not found");
    }

    @Test
    void updateReservationDateForUser_reservationNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(reservationRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateReservationDateForUser(1L, 
                LocalDate.now(), LocalDate.now().plusDays(1), 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation not found");
    }

    @Test
    void cancelReservationForUser_adminNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.cancelReservationForUser(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin user not found");
    }

    @Test
    void cancelReservationForUser_reservationNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));
        when(reservationRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.cancelReservationForUser(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation not found");
    }
} 