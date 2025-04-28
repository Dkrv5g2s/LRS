package com.example.locker_reservation_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity @Getter @Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"date", "locker_id"}))
public class LockerDateDetail {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lockerDateDetailId;

    @ManyToOne(fetch = FetchType.LAZY)               // 留意 Lazy
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    /* available / occupied / maintenance… */
    private String status;

    private String memo;

    public void setLocker(Locker locker) {
        this.locker = locker;
        if (locker != null && !locker.getDateDetails().contains(this)) {
            locker.getDateDetails().add(this);
        }
    }

    @Override
    public String toString() {
        return "LockerDateDetail{" +
                "lockerDateDetailId=" + lockerDateDetailId +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", memo='" + memo + '\'' +
                '}';
    }
}
