import React, { useState, useRef, useEffect } from 'react';
import { useUser } from '../context/UserContext';
import axios from 'axios';
import { Modal } from './ui/modal';

interface User {
  userId: number;
  accountName: string | null;
  phoneNumber: string | null;
}

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
  startDate: Date | null;
  endDate: Date | null;
  fetchLockerStatus: (start: Date, end: Date) => void;
}

const LockerCard: React.FC<LockerCardProps> = ({
  locker,
  index,
  onLockerClick,
  onMouseEnter,
  onMouseLeave,
  refCallback,
  startDate,
  endDate,
  fetchLockerStatus,
}) => {
  const { user } = useUser();
  const isAdmin = user?.isAdmin;
  const [showModal, setShowModal] = useState(false);
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [editedStatus, setEditedStatus] = useState(locker.status);
  const [editedMemo, setEditedMemo] = useState(locker.memo);
  const [editedCapacity, setEditedCapacity] = useState(locker.capacity);
  const [searchQuery, setSearchQuery] = useState('');
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const searchTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

  const formatDate = (date: Date): string => {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  };

  const handleOpenModal = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!locker.usability) {
      // Reset all fields when adding new locker
      setEditedStatus('available');
      setEditedMemo('');
      setEditedCapacity(0);
    }
    setShowModal(true);
  };

  const handleEdit = async () => {
    try {
      const response = await axios.put(`http://localhost:8080/api/lockers/${locker.lockerId}`, {
        status: editedStatus,
        memo: editedMemo,
        capacity: editedCapacity,
        startDate: startDate ? formatDate(startDate) : null,
        endDate: endDate ? formatDate(endDate) : null
      });
      if (response.status === 200) {
        alert('Update successful!');
        if (startDate && endDate) {
          fetchLockerStatus(startDate, endDate);
        }
      }
    } catch (error) {
      console.error('Update failed:', error);
      alert('Update failed, please try again later!');
    }
    setShowModal(false);
  };

  const handleAdd = async () => {
    try {
      const response = await axios.post(`http://localhost:8080/api/lockers`, null, {
        params: {
          lockerId: locker.lockerId,
          capacity: editedCapacity
        }
      });
      if (response.status === 200) {
        alert('New locker added successfully!');
        if (startDate && endDate) {
          fetchLockerStatus(startDate, endDate);
        }
      }
    } catch (error) {
      console.error('Add failed:', error);
      alert('Add failed, please try again later!');
    }
    setShowModal(false);
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this locker?')) {
      try {
        const response = await axios.delete(`http://localhost:8080/api/lockers/${locker.lockerId}`);
        if (response.status === 200) {
          alert('Locker deleted successfully!');
          if (startDate && endDate) {
            fetchLockerStatus(startDate, endDate);
          }
        }
      } catch (error) {
        console.error('Delete failed:', error);
        alert('Delete failed, please try again later!');
      }
    }
  };

  const searchUsers = async (query: string) => {
    if (!user?.userId) return; // Ensure adminUserId is available
    try {
      const response = await axios.get(`http://localhost:8080/api/reservations/admin/users/search?query=${query}&adminUserId=${user.userId}`);
      setUsers(response.data);
    } catch (error) {
      console.error('Error searching users:', error);
      setUsers([]);
    }
  };

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const query = event.target.value;
    setSearchQuery(query);
    
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    
    searchTimeoutRef.current = setTimeout(() => {
      searchUsers(query);
    }, 300);
  };

  const handleUserSelect = (user: User) => {
    setSelectedUser(user);
    setUsers([]);
  };

  const handleReserveForUser = async () => {
    if (!selectedUser || !startDate || !endDate || !user?.userId) return;
    
    try {
      const response = await axios.post(`http://localhost:8080/api/reservations/admin?adminUserId=${user.userId}`, {
        lockerId: locker.lockerId,
        userId: selectedUser.userId,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      });
      
      if (response.status === 200) {
        alert('Reservation successful!');
        fetchLockerStatus(startDate, endDate);
        setShowReserveModal(false);
        setSelectedUser(null);
        setSearchQuery('');
      }
    } catch (error) {
      console.error('Error making reservation:', error);
      alert('Reservation failed, please try again later!');
    }
  };

  return (
    <>
      <div
        ref={refCallback}
        className={`p-4 rounded-lg flex flex-col justify-between min-h-[140px] ${
          locker.usability
            ? locker.status === "available"
              ? "bg-emerald-100"
              : "bg-red-100"
            : "bg-gray-200"
        }`}
        onMouseEnter={() => onMouseEnter(locker, index)}
        onMouseLeave={onMouseLeave}
      >
        <div className="flex flex-col space-y-2 flex-grow">
          <div className="flex justify-between items-center">
            <span className="text-lg font-medium">{locker.site}</span>
            <span className={`px-2 py-1 rounded text-sm ${
              locker.usability
                ? locker.status === "available"
                  ? "bg-emerald-200 text-emerald-800"
                  : "bg-red-200 text-red-800"
                : "bg-gray-300 text-gray-600"
            }`}>
              {locker.usability
                ? locker.status === "available"
                  ? "Available"
                  : "Unavailable"
                : "No Locker"}
            </span>
          </div>
          {locker.usability && (
            <div className="space-y-1">
              <div className="flex items-center space-x-2">
                <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
                <span className="text-sm text-gray-600">Capacity: {locker.capacity}</span>
              </div>
              <div className="flex items-start space-x-2">
                <svg className="w-4 h-4 text-gray-500 mt-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                </svg>
                <div className="flex flex-col w-full">
                  <span className="text-sm text-gray-600">Memo:</span>
                  <div className={`text-sm text-gray-600 mt-1 h-20 overflow-y-auto whitespace-pre-line p-1 ${
                    locker.usability
                      ? locker.status === "available"
                        ? "bg-emerald-50"
                        : "bg-red-50"
                      : "bg-gray-100"
                  }`}>
                    {locker.memo.split('\n').map((line, index) => (
                      <div key={index}>
                        {line}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
        <div className="flex space-x-2 mt-3 self-end">
          {locker.usability && locker.status === "available" && (
            <>
              {isAdmin ? (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setShowReserveModal(true);
                  }}
                  className="p-1.5 rounded-full text-blue-600 hover:text-blue-800"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </button>
              ) : (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onLockerClick(locker, index);
                  }}
                  className="p-1.5 rounded-full text-blue-600 hover:text-blue-800"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </button>
              )}
            </>
          )}
          {isAdmin && (
            <button
              onClick={handleOpenModal}
              className="p-1.5 rounded-full text-gray-600 hover:text-gray-800"
            >
              {locker.usability ? (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
              ) : (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" />
                </svg>
              )}
            </button>
          )}
          {isAdmin && locker.usability && locker.status !== "unavailable" && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleDelete();
              }}
              className="p-1.5 rounded-full text-red-600 hover:text-red-800"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          )}
        </div>
      </div>

      <Modal 
        isOpen={showModal} 
        onClose={() => setShowModal(false)} 
        className="max-w-[800px] m-4 p-4 lg:p-11"
      >
        <div className="px-2 pr-14">
          <div className="flex justify-between items-center mb-2">
            <h3 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              {locker.usability ? `Edit Locker ${locker.lockerId}` : 'Add New Locker'}
            </h3>
            {locker.usability && startDate && endDate && (
              <span className="text-sm text-gray-500 whitespace-nowrap">
                {formatDate(startDate)} - {formatDate(endDate)}
              </span>
            )}
          </div>
          <p className="mb-6 text-sm text-gray-500 dark:text-gray-400 lg:mb-7">
            {locker.site}
          </p>
        </div>
        <form className="flex flex-col">
          <div className="px-2 pb-3">
            <div className="grid grid-cols-1 gap-x-3 gap-y-5 lg:grid-cols-2">
              <div>
                <label className="block text-sm font-medium text-gray-700">Capacity</label>
                <input
                  type="number"
                  value={editedCapacity}
                  onChange={(e) => setEditedCapacity(Number(e.target.value))}
                  className="mt-1 block w-full p-2 text-gray-700 border border-gray-300 rounded-md"
                  min="0"
                />
              </div>
              {locker.usability && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Status</label>
                    <select
                      value={editedStatus}
                      onChange={(e) => setEditedStatus(e.target.value)}
                      className="mt-1 block w-full p-2 text-gray-700 border border-gray-300 rounded-md"
                    >
                      <option value="available">Available</option>
                      <option value="unavailable">Unavailable</option>
                    </select>
                  </div>
                  <div className="lg:col-span-2">
                    <label className="block text-sm font-medium text-gray-700">Memo</label>
                    <textarea
                      value={editedMemo}
                      onChange={(e) => setEditedMemo(e.target.value)}
                      className="mt-1 block w-full p-2 text-gray-700 border border-gray-300 rounded-md"
                      rows={3}
                    />
                  </div>
                </>
              )}
            </div>
          </div>
          <div className="text-right">
            <button
              onClick={() => setShowModal(false)}
              type="button"
              className="mt-4 px-6 py-2.5 text-sm font-medium text-gray-700 bg-gray-200 rounded-lg mr-2 hover:bg-gray-300"
            >
              Cancel
            </button>
            <button
              onClick={locker.usability ? handleEdit : handleAdd}
              type="button"
              className="mt-4 px-6 py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700"
            >
              {locker.usability ? 'Update' : 'Add'}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={showReserveModal}
        onClose={() => {
          setShowReserveModal(false);
          setSelectedUser(null);
          setSearchQuery('');
        }}
        className="max-w-[600px] m-4 p-4 lg:p-11"
      >
        <div className="px-2 pr-14">
          <div className="flex justify-between items-center mb-2">
            <h3 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              Admin Reservation
            </h3>
            {startDate && endDate && (
              <span className="text-sm text-gray-500 whitespace-nowrap">
                {formatDate(startDate)} - {formatDate(endDate)}
              </span>
            )}
          </div>
          <p className="mb-6 text-sm text-gray-500 dark:text-gray-400 lg:mb-7">
            {locker.site}
          </p>
        </div>

        <div className="px-2 pb-3">
          <div className="relative">
            <input
              type="text"
              value={searchQuery}
              onChange={handleSearchChange}
              className="w-full p-2 text-gray-700 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Search users by name"
            />
            {users.length > 0 && (
              <div className="absolute z-10 w-full mt-1 top-full bg-white rounded-md shadow-lg max-h-60 overflow-auto">
                {users.map((user) => {
                  return (
                    <div
                      key={user.userId}
                      onClick={() => handleUserSelect(user)}
                      className="px-4 py-2 hover:bg-blue-100 cursor-pointer"
                    >
                      <div className="font-medium">{user.accountName}</div>
                      <div className="text-sm text-gray-500">{user.phoneNumber}</div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
          {selectedUser && (
            <div className="mt-4 p-3 bg-blue-100 rounded-md">
              <div className="text-sm text-gray-600">Selected User:</div>
              <div className="mt-1">
                <div className="font-medium">{selectedUser.accountName}</div>
                <div className="text-sm text-gray-500">{selectedUser.phoneNumber}</div>
              </div>
            </div>
          )}
        </div>

        <div className="text-right">
          <button
            onClick={() => {
              setShowReserveModal(false);
              setSelectedUser(null);
              setSearchQuery('');
            }}
            type="button"
            className="mt-4 px-6 py-2.5 text-sm font-medium text-gray-700 bg-gray-200 rounded-lg mr-2 hover:bg-gray-300"
          >
            Cancel
          </button>
          <button
            onClick={handleReserveForUser}
            type="button"
            disabled={!selectedUser}
            className={`mt-4 px-6 py-2.5 text-sm font-medium text-white rounded-lg ${
              selectedUser ? 'bg-blue-600 hover:bg-blue-700' : 'bg-gray-400 cursor-not-allowed'
            }`}
          >
            Make Reservation
          </button>
        </div>
      </Modal>
    </>
  );
};

export default LockerCard; 