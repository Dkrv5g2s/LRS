"use client";
import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useUser } from '../../context/UserContext';
import { zhTW } from 'date-fns/locale';

interface Locker {
  lockerId: number;
  capacity: number;
  memo: string;
  usability: boolean;
  status: string;
  site: string;
}

const LockerGrid = () => {
  const { user } = useUser();
  const [lockers, setLockers] = useState<Locker[]>([]);
  const today = new Date();
  const [startDate, setStartDate] = useState<Date | null>(today);
  const [endDate, setEndDate] = useState<Date | null>(today);
  const [tooltip, setTooltip] = useState<{ visible: boolean; message: string; position: { top: number; left: number } }>({ visible: false, message: "", position: { top: 0, left: 0 } });
  const lockerRefs = useRef<(HTMLDivElement | null)[]>([]);

  const formatDate = (date: Date): string => {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  };

  const fetchLockerStatus = async (start: Date, end: Date) => {
    if (!start || !end) {
      console.error("Please select both start and end dates.");
      return;
    }
    try {
      const formattedStartDate = formatDate(start);
      const formattedEndDate = formatDate(end);
      const response = await axios.get(
        `http://localhost:8080/api/lockers/status?startDate=${formattedStartDate}&endDate=${formattedEndDate}`
      );
      console.log("Fetched locker status:", response.data);
      setLockers(response.data);
    } catch (error) {
      console.error("Error fetching locker status:", error);
      alert("查詢失敗，請稍後再試！");
  
      // 當錯誤發生時將日期回復為當前日期並重新查詢
      const today = new Date();
      setStartDate(today);
      setEndDate(today);
  
      // 重新發送查詢請求
      fetchLockerStatus(today, today);
    }
  };

  const handleDateChange = (start: Date | null, end: Date | null) => {
    setStartDate(start);
    setEndDate(end);
  };

  const handleSearch = () => {
    if (startDate instanceof Date && endDate instanceof Date) {
      fetchLockerStatus(startDate, endDate);
    } else {
      alert("請選擇起始與結束日期！");
    }
  };

  const handleLockerClick = (locker: Locker, index: number) => {
    if (
      locker.usability &&
      locker.status === "available" &&
      startDate instanceof Date &&
      endDate instanceof Date
    ) {
      const confirmed = window.confirm("確定要預約此櫃子嗎?");
      if (confirmed) {
        reserveLocker(locker.lockerId);
      }
    }
  };

  const handleMouseEnter = (locker: Locker, index: number) => {
    const lockerRef = lockerRefs.current[index];
    if (lockerRef) {
      let message = "";
      if (!locker.usability) {
        message = `${locker.site} 無櫃位`;
      } else if (locker.status !== "available") {
        message = `${locker.site} 不可預約`;
      } else {
        message = `${locker.site} 可預約`;
      }

      setTooltip({
        visible: true,
        message,
        position: {
          top: lockerRef.offsetTop + 10,
          left: lockerRef.offsetLeft - 100,
        },
      });
    }
  };

  const handleMouseLeave = () => {
    setTooltip({ ...tooltip, visible: false });
  };

  const reserveLocker = async (lockerId: number) => {
    if (!user) {
      console.error("User is not logged in.");
      return;
    }

    if (!(startDate instanceof Date) || !(endDate instanceof Date)) {
      alert("請先選擇預約日期！");
      return;
    }

    const reservationRequest = {
      lockerId: lockerId,
      userId: user.userId,
      startDate: formatDate(startDate),
      endDate: formatDate(endDate)
    };

    try {
      const response = await axios.post(`http://localhost:8080/api/reservations`, reservationRequest);
      console.log("Locker reserved successfully:", response.data);
      alert("預約成功！");
      fetchLockerStatus(startDate, endDate); // 預約成功後更新列表
    } catch (error) {
      console.error("Error reserving locker:", error);
      alert("預約失敗，請稍後再試！");
    }
  };

  useEffect(() => {
    // useEffect會依賴startDate和endDate來更新資料，這樣日期一改變就會重新發送查詢
    if (startDate && endDate) {
      fetchLockerStatus(startDate, endDate);
    }
  }, []); // 加入startDate和endDate作為依賴項

  return (
    <div className="p-4 relative">
      <div className="mb-4 flex justify-center items-center">
        <label className="mr-2">選擇日期範圍</label>
        <DatePicker
          selected={startDate}
          onChange={(date: Date | null) => {
            handleDateChange(date, endDate);
          }}
          dateFormat="yyyy/MM/dd"
          placeholderText="開始日期"
          className="border p-2 mr-2"
          locale={zhTW}
          minDate={today}
        />
        <DatePicker
          selected={endDate}
          onChange={(date: Date | null) => {
            handleDateChange(startDate, date);
          }}
          dateFormat="yyyy/MM/dd"
          placeholderText="結束日期"
          className="border p-2 mr-2"
          locale={zhTW}
          minDate={today}
        />
        <button
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          onClick={handleSearch}
        >
          搜尋
        </button>
      </div>

      {tooltip.visible && (
        <div
          className="absolute bg-gray-700 text-white p-2 rounded"
          style={{
            top: `${tooltip.position.top - 20}px`,
            left: `${tooltip.position.left + 200}px`,
          }}
        >
          {tooltip.message}
        </div>
      )}

      <div className="grid grid-cols-12 gap-4">
        <div className="col-span-12 border-b-2 mb-4"></div>

        <div className="col-span-5 grid grid-cols-2 gap-2">
          {lockers.slice(0, 10).map((locker, index) => (
            <div
              key={locker.lockerId}
              className={`p-4 border rounded-lg ${locker.usability
                ? locker.status === "available"
                  ? "bg-green-500 cursor-pointer"
                  : "bg-red-500 cursor-not-allowed"
                : "bg-black opacity-50 cursor-not-allowed"
                }`}
              onClick={() => handleLockerClick(locker, index)}
              onMouseEnter={() => handleMouseEnter(locker, index)}
              onMouseLeave={handleMouseLeave}
              ref={(el) => {
                lockerRefs.current[index] = el;
              }}
            >
              {locker.usability ? (
                <>
                  <p>位置: {locker.site}</p>
                  <p>容量: {locker.capacity}</p>
                  <p>備註:<br /> {locker.memo}</p>
                </>
              ) : (
                <div></div>
              )}
            </div>
          ))}
        </div>

        <div className="col-span-2 flex flex-col items-center justify-center">
          <div className="border-l-2 h-full"></div>
        </div>

        <div className="col-span-5 grid grid-cols-2 gap-2">
          {lockers.slice(10, 20).map((locker, index) => (
            <div
              key={locker.lockerId}
              className={`p-4 border rounded-lg ${locker.usability
                ? locker.status === "available"
                  ? "bg-green-500 cursor-pointer"
                  : "bg-red-500 cursor-not-allowed"
                : "bg-black opacity-50 cursor-not-allowed"
                }`}
              onClick={() => handleLockerClick(locker, index + 10)}
              onMouseEnter={() => handleMouseEnter(locker, index + 10)}
              onMouseLeave={handleMouseLeave}
              ref={(el) => {
                lockerRefs.current[index + 10] = el;
              }}
            >
              {locker.usability ? (
                <>
                  <p>位置: {locker.site}</p>
                  <p>容量: {locker.capacity}</p>
                  <p>備註:<br /> {locker.memo}</p>
                </>
              ) : (
                <div></div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default LockerGrid;
