package com.example.locker_reservation_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
public class LockerReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LockerReservationSystemApplication.class, args);
	}

}
