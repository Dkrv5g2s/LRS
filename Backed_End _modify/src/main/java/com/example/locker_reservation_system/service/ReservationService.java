package com.example.locker_reservation_system.service;

import com.example.locker_reservation_system.dto.ReservationRequest;
import com.example.locker_reservation_system.entity.Locker;
import com.example.locker_reservation_system.entity.LockerDateDetail;
import com.example.locker_reservation_system.entity.Reservation;
import com.example.locker_reservation_system.entity.User;
import com.example.locker_reservation_system.repository.ReservationRepository;
import com.example.locker_reservation_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LockerService lockerService;

    @Autowired
    private LockerDateDetailService lockerDateDetailService;

    @Autowired
    private UserRepository userRepository;

    public Reservation reserve(ReservationRequest request) {
        Locker locker = lockerService.findLockerById(request.getLockerId())
                .orElseThrow(() -> new RuntimeException("Locker not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Reservation> existingReservations = reservationRepository.findByLockerAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                locker, request.getEndDate(), request.getStartDate());
        if (!existingReservations.isEmpty()) {
            throw new RuntimeException("Locker is already reserved for the requested time period.");
        }

        List<LockerDateDetail> lockerDateDetails = lockerDateDetailService.findByLockerIdAndDateBetween(locker.getLockerId(), request.getStartDate(), request.getEndDate());
        for (LockerDateDetail detail : lockerDateDetails) {
            if (!"available".equals(detail.getStatus())) {
                throw new RuntimeException("Locker has a detail status that is not occupied for the requested time period.");
            }
        }

        Reservation reservation = new Reservation();
        reservation.setLocker(locker);
        reservation.setUser(user);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());

        Reservation savedReservation = reservationRepository.save(reservation);

        updateLockerDateDetails(locker, request.getStartDate(), request.getEndDate());

        return savedReservation;
    }

    private void updateLockerDateDetails(Locker locker, Date startDate, Date endDate) {
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate currentDate = startLocalDate;
        while (!currentDate.isAfter(endLocalDate)) {
            LockerDateDetail lockerDateDetail = lockerDateDetailService.findByLockerAndDate(locker.getLockerId(), Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .orElse(new LockerDateDetail());
            lockerDateDetail.setLocker(locker);
            lockerDateDetail.setDate(Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            lockerDateDetail.setStatus("occupied");

            lockerDateDetailService.updateLockerDateDetail(lockerDateDetail);

            currentDate = currentDate.plusDays(1);
        }
    }

    public List<Reservation> getReservationsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByUser(user);
    }
}



//import java.util.UUID;
//
//public Reservation Reserve(ReservationRequest request) {
//    Locker locker = lockerRepository.findById(request.getLockerId())
//            .orElseThrow(() -> new RuntimeException("Locker not found"));
//
//    User user = userRepository.findById(request.getUserId())
//            .orElseThrow(() -> new RuntimeException("User not found"));
//
//    // 檢查是否有人已經預約了該時間段和 locker
//    List<Reservation> existingReservations = reservationRepository.findByLockerAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//            locker, request.getEndDate(), request.getStartDate());
//    if (!existingReservations.isEmpty()) {
//        throw new RuntimeException("Locker is already reserved for the requested time period.");
//    }
//
//    // 檢查 LockerDateDetail 的 status是否有非available
//    List<LockerDateDetail> lockerDateDetails = lockerDateDetailRepository.findByLockerAndDateBetween(locker, request.getStartDate(), request.getEndDate());
//    for (LockerDateDetail detail : lockerDateDetails) {
//        if (!"available".equals(detail.getStatus())) {
//            throw new RuntimeException("Locker has a detail status that is not occupied for the requested time period.");
//        }
//    }
//
//    Reservation reservation = new Reservation();
//    reservation.setLocker(locker);
//    reservation.setUser(user);
//    reservation.setStartDate(request.getStartDate());
//    reservation.setEndDate(request.getEndDate());
//
//    // 生成條形碼
//    String barcode = generateBarcode();
//    reservation.setBarcode(barcode);
//
//    // 保存預約
//    Reservation savedReservation = reservationRepository.save(reservation);
//
//    // 更新 LockerDateDetail 的狀態為 occupied
//    updateLockerDateDetails(locker, request.getStartDate(), request.getEndDate());
//
//    return savedReservation;
//}
//
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
//private String generateBarcode(Long lockerId, String startDate, String endDate) {
//    // 將 locker_id 和起訖日期組合成一個字符串
//    String data = lockerId + ":" + startDate + ":" + endDate;
//
//    // 將字符串轉換為 Base64 編碼
//    return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
//}




//public Reservation updateReservationDates(Long reservationId, Date newStartDate, Date newEndDate) {
//    Reservation reservation = reservationRepository.findById(reservationId)
//            .orElseThrow(() -> new RuntimeException("Reservation not found"));
//
//    Locker locker = reservation.getLocker();
//
//    // 取消原始日期範圍的狀態
//    updateLockerDateDetails(locker, reservation.getStartDate(), reservation.getEndDate(), "available");
//
//    // 更新預約日期
//    reservation.setStartDate(newStartDate);
//    reservation.setEndDate(newEndDate);
//
//    // 保存更新後的預約
//    Reservation updatedReservation = reservationRepository.save(reservation);
//
//    // 更新新日期範圍的狀態
//    updateLockerDateDetails(locker, newStartDate, newEndDate, "occupied");
//
//    return updatedReservation;
//}

//public void cancelReservation(Long reservationId) {
//    Reservation reservation = reservationRepository.findById(reservationId)
//            .orElseThrow(() -> new RuntimeException("Reservation not found"));
//
//    Locker locker = reservation.getLocker();
//
//    // 將原始日期範圍的狀態設為 available
//    updateLockerDateDetails(locker, reservation.getStartDate(), reservation.getEndDate(), "available");
//
//    // 刪除預約
//    reservationRepository.delete(reservation);
//}

//private void updateLockerDateDetails(Locker locker, Date startDate, Date endDate, String status) {
//    LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//    LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
//    LocalDate currentDate = startLocalDate;
//    while (!currentDate.isAfter(endLocalDate)) {
//        LockerDateDetail lockerDateDetail = lockerDateDetailRepository.findByLockerAndDate(locker, Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
//                .orElse(new LockerDateDetail());
//        lockerDateDetail.setLocker(locker);
//        lockerDateDetail.setDate(Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
//        lockerDateDetail.setStatus(status);
//
//        lockerDateDetailRepository.save(lockerDateDetail);
//
//        currentDate = currentDate.plusDays(1);
//    }
//}
//public void cancelReservation(Long reservationId) {
//    Reservation reservation = reservationRepository.findById(reservationId)
//            .orElseThrow(() -> new RuntimeException("Reservation not found"));
//
//    Locker locker = reservation.getLocker();
//
//    // 將原始日期範圍的狀態設為 available
//    updateLockerDateDetails(locker, reservation.getStartDate(), reservation.getEndDate(), "available");
//
//    // 刪除預約
//    reservationRepository.delete(reservation);
//}
