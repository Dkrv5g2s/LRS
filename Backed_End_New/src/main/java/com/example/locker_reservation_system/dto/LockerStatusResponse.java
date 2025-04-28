// dto/LockerStatusResponse.java
package com.example.locker_reservation_system.dto;

import lombok.Data;

@Data
public class LockerStatusResponse {
    private long   lockerId;
    private String site;
    private int    capacity;
    private Boolean usability;

    /** 區段是否皆可用 */
    private String status;          // available / unavailable
    private String memo;            // 例如 "2025‑04‑28 : 維修"


}
