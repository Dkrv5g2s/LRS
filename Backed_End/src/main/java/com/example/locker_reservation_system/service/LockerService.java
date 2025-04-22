package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.dto.LockerStatusResponse;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.LockerDateDetail;
import com.example.locker_reservation_system.repository.LockerDateDetailRepository;
import com.example.locker_reservation_system.repository.LockerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LockerService {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private LockerDateDetailRepository lockerDateDetailRepository;

    public List<LockerStatusResponse> getLockerStatusByDate(Date date) {
        List<Locker> lockers = lockerRepository.findAll();
        return lockers.stream().map(locker -> {
            LockerStatusResponse response = new LockerStatusResponse();
            response.setLockerId(locker.getLockerId());
            response.setSite(locker.getSite());
            response.setCapacity(locker.getCapacity());
            response.setUsability(locker.getUsability());

            List<LockerDateDetail> details = lockerDateDetailRepository.findByLockerLockerIdAndDate(locker.getLockerId(), date);
            if (!details.isEmpty()) {
                LockerDateDetail detail = details.getFirst();
                response.setStatus(detail.getStatus());
                response.setMemo(detail.getMemo());
            } else {
                response.setStatus("Available");
                response.setMemo(null);
            }
            return response;
        }).collect(Collectors.toList());
    }
}