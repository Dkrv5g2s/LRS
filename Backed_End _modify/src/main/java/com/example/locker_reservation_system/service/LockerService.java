package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.LockerDateDetail;
import com.example.locker_reservation_system.repository.LockerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LockerService {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private LockerDateDetailService lockerDateDetailService;

    public List<LockerStatusResponse> getLockerStatusByDateRange(Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("startDate cannot be greater than endDate");
        }
        List<Locker> lockers = lockerRepository.findAll();
        List<LockerStatusResponse> responses = new ArrayList<>();

        for (Locker locker : lockers) {
            List<LockerDateDetail> details = lockerDateDetailService.findByLockerIdAndDateBetween(locker.getLockerId(), startDate, endDate);

            LockerStatusResponse response = new LockerStatusResponse();
            response.setLockerId(locker.getLockerId());
            response.setSite(locker.getSite());
            response.setCapacity(locker.getCapacity());
            response.setUsability(locker.getUsability());

            boolean allAvailable = true;
            StringBuilder memoBuilder = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (LockerDateDetail detail : details) {
                if (!detail.getStatus().equals("available")) {
                    allAvailable = false;
                }

                if (detail.getMemo() != null && !detail.getMemo().isEmpty()) {
                    memoBuilder.append(dateFormat.format(detail.getDate())).append(": ").append(detail.getMemo()).append(", ");
                }
            }

            if (allAvailable) {
                response.setStatus("available");
            } else {
                response.setStatus("Unavailable");
            }

            String memo = memoBuilder.toString().trim();
            if (!memo.isEmpty()) {
                response.setMemo(memo.substring(0, memo.length() - 1));
            }

            responses.add(response);
        }

        return responses;
    }

    public Optional<Locker> findLockerById(long lockerId) {
        return lockerRepository.findById(lockerId);
    }

}
