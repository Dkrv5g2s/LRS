"use client";
import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useUser } from '../../context/UserContext';
import { zhTW } from 'date-fns/locale';
import LockerCard from "../../components/LockerCard";
import BasicTableOne from "../../components/tables/BasicTableOne";

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
        `http://localhost:8080/api/reservations/status?startDate=${formattedStartDate}&endDate=${formattedEndDate}`
      );
      console.log("Fetched locker status:", response.data);
      setLockers(response.data);
    } catch (error) {
      console.error("Error fetching locker status:", error);
      alert("Query failed, please try again later!");
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
      alert("Please select both start and end dates!");
    }
  };

  const handleLockerClick = (locker: Locker, index: number) => {
    if (
      locker.usability &&
      locker.status === "available" &&
      startDate instanceof Date &&
      endDate instanceof Date
    ) {
      const confirmed = window.confirm("Are you sure you want to reserve this locker?");
      if (confirmed) {
        reserveLocker(locker.lockerId);
      }
    }
  };

  const reserveLocker = async (lockerId: number) => {
    if (!user) {
      console.error("User is not logged in.");
      return;
    }

    if (!(startDate instanceof Date) || !(endDate instanceof Date)) {
      alert("Please select reservation dates first!");
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
      alert("Reservation successful!");
      fetchLockerStatus(startDate, endDate); // 預約成功後更新列表
    } catch (error) {
      console.error("Error reserving locker:", error);
      alert("Reservation failed, please try again later!");
    }
  };

  useEffect(() => {
    // useEffect會依賴startDate和endDate來更新資料，這樣日期一改變就會重新發送查詢
    if (startDate && endDate) {
      fetchLockerStatus(startDate, endDate);
    }
  }, [startDate, endDate]); // 加入startDate和endDate作為依賴項

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* 日期選擇區域 */}
        <div className="mb-8">
          <div className="flex flex-col md:flex-row justify-center items-center gap-4">
            <div className="flex items-center space-x-2">
              <label className="text-gray-700 font-medium">Start Date</label>
              <DatePicker
                selected={startDate}
                onChange={(date: Date | null) => {
                  handleDateChange(date, endDate);
                }}
                dateFormat="yyyy/MM/dd"
                placeholderText="Select start date"
                className="border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                locale={zhTW}
                minDate={today}
              />
            </div>

            <div className="flex items-center space-x-2">
              <label className="text-gray-700 font-medium">End Date</label>
              <DatePicker
                selected={endDate}
                onChange={(date: Date | null) => {
                  handleDateChange(startDate, date);
                }}
                dateFormat="yyyy/MM/dd"
                placeholderText="Select end date"
                className="border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                locale={zhTW}
                minDate={today}
              />
            </div>
          </div>
        </div>

        {/* 置物櫃網格 */}
        <div className="grid grid-cols-12 gap-6">
          <div className="col-span-12">
            <div className="h-1 bg-gray-200 rounded-full mb-6"></div>
          </div>

          <div className="col-span-5 grid grid-cols-2 gap-4">
            {lockers.slice(0, 10).map((locker, index) => (
              <LockerCard
                key={locker.lockerId}
                locker={locker}
                index={index}
                onLockerClick={handleLockerClick}
                onMouseEnter={() => {}}
                onMouseLeave={() => {}}
                refCallback={(el) => {
                  lockerRefs.current[index] = el;
                }}
                startDate={startDate}
                endDate={endDate}
                fetchLockerStatus={fetchLockerStatus}
              />
            ))}
          </div>

          <div className="col-span-2 flex flex-col items-center justify-center">
            <div className="h-full w-1 bg-gray-200 rounded-full"></div>
          </div>

          <div className="col-span-5 grid grid-cols-2 gap-4">
            {lockers.slice(10, 20).map((locker, index) => (
              <LockerCard
                key={locker.lockerId}
                locker={locker}
                index={index + 10}
                onLockerClick={handleLockerClick}
                onMouseEnter={() => {}}
                onMouseLeave={() => {}}
                refCallback={(el) => {
                  lockerRefs.current[index + 10] = el;
                }}
                startDate={startDate}
                endDate={endDate}
                fetchLockerStatus={fetchLockerStatus}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default LockerGrid;
