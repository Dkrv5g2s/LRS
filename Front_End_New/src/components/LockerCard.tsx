import React, { useState } from 'react';
import { useUser } from '../context/UserContext';
import axios from 'axios';

interface Locker {
  lockerId: number;
  capacity: number;
  memo: string;
  usability: boolean;
  status: string;
  site: string;
}

interface LockerCardProps {
  locker: Locker;
  index: number;
  onLockerClick: (locker: Locker, index: number) => void;
  onMouseEnter: (locker: Locker, index: number) => void;
  onMouseLeave: () => void;
  refCallback: (el: HTMLDivElement | null) => void;
}

const LockerCard: React.FC<LockerCardProps> = ({
  locker,
  index,
  onLockerClick,
  onMouseEnter,
  onMouseLeave,
  refCallback,
}) => {
  const { user } = useUser();
  const isAdmin = user?.isAdmin;
  const [isEditing, setIsEditing] = useState(false);
  const [editedStatus, setEditedStatus] = useState(locker.status);
  const [editedMemo, setEditedMemo] = useState(locker.memo);
  const [editedCapacity, setEditedCapacity] = useState(locker.capacity);

  const handleEdit = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isEditing) {
      try {
        const response = await axios.put(`http://localhost:8080/api/lockers/${locker.lockerId}`, {
          status: editedStatus,
          memo: editedMemo,
          capacity: editedCapacity
        });
        if (response.status === 200) {
          alert('更新成功！');
          window.location.reload();
        }
      } catch (error) {
        console.error('更新失敗：', error);
        alert('更新失敗，請稍後再試！');
      }
    }
    setIsEditing(!isEditing);
  };

  const handleAdd = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      const response = await axios.post(`http://localhost:8080/api/lockers`, {
        site: locker.site,
        capacity: 0,
        memo: '',
        usability: true,
        status: 'available'
      });
      if (response.status === 200) {
        alert('新增櫃位成功！');
        window.location.reload();
      }
    } catch (error) {
      console.error('新增失敗：', error);
      alert('新增失敗，請稍後再試！');
    }
  };

  return (
    <div
      ref={refCallback}
      className={`relative p-4 rounded-lg ${
        locker.usability
          ? locker.status === "available"
            ? "bg-emerald-100"
            : "bg-red-100"
          : "bg-gray-200"
      }`}
      onClick={() => !isEditing && onLockerClick(locker, index)}
      onMouseEnter={() => onMouseEnter(locker, index)}
      onMouseLeave={onMouseLeave}
    >
      <div className="flex flex-col space-y-2">
        <div className="flex justify-between items-center">
          <span className="text-lg font-medium">{locker.site}</span>
          {isEditing ? (
            <select
              value={editedStatus}
              onChange={(e) => setEditedStatus(e.target.value)}
              className="px-2 py-1 rounded text-sm border border-gray-300 focus:outline-none focus:ring-2 focus:ring-emerald-500"
            >
              <option value="available">可預約</option>
              <option value="unavailable">不可預約</option>
            </select>
          ) : (
            <span className={`px-2 py-1 rounded text-sm ${
              locker.usability
                ? locker.status === "available"
                  ? "bg-emerald-200 text-emerald-800"
                  : "bg-red-200 text-red-800"
                : "bg-gray-300 text-gray-600"
            }`}>
              {locker.usability
                ? locker.status === "available"
                  ? "可預約"
                  : "不可預約"
                : "無櫃位"}
            </span>
          )}
        </div>
        {locker.usability && (
          <div className="space-y-1">
            {/* <div className="flex items-center space-x-2">
              <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span className="text-sm text-gray-600">位置: {locker.site}</span>
            </div> */}
            <div className="flex items-center space-x-2">
              <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
              {isEditing ? (
                <input
                  type="number"
                  value={editedCapacity}
                  onChange={(e) => setEditedCapacity(Number(e.target.value))}
                  className="text-sm text-gray-600 border border-gray-300 rounded px-2 py-1 w-20"
                  min="0"
                />
              ) : (
                <span className="text-sm text-gray-600">容量: {locker.capacity}</span>
              )}
            </div>
            <div className="flex items-start space-x-2">
              <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
              </svg>
              <div className="flex flex-col">
                <span className="text-sm text-gray-600">備註:</span>
                {isEditing ? (
                  <textarea
                    value={editedMemo}
                    onChange={(e) => setEditedMemo(e.target.value)}
                    className="ml-7 text-sm text-gray-600 border border-gray-300 rounded px-2 py-1"
                    rows={2}
                  />
                ) : (
                  <span className="text-sm text-gray-600 ml-7">{locker.memo}</span>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
      {isAdmin && (
        <div className="absolute bottom-2 right-2">
          <button
            onClick={locker.usability ? handleEdit : handleAdd}
            className="p-1.5 rounded-full text-gray-600 hover:text-gray-800"
          >
            {locker.usability ? (
              isEditing ? (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                </svg>
              ) : (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
              )
            ) : (
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" />
              </svg>
            )}
          </button>
        </div>
      )}
    </div>
  );
};

export default LockerCard; 