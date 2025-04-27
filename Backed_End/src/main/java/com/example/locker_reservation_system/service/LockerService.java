package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.LockerDateDetail;
import com.example.locker_reservation_system.repository.LockerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LockerService {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private LockerDateDetailService lockerDateDetailService;

    /* 依區段回傳所有 Locker 的狀態 */
    public List<LockerStatusResponse> getLockerStatusByDateRange(LocalDate startDate,
                                                                 LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be greater than endDate");
        }

        List<Locker> lockers = lockerRepository.findAll();
        List<LockerStatusResponse> responses = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Locker locker : lockers) {

            List<LockerDateDetail> details =
                    lockerDateDetailService.findByLockerIdAndDateBetween(
                            locker.getLockerId(), startDate, endDate);

            LockerStatusResponse resp = new LockerStatusResponse();
            resp.setLockerId(locker.getLockerId());
            resp.setSite(locker.getSite());
            resp.setCapacity(locker.getCapacity());
            resp.setUsability(locker.getUsability());

            boolean allAvailable = true;
            StringBuilder memoBuilder = new StringBuilder();

            for (LockerDateDetail d : details) {
                if (!"available".equalsIgnoreCase(d.getStatus())) {
                    allAvailable = false;
                }

                if (d.getMemo() != null && !d.getMemo().isEmpty()) {
                    memoBuilder.append(d.getDate().format(fmt))
                            .append(": ")
                            .append(d.getMemo())
                            .append(", ");
                }
            }

            resp.setStatus(allAvailable ? "available" : "unavailable");

            String memo = memoBuilder.toString().trim();
            if (!memo.isEmpty()) {
                // 去掉最後一個逗號
                resp.setMemo(memo.substring(0, memo.length() - 1));
            }

            responses.add(resp);
        }

        return responses;
    }

    public Optional<Locker> findLockerById(long lockerId) {
        return lockerRepository.findById(lockerId);
    }
}
