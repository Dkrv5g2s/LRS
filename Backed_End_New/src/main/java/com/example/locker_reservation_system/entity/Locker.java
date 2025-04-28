package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
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

    @OneToMany(mappedBy = "locker", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

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
                .noneMatch(d -> dateDetails.stream()
                        .anyMatch(det -> det.getDate().equals(d)
                                && !"available".equalsIgnoreCase(det.getStatus())));
    }

    /** 取消某筆預約 (由 Reservation 呼叫) */
    public void release(LocalDate start, LocalDate end) {
        markDateRange(start, end, "available");
    }

    /** 修改預約日期；會做衝突檢查 */
    public void reschedule(Reservation r, LocalDate newStart, LocalDate newEnd) {
        // 先釋放原日期
        release(r.getStartDate(), r.getEndDate());

        // 檢查新區段是否可用
        if (!isAvailable(newStart, newEnd)) {
            // 回滾原日期
            markDateRange(r.getStartDate(), r.getEndDate(), "occupied");
            throw new RuntimeException("Locker already reserved in new period");
        }

        // 標記新區段並更新 Reservation
        markDateRange(newStart, newEnd, "occupied");
        r.setStartDate(newStart);
        r.setEndDate(newEnd);
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

    public void markDateRange(LocalDate start, LocalDate end, String status) {
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



    // entity/Locker.java  新增 / 修改片段

//    /* 工廠：由管理員建立 */
//    public static Locker create(String site, Integer cap, Boolean usable) {
//        Locker l = new Locker();
//        l.site      = site;
//        l.capacity  = cap;
//        l.usability = usable;
//        return l;
//    }
//
//    /* 基本資料異動 */
//    public void updateBasicInfo(String site, Integer cap, Boolean usable) {
//        if (site != null)      this.site      = site;
//        if (cap  != null)      this.capacity  = cap;
//        if (usable != null)    this.usability = usable;
//    }
//
//    /* 管理員手動覆寫某天狀態／備註 */
//    public void overrideDateDetail(LocalDate date, String status, String memo) {
//        LockerDateDetail d = dateDetails.stream()
//                .filter(dd -> dd.getDate().equals(date))
//                .findFirst()
//                .orElseGet(() -> {
//                    LockerDateDetail nd = new LockerDateDetail();
//                    nd.setLocker(this);
//                    nd.setDate(date);
//                    dateDetails.add(nd);
//                    return nd;
//                });
//        if (status != null) d.setStatus(status);
//        if (memo   != null) d.setMemo(memo);
//    }

}
