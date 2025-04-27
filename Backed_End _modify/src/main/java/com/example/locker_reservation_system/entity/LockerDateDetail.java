package com.example.locker_reservation_system.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat; // 引入 JsonFormat

import java.util.Date;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"date", "locker_id"}))
public class LockerDateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lockerDateDetailId;

    @ManyToOne
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // 設置日期格式
    private Date date;

    private String status;

    private String memo;
}
