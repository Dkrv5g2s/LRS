// dto/RegisterRequest.java
package com.example.locker_reservation_system.dto;
import lombok.Data;
@Data
public class RegisterRequest {
    private String accountName;
    private String password;
    private String phoneNumber;
}
