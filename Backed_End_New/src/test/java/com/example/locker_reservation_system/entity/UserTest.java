package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private ReservationRepository reservationRepo;

    private Customer customer;
    private Administrator admin;
    private Locker locker;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        customer = new Customer("test", "password", "1234567890");
        customer.setUserId(1L);
        
        admin = new Administrator("admin", "adminpw", "0987654321");
        admin.setUserId(2L);
        
        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);
        
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = startDate.plusDays(1);
        reservation = new Reservation(locker, customer, startDate, endDate);
        reservation.setId(1L);
    }

    @Test
    void reserve_shouldCreateReservation() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);

        Reservation result = customer.reserve(locker, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.getLocker()).isEqualTo(locker);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getBarcode()).isNotBlank();
        assertThat(customer.getReservations()).contains(result);
    }

    @Test
    void reserve_invalidDateRange() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now();

        assertThatThrownBy(() -> customer.reserve(locker, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start > end");
    }

    @Test
    void reserve_lockerNotAvailable() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);
        Customer otherCustomer = new Customer("other", "password", "1111111111");
        otherCustomer.reserve(locker, startDate, endDate);

        assertThatThrownBy(() -> customer.reserve(locker, startDate, endDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Locker already reserved");
    }

    @Test
    void cancelReservation_shouldRemoveReservation() {
        customer.getReservations().add(reservation);

        customer.cancelReservation(reservation.getId());

        assertThat(customer.getReservations()).doesNotContain(reservation);
        assertThat(locker.isAvailable(reservation.getStartDate(), reservation.getEndDate())).isTrue();
    }

    @Test
    void cancelReservation_notFound() {
        assertThatThrownBy(() -> customer.cancelReservation(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation with ID 999 not found");
    }

    @Test
    void updateReservationDates_shouldUpdateDates() {
        LocalDate newStartDate = LocalDate.now().plusDays(2);
        LocalDate newEndDate = newStartDate.plusDays(1);

        customer.getReservations().add(reservation);

        Reservation result = customer.updateReservationDates(reservation.getId(), newStartDate, newEndDate);

        assertThat(result.getStartDate()).isEqualTo(newStartDate);
        assertThat(result.getEndDate()).isEqualTo(newEndDate);
        assertThat(result.getBarcode()).isNotBlank();
    }

    @Test
    void updateReservationDates_notFound() {
        assertThatThrownBy(() -> customer.updateReservationDates(999L, 
                LocalDate.now(), LocalDate.now().plusDays(1)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation with ID 999 not found");
    }

    @Test
    void adminSearchCustomers_shouldReturnNonAdminUsers() {
        Customer customer1 = new Customer("customer1", "password", "1111111111");
        Customer customer2 = new Customer("customer2", "password", "2222222222");
        when(userRepo.findByAccountNameContainingIgnoreCase("customer"))
                .thenReturn(List.of(customer1, customer2));

        List<User> result = admin.adminSearchCustomers("customer", userRepo);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(customer1, customer2);
    }

    @Test
    void adminReserveLockerForUser_shouldCreateReservation() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);

        Reservation result = admin.adminReserveLockerForUser(customer, locker, startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.getLocker()).isEqualTo(locker);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getBarcode()).isNotBlank();
        assertThat(customer.getReservations()).contains(result);
    }

    @Test
    void adminGetReservationsForUser_shouldReturnUserReservations() {
        customer.getReservations().add(reservation);

        List<Reservation> result = admin.adminGetReservationsForUser(customer);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reservation);
    }

    @Test
    void adminUpdateUserReservationDates_shouldUpdateDates() {
        LocalDate newStartDate = LocalDate.now().plusDays(2);
        LocalDate newEndDate = newStartDate.plusDays(1);

        customer.getReservations().add(reservation);

        Reservation result = admin.adminUpdateUserReservationDates(reservation, newStartDate, newEndDate);

        assertThat(result.getStartDate()).isEqualTo(newStartDate);
        assertThat(result.getEndDate()).isEqualTo(newEndDate);
        assertThat(result.getBarcode()).isNotBlank();
    }

    @Test
    void adminCancelUserReservation_shouldCancelReservation() {
        customer.getReservations().add(reservation);

        admin.adminCancelUserReservation(reservation, reservationRepo);

        assertThat(customer.getReservations()).doesNotContain(reservation);
        verify(reservationRepo).delete(reservation);
    }
}
