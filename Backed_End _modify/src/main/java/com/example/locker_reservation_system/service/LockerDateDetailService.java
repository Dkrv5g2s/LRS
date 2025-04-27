package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.entity.LockerDateDetail;
import com.example.locker_reservation_system.repository.LockerDateDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LockerDateDetailService {

    @Autowired
    private LockerDateDetailRepository lockerDateDetailRepository;

    public List<LockerDateDetail> findByLockerIdAndDateBetween(long lockerId, Date startDate, Date endDate) {
        return lockerDateDetailRepository.findByLockerLockerIdAndDateBetween(lockerId, startDate, endDate);
    }

    public Optional<LockerDateDetail> findByLockerAndDate(long lockerId, Date date) {
        return lockerDateDetailRepository.findByLockerLockerIdAndDate(lockerId, date);
    }

    public void updateLockerDateDetail(LockerDateDetail lockerDateDetail) {
        lockerDateDetailRepository.save(lockerDateDetail);
    }
}
