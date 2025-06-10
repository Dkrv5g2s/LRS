package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class UserTest {
    private User user;
    private Locker locker;
    private final LocalDate D1 = LocalDate.of(2024, 1, 1);
    private final LocalDate D2 = LocalDate.of(2024, 1, 3);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setAccountName("test");
        user.setPhoneNumber("1234567890");
        user.setEncryptedPassword(encoder.encode("mypw"));

        locker = new Locker();
        locker.setLockerId(1L);
        locker.setSite("A");
        locker.setCapacity(1);
        locker.setUsability(true);
    }

    @Test
    void password_encryption_and_verify() {
        assertThat(user.checkPassword("mypw")).isTrue();
        assertThat(user.checkPassword("wrong")).isFalse();
    }

    @Test
    void reserve_locker() {
        Reservation r = user.reserve(locker, D1, D2);
        assertThat(r).isNotNull();
        assertThat(user.getReservations()).contains(r);
        assertThat(r.getUser()).isEqualTo(user);
        assertThat(r.getLocker()).isEqualTo(locker);
    }

    @Test
    void reserve_invalid_date_range() {
        assertThrows(IllegalArgumentException.class, () -> user.reserve(locker, D2, D1));
    }

    @Test
    void reserve_unavailable_locker() {
        User user2 = new User();
        user2.setUserId(2L);
        user2.setAccountName("test2");
        user2.setPhoneNumber("0987654321");
        user2.reserve(locker, D1, D2);

        assertThrows(RuntimeException.class, () -> user.reserve(locker, D1, D2));
    }

    @Test
    void cancelReservation_shouldRemoveReservation() {
        Reservation r = user.reserve(locker, D1, D2);
        r.setId(1L);
        user.cancelReservation(r.getId());
        assertThat(user.getReservations()).doesNotContain(r);
        assertTrue(locker.isAvailable(D1, D2));
    }

    @Test
    void cancelReservation_notFound() {
        assertThatThrownBy(() -> user.cancelReservation(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation with ID 999 not found for this user");
    }

    @Test
    void updateReservationDates_shouldUpdateDates() {
        Reservation r = user.reserve(locker, D1, D2);
        r.setId(1L);
        LocalDate newStart = LocalDate.of(2024, 1, 4);
        LocalDate newEnd = LocalDate.of(2024, 1, 5);
        
        Reservation updated = user.updateReservationDates(r.getId(), newStart, newEnd);
        
        assertThat(updated.getStartDate()).isEqualTo(newStart);
        assertThat(updated.getEndDate()).isEqualTo(newEnd);
        assertTrue(locker.isAvailable(D1, D2));
        assertFalse(locker.isAvailable(newStart, newEnd));
    }

    @Test
    void updateReservationDates_notFound() {
        assertThatThrownBy(() -> user.updateReservationDates(999L, D1, D2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reservation with ID 999 not found for this user");
    }

    @Test
    void adminSearchCustomers_shouldReturnNonAdminUsers() {
        user.setIsAdmin(true);
        UserRepository userRepo = mock(UserRepository.class);
        List<User> allUsers = List.of(
            createUser(1L, "user1", false),
            createUser(2L, "user2", false),
            createUser(3L, "admin1", true)
        );
        
        when(userRepo.findByAccountNameContainingIgnoreCase("user")).thenReturn(allUsers);
        
        List<User> results = user.adminSearchCustomers("user", userRepo);
        
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(u -> !u.getIsAdmin());
    }

    @Test
    void adminSearchCustomers_notAdmin() {
        user.setIsAdmin(false);
        UserRepository userRepo = mock(UserRepository.class);
        
        assertThatThrownBy(() -> user.adminSearchCustomers("user", userRepo))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only administrators can search customers");
    }

    @Test
    void adminReserveLockerForUser_shouldCreateReservation() {
        user.setIsAdmin(true);
        User targetUser = createUser(2L, "user2", false);
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 2);
        
        Reservation r = user.adminReserveLockerForUser(targetUser, locker, start, end);
        
        assertThat(r).isNotNull();
        assertThat(r.getUser()).isEqualTo(targetUser);
        assertThat(r.getLocker()).isEqualTo(locker);
        assertThat(targetUser.getReservations()).contains(r);
    }

    @Test
    void adminReserveLockerForUser_notAdmin() {
        user.setIsAdmin(false);
        User targetUser = createUser(2L, "user2", false);
        
        assertThatThrownBy(() -> user.adminReserveLockerForUser(targetUser, locker, D1, D2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only administrators can reserve lockers for other users");
    }

    @Test
    void adminGetReservationsForUser_shouldReturnUserReservations() {
        user.setIsAdmin(true);
        User targetUser = createUser(2L, "user2", false);
        Reservation r = targetUser.reserve(locker, D1, D2);
        
        List<Reservation> reservations = user.adminGetReservationsForUser(targetUser);
        
        assertThat(reservations).hasSize(1);
        assertThat(reservations).contains(r);
    }

    @Test
    void adminGetReservationsForUser_notAdmin() {
        user.setIsAdmin(false);
        User targetUser = createUser(2L, "user2", false);
        
        assertThatThrownBy(() -> user.adminGetReservationsForUser(targetUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only administrators can view other users' reservations");
    }

    @Test
    void adminUpdateUserReservationDates_shouldUpdateDates() {
        user.setIsAdmin(true);
        User targetUser = createUser(2L, "user2", false);
        Reservation r = targetUser.reserve(locker, D1, D2);
        r.setId(1L);
        LocalDate newStart = LocalDate.of(2024, 1, 4);
        LocalDate newEnd = LocalDate.of(2024, 1, 5);
        
        Reservation updated = user.adminUpdateUserReservationDates(r, newStart, newEnd);
        
        assertThat(updated.getStartDate()).isEqualTo(newStart);
        assertThat(updated.getEndDate()).isEqualTo(newEnd);
    }

    @Test
    void adminUpdateUserReservationDates_notAdmin() {
        user.setIsAdmin(false);
        User targetUser = createUser(2L, "user2", false);
        Reservation r = targetUser.reserve(locker, D1, D2);
        
        assertThatThrownBy(() -> user.adminUpdateUserReservationDates(r, D1, D2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only administrators can update other users' reservations");
    }

    @Test
    void adminCancelUserReservation_shouldCancelReservation() {
        user.setIsAdmin(true);
        User targetUser = createUser(2L, "user2", false);
        Reservation r = targetUser.reserve(locker, D1, D2);
        r.setId(1L);
        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        
        user.adminCancelUserReservation(r, reservationRepo);
        
        assertThat(targetUser.getReservations()).doesNotContain(r);
        verify(reservationRepo).delete(r);
    }

    @Test
    void adminCancelUserReservation_notAdmin() {
        user.setIsAdmin(false);
        User targetUser = createUser(2L, "user2", false);
        Reservation r = targetUser.reserve(locker, D1, D2);
        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        
        assertThatThrownBy(() -> user.adminCancelUserReservation(r, reservationRepo))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only administrators can cancel other users' reservations");
    }

    private User createUser(Long id, String accountName, boolean isAdmin) {
        User u = new User();
        u.setUserId(id);
        u.setAccountName(accountName);
        u.setPhoneNumber("1234567890");
        u.setIsAdmin(isAdmin);
        return u;
    }
}
