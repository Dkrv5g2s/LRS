package com.example.locker_reservation_system.entity;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.repository.LockerRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LockerList {

    @Autowired
    private LockerRepository lockerRepository;


    @Getter
    private List<Locker> lockers;

    @PostConstruct
    public void init() {
        this.lockers = lockerRepository.findAll();
        lockers.forEach(locker -> {
            // 確保每個 Locker 的 dateDetails 都已初始化
            locker.getDateDetails().size(); // 使用 size() 強制加載
        });
    }

    public List<LockerStatusResponse> getLockerStatusByDateRange(Date startDate, Date endDate) {
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("startDate cannot be greater than endDate");
        }

        List<LockerStatusResponse> responses = new ArrayList<>();

        for (Locker locker : lockers) { // 這裡改成直接用 private List<Locker> lockers
            List<LockerDateDetail> filteredDetails = new ArrayList<>();

            for (LockerDateDetail detail : locker.getDateDetails()) {
                Date detailDate = detail.getDate();
                if (!detailDate.before(startDate) && !detailDate.after(endDate)) {
                    filteredDetails.add(detail);
                }
            }

            LockerStatusResponse response = new LockerStatusResponse();
            response.setLockerId(locker.getLockerId());
            response.setSite(locker.getSite());
            response.setCapacity(locker.getCapacity());
            response.setUsability(locker.isUsability());

            boolean allAvailable = true;
            StringBuilder memoBuilder = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (LockerDateDetail detail : filteredDetails) {
                if (!"available".equals(detail.getStatus())) {
                    allAvailable = false;
                }

                if (detail.getMemo() != null && !detail.getMemo().isEmpty()) {
                    memoBuilder.append(dateFormat.format(detail.getDate()))
                            .append(": ")
                            .append(detail.getMemo())
                            .append(", ");
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



}
