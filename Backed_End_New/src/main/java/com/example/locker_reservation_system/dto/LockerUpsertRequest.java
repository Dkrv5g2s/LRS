// dto/LockerUpsertRequest.java  ── 管理員增修 Locker
package com.example.locker_reservation_system.dto;
import lombok.Data;

@Data
public class LockerUpsertRequest {
    private String  site;
    private Integer capacity;
    private Boolean usability;
}
