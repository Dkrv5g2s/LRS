package com.example.locker_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Lob
    @Column(name = "barcode")
    private String barcode;

    /* ====== 建構器供 Locker 呼叫 ====== */
    public Reservation(Locker locker, User user, LocalDate start, LocalDate end) {
        this.locker = locker;
        this.user = user;
        this.startDate = start;
        this.endDate = end;
        regenerateBarcode();
    }

    /* ====== 行為 ====== */
    /** 取消預約 */
    public void cancel() {
        locker.release(startDate, endDate);
        // 移除關聯
        user.getReservations().remove(this);
        locker.getReservations().remove(this);
    }

    /* 重新產生條碼 (給 reschedule 用) */
    public void regenerateBarcode() {
        String raw = locker.getLockerId() + "-" + user.getUserId() + "-" + startDate + "-" + endDate;
        this.barcode = com.example.locker_reservation_system.util.BarcodeUtil.generateBase64(raw);
    }

    /* 調整日期 */
    public void reschedule(LocalDate newStart, LocalDate newEnd) {
        if (newStart.isAfter(newEnd)) throw new IllegalArgumentException("start > end");
        
        // 先釋放原日期
        locker.release(startDate, endDate);

        // 檢查新區段是否可用
        if (!locker.isAvailable(newStart, newEnd)) {
            // 回滾原日期
            locker.markDateRange(startDate, endDate, "occupied");
            throw new RuntimeException("Locker already reserved in new period");
        }

        // 標記新區段並更新日期
        locker.markDateRange(newStart, newEnd, "occupied");
        this.startDate = newStart;
        this.endDate = newEnd;
        regenerateBarcode();
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", locker=" + locker +
                ", user=" + user +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", barcode='" + barcode + '\'' +
                '}';
    }
}
