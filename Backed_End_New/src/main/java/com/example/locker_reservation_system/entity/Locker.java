package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.dto.LockerUpdateRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity @Getter @Setter @NoArgsConstructor
public class Locker {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lockerId;

    private String  site;
    private Integer capacity;
    private Boolean usability;

    /* ==== 關聯 ==== */
    @OneToMany(mappedBy = "locker", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LockerDateDetail> dateDetails = new ArrayList<>();

    @Override
    public String toString() {
        return "Locker{" +
                "lockerId=" + lockerId +
                ", site='" + site + '\'' +
                ", capacity=" + capacity +
                ", usability=" + usability +
                '}';
    }

    /* ========= 主要業務行為 ========= */

    /** 回傳此區段是否全部可訂 */
    public boolean isAvailable(LocalDate start, LocalDate end) {
        return generateDateStream(start, end)
                .allMatch(d -> dateDetails.stream()
                        .filter(det -> det.getDate().equals(d))
                        .allMatch(det -> "available".equalsIgnoreCase(det.getStatus())));
    }

    /** 取消某筆預約 (由 Reservation 呼叫) */
    public void release(LocalDate start, LocalDate end) {
        markDateRangeStatus(start, end, "available");
    }

    /** 生成 LockerStatusResponse（for Controller） */
    public LockerStatusResponse toStatusResponse(LocalDate start, LocalDate end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LockerStatusResponse resp = new LockerStatusResponse();
        resp.setLockerId(lockerId);
        resp.setSite(site);
        resp.setCapacity(capacity);
        resp.setUsability(usability);

        boolean allAvailable = isAvailable(start, end);
        resp.setStatus(allAvailable ? "available" : "unavailable");

        // 彙整備註
        String memo = dateDetails.stream()
                .filter(d -> !d.getDate().isBefore(start) && !d.getDate().isAfter(end))
                .filter(d -> d.getMemo() != null && !d.getMemo().isBlank())
                .map(d -> d.getDate().format(fmt) + ": " + d.getMemo())
                .collect(Collectors.joining(", "));
        resp.setMemo(memo);

        return resp;
    }

    /* =========== 工具方法 =========== */

    public void markDateRangeStatus(LocalDate start, LocalDate end, String status) {
        generateDateStream(start, end).forEach(date -> {
            LockerDateDetail detail = dateDetails.stream()
                    .filter(d -> d.getDate().equals(date))
                    .findFirst()
                    .orElseGet(() -> {               // 若不存在則新建
                        LockerDateDetail nd = new LockerDateDetail();
                        nd.setLocker(this);
                        nd.setDate(date);
                        dateDetails.add(nd);
                        return nd;
                    });
            detail.setStatus(status);
        });
    }

    private Stream<LocalDate> generateDateStream(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1));     // inclusive
    }

    /** 更新日期範圍內的備註 */
    public void updateDateRangeMemo(LocalDate start, LocalDate end, String memo) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        generateDateStream(start, end).forEach(date -> {
            LockerDateDetail detail = dateDetails.stream()
                .filter(d -> d.getDate().equals(date))
                .findFirst()
                .orElseGet(() -> {
                    LockerDateDetail newDetail = new LockerDateDetail();
                    newDetail.setLocker(this);
                    newDetail.setDate(date);
                    newDetail.setStatus("available");
                    dateDetails.add(newDetail);
                    return newDetail;
                });
            detail.setMemo(memo);
        });
    }

    /** 新增置物櫃 */
    public void add(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.usability = true;
        this.capacity = capacity;
    }

    /** 更新置物櫃資訊 */
    public void update(LockerUpdateRequest req) {
        // 更新容量
        if (req.getCapacity() != null && req.getCapacity() > 0) {
            this.capacity = req.getCapacity();
        }

        // 更新日期範圍內的狀態和備註
        if (req.getStartDate() != null && req.getEndDate() != null && 
            (req.getStatus() != null || req.getMemo() != null)) {
            // 更新狀態
            if (req.getStatus() != null) {
                markDateRangeStatus(req.getStartDate(), req.getEndDate(), req.getStatus());
            }
            
            // 更新備註
            if (req.getMemo() != null) {
                updateDateRangeMemo(req.getStartDate(), req.getEndDate(), req.getMemo());
            }
        }
    }



}
