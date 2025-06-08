// dto/LockerUpsertRequest.java  ── 管理員增修 Locker
package com.example.locker_reservation_system.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LockerUpdateRequest {
    private Integer capacity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    /** 設定dateDetail中上面時間段的status和memo */
    private String status;          // available / unavailable
    private String memo;            // 例如 "2025‑04‑28 : 維修"
}
