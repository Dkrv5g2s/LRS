package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.LockerDateDetail;
import com.example.locker_reservation_system.repository.LockerDateDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LockerDateDetailService {

    @Autowired private LockerDateDetailRepository repo;

    /* 依日期區段查詢 */
    public List<LockerDateDetail> findByLockerIdAndDateBetween(long lockerId,
                                                               LocalDate start,
                                                               LocalDate end) {
        return repo.findByLockerLockerIdAndDateBetween(lockerId, start, end);
    }

    public Optional<LockerDateDetail> findByLockerAndDate(long lockerId, LocalDate date) {
        return repo.findByLockerLockerIdAndDate(lockerId, date);
    }

    public void updateLockerDateDetail(LockerDateDetail detail) {
        repo.save(detail);
    }

    /* 主要：依區段批次更新 */
    public void updateLockerDateDetails(Locker locker,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        String status) {

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LockerDateDetail detail = findByLockerAndDate(locker.getLockerId(), current)
                    .orElse(new LockerDateDetail());

            detail.setLocker(locker);
            detail.setDate(current);
            detail.setStatus(status);
            updateLockerDateDetail(detail);

            current = current.plusDays(1);
        }
    }
}
