package com.example.locker_reservation_system.controller;

import com.example.locker_reservation_system.repository.LockerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LockerControllerTest {

    @Mock
    private LockerRepository lockerRepo;

    @InjectMocks
    private LockerController lockerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
