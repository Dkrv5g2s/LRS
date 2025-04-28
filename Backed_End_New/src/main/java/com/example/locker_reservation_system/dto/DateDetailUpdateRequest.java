// dto/DateDetailUpdateRequest.java ── 管理員調整單日狀態／備註
package com.example.locker_reservation_system.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DateDetailUpdateRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String    status;
    private String    memo;
}
