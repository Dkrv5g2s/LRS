"use client";
import React, { useEffect, useState } from "react";
import axios from "axios";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

interface Locker {
  lockerId: number;
  capacity: number;
  memo: string;
  status: string;
}

const LockerGrid = () => {
  const [lockers, setLockers] = useState<Locker[]>([]);
  const [selectedDate, setSelectedDate] = useState(new Date("2025-04-22"));

  useEffect(() => {
    const fetchLockerStatus = async () => {
      try {
        const formattedDate = selectedDate.toISOString().split('T')[0];
        const response = await axios.get(`http://localhost:8080/api/locker/status?date=${formattedDate}`);
        setLockers(response.data);
      } catch (error) {
        console.error("Error fetching locker status:", error);
      }
    };

    fetchLockerStatus();
  }, [selectedDate]);

  return (
    <div className="p-4">
      <div className="mb-4">
        <DatePicker
          selected={selectedDate}
          onChange={(date: Date | null) => {
            if (date) {
              setSelectedDate(date);
            }
          }}
          dateFormat="yyyy-MM-dd"
          className="border p-2"
        />
      </div>

      <div className="grid grid-cols-12 gap-4">
        <div className="col-span-5 grid grid-cols-2 gap-2">
          {lockers.slice(0, 10).map((locker) => (
            <div
              key={locker.lockerId}
              className={`p-4 border rounded-lg ${locker.status === "available" ? "bg-green-500" : "bg-red-500"}`}
            >
              <p>容量: {locker.capacity}</p>
              <p>備註: {locker.memo}</p>
            </div>
          ))}
        </div>

        <div className="col-span-2 flex flex-col items-center justify-center">
          <div className="border-l-2 h-full"></div>
        </div>

        <div className="col-span-5 grid grid-cols-2 gap-2">
          {lockers.slice(10, 20).map((locker) => (
            <div
              key={locker.lockerId}
              className={`p-4 border rounded-lg ${locker.status === "available" ? "bg-green-500" : "bg-red-500"}`}
            >
              <p>容量: {locker.capacity}</p>
              <p>備註: {locker.memo}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default LockerGrid;
