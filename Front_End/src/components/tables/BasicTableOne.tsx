"use client";
import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../ui/table";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css"; // 必須引入樣式
import { useUser } from "../../context/UserContext";
import { format } from "date-fns";
import { zhTW } from "date-fns/locale"; // 引入台灣地區的 locale
import { Modal } from "../ui/modal/index"; // 引入 Modal

interface Reservation {
  reservationId: number;
  locker: {
    lockerId: number;
    site: string;
    capacity: number;
    usability: boolean;
  };
  user: {
    userId: number;
    accountName: string;
    password: string;
    phoneNumber: string;
    isAdmin: boolean;
  };
  startDate: string;
  endDate: string;
  barcode: string | null;
}

export default function BasicTableOne() {
  const { user } = useUser();
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
  const [selectedStartDate, setSelectedStartDate] = useState<Date | null>(null);
  const [selectedEndDate, setSelectedEndDate] = useState<Date | null>(null);
  const [showDialog, setShowDialog] = useState<boolean>(false);

  // 加载当前用户的预约数据
  useEffect(() => {
    if (user) {
      axios
        .get(`http://localhost:8080/api/reservation/user/${user.userId}`)
        .then((response) => {
          console.log("Fetched reservations:", response.data); // 打印后端返回的数据
          setReservations(response.data);
        })
        .catch((error) => {
          console.error("Error fetching reservations:", error);
        });
    }
  }, [user]);

  // 删除预约
  const handleDelete = (reservationId: number) => {
    if (window.confirm("確定要刪除此預約嗎？")) {
      axios
        .delete(`http://localhost:8080/api/reservation/${reservationId}`)
        .then(() => {
          setReservations(reservations.filter((res) => res.reservationId !== reservationId));
          alert("預約已刪除！");
        })
        .catch((error) => {
          console.error("Error deleting reservation:", error);
          alert("刪除預約失敗！");
        });
    }
  };

  // 打開編輯對話框
  const openEditDialog = (reservation: Reservation) => {
    setSelectedReservation(reservation);
    setStartDate(new Date(reservation.startDate));
    setEndDate(new Date(reservation.endDate));
    setSelectedStartDate(new Date(reservation.startDate));
    setSelectedEndDate(new Date(reservation.endDate));
    setShowDialog(true);
  };

  // 關閉編輯對話框
  const closeDialog = () => {
    setShowDialog(false);
  };

  // 更新預約
  const handleEdit = async () => {
    if (selectedStartDate && selectedEndDate && selectedReservation) {
      const formattedStartDate = format(selectedStartDate, "yyyy-MM-dd");
      const formattedEndDate = format(selectedEndDate, "yyyy-MM-dd");

      const isValidDateRange = (startDate: Date, endDate: Date) => {
        return startDate <= endDate;
      };

      if (!isValidDateRange(selectedStartDate, selectedEndDate)) {
        alert("結束時間不能早於起始時間！");
        return;
      }
      setStartDate(selectedStartDate);
      setEndDate(selectedEndDate);

      try {
        const response = await axios.put(
          `http://localhost:8080/api/reservation/${selectedReservation.reservationId}/dates`,
          null,
          {
            params: {
              newStartDate: formattedStartDate,
              newEndDate: formattedEndDate
            }
          }
        );
        console.log("Reservation updated:", response.data);
        setReservations(
          reservations.map((reservation) =>
            reservation.reservationId === selectedReservation.reservationId
              ? { ...reservation, startDate: formattedStartDate, endDate: formattedEndDate }
              : reservation
          )
        );
        alert("預約已更新！");
        closeDialog();
      } catch (error) {
        console.error("Error updating reservation:", error);
        alert("更新預約失敗！");
      }
    }
  };


  // 生成條碼
  const generateBarcode = (reservationId: number) => {
    axios
      .get(`http://localhost:8080/api/barcode/${reservationId}`)
      .then((response) => {
        alert(`條碼內容：${response.data.barcode}`);
      })
      .catch((error) => {
        console.error("Error generating barcode:", error);
      });
  };

  // 處理開始日期改變時的邏輯
  const handleStartDateChange = (date: Date | null) => {
    if (date) {
      setSelectedStartDate(date);
      if (selectedEndDate && date > selectedEndDate) {
        setSelectedEndDate(date);
      }
    }
  };

  // 處理結束日期改變時的邏輯
  const handleEndDateChange = (date: Date | null) => {
    if (date) {
      setSelectedEndDate(date);
      if (selectedStartDate && date < selectedStartDate) {
        setSelectedStartDate(date);
      }
    }
  };

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="max-w-full overflow-x-auto">
        <div className="min-w-[1102px]">
          <Table>
            <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
              <TableRow>
                <TableCell isHeader className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">Locker Site</TableCell>
                <TableCell isHeader className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">Capacity</TableCell>
                <TableCell isHeader className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">Usability</TableCell>
                <TableCell isHeader className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">Start Date</TableCell>
                <TableCell isHeader className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">End Date</TableCell>
                <TableCell isHeader className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">Barcode</TableCell>
                <TableCell isHeader className="px-5 py-3 font-medium text-gray-500 text-start text-theme-xs dark:text-gray-400">Actions</TableCell>
              </TableRow>
            </TableHeader>

            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {reservations.map((reservation) => (
                <TableRow key={reservation.reservationId}>
                  <TableCell className="px-5 py-4 sm:px-6 text-start">{reservation.locker.site}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.locker.capacity}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.locker.usability ? "Yes" : "No"}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.startDate}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.endDate}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.barcode || "N/A"}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    <button
                      onClick={() => handleDelete(reservation.reservationId)}
                      className="bg-red-500 text-white px-4 py-2 rounded mr-2"
                    >
                      刪除
                    </button>
                    <button
                      onClick={() => openEditDialog(reservation)}
                      className="bg-blue-500 text-white px-4 py-2 rounded mr-2"
                    >
                      更新
                    </button>
                    <button
                      onClick={() => generateBarcode(reservation.reservationId)}
                      className="flex w-full items-center justify-center gap-2 rounded-full border border-gray-300 bg-white px-4 py-3 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200 lg:inline-flex lg:w-auto"
                    >
                      查看條碼
                    </button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </div>

      {/* 使用 Modal 替代對話框 */}
      <Modal isOpen={showDialog} onClose={closeDialog} className="max-w-[700px] m-4">
        <div className="relative w-full max-w-[700px] rounded-3xl bg-white p-4 dark:bg-gray-900 lg:p-11">
          <div className="px-2 pr-14">
            <h3 className="mb-2 text-2xl font-semibold text-gray-800 dark:text-white/90">
              更新預約
            </h3>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400 lg:mb-7">
              調整日期只能在原始選擇的範圍內
            </p>
          </div>
          <form className="flex flex-col">
            <div className="px-2 pb-3">
              <div className="grid grid-cols-1 gap-x-3 gap-y-5 lg:grid-cols-2">
                <div><label >開始日期</label>
                  <DatePicker
                    selected={selectedStartDate}
                    onChange={handleStartDateChange}
                    minDate={startDate || undefined}
                    maxDate={endDate || undefined}
                    dateFormat="yyyy/MM/dd"
                    placeholderText="開始日期"
                    className="border p-2"
                    locale={zhTW}
                  />
                </div>
                <div><label >結束日期</label>
                  <DatePicker
                    selected={selectedEndDate}
                    onChange={handleEndDateChange}
                    minDate={startDate || undefined}
                    maxDate={endDate || undefined}
                    dateFormat="yyyy/MM/dd"
                    placeholderText="結束日期"
                    className="border p-2"
                    locale={zhTW}
                  />
                </div>


              </div>
            </div>
            <div className="flex justify-end space-x-2 px-2 mt-6 lg:justify-end">
              <button onClick={closeDialog} className="bg-gray-300 px-4 py-2 rounded">取消</button>
              <button onClick={handleEdit} className="bg-blue-500 text-white px-4 py-2 rounded">確定更新</button>
            </div>
          </form>
        </div>
      </Modal>
    </div>
  );
}
