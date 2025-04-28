// dto/LoginRequest.java
package com.example.locker_reservation_system.dto;
import lombok.Data;
@Data
public class LoginRequest {
    private String accountName;
    private String password;
}
