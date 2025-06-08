"use client";
import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../components/ui/table";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css"; 
import { useUser } from "../../context/UserContext";
import { format } from "date-fns";
import { zhTW } from "date-fns/locale"; 
import { Modal } from "../../components/ui/modal/index"; 

interface User {
  userId: number;
  accountName: string | null;
  phoneNumber: string | null;
}

interface Locker {
  lockerId: number;
  site: string;
  capacity: number;
  usability: boolean;
}

interface Reservation {
  id: number;
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
  const isAdmin = user?.isAdmin;
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
  const [selectedStartDate, setSelectedStartDate] = useState<Date | null>(null);
  const [selectedEndDate, setSelectedEndDate] = useState<Date | null>(null);
  const [showDialog, setShowDialog] = useState<boolean>(false);
  const [activeBarcodeId, setActiveBarcodeId] = useState<number | null>(null);

  // Admin-specific state for searching users
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<User | null>(null);
  const searchTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

  const fetchReservations = async (targetUserId: number, adminUserId: number | undefined) => {
    try {
      let url = '';
      if (isAdmin && adminUserId) {
        url = `http://localhost:8080/api/reservations/admin/${targetUserId}?adminUserId=${adminUserId}`;
      } else {
        url = `http://localhost:8080/api/reservations/user/${targetUserId}`;
      }
      const response = await axios.get(url);
      console.log("Fetched reservations:", response.data);
      setReservations(response.data);
    } catch (error) {
      console.error("Error fetching reservations:", error);
      setReservations([]);
    }
  };

  useEffect(() => {
    if (isAdmin && selectedCustomer) {
      fetchReservations(selectedCustomer.userId, user?.userId);
    } else if (!isAdmin && user) {
      fetchReservations(user.userId, undefined);
    } else if (isAdmin && !selectedCustomer) {
        setReservations([]); // Clear reservations if no customer is selected by admin
    }
  }, [user, isAdmin, selectedCustomer]);

  const searchUsers = async (query: string) => {
    if (!user?.userId) return; // Ensure adminUserId is available
    try {
      const response = await axios.get(`http://localhost:8080/api/reservations/admin/users/search?query=${query}&adminUserId=${user.userId}`);
      setSearchResults(response.data);
    } catch (error) {
      console.error('Error searching users:', error);
      setSearchResults([]);
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

  const handleCustomerSelect = (customer: User) => {
    setSelectedCustomer(customer);
    setSearchQuery(''); // Clear search query
    setSearchResults([]); // Clear search results
  };

  const handleDelete = async (id: number) => {
    if (window.confirm("Are you sure you want to delete this reservation?")) {
      try {
        let url = '';
        if (isAdmin && user?.userId) {
          url = `http://localhost:8080/api/reservations/admin/${id}?adminUserId=${user.userId}`;
        } else if (user?.userId) {
          url = `http://localhost:8080/api/reservations/${id}`;
        } else {
            alert('User not logged in or admin ID not available.');
            return;
        }

        const response = await axios.delete(url);
        if (response.status === 200) {
          setReservations(reservations.filter((res) => res.id !== id));
          alert("Reservation deleted!");
          if (isAdmin && selectedCustomer) {
            fetchReservations(selectedCustomer.userId, user?.userId); // Refresh for admin
          } else if (!isAdmin && user) {
            fetchReservations(user.userId, undefined);
          }
        }
      } catch (error) {
        console.error("Error deleting reservation:", error);
        alert("Failed to delete reservation!");
      }
    }
  };

  const openEditDialog = (reservation: Reservation) => {
    setSelectedReservation(reservation);
    setStartDate(new Date(reservation.startDate));
    setEndDate(new Date(reservation.endDate));
    setSelectedStartDate(new Date(reservation.startDate));
    setSelectedEndDate(new Date(reservation.endDate));
    setShowDialog(true);
  };

  const closeDialog = () => {
    setShowDialog(false);
  };

  const handleEdit = async () => {
    if (selectedStartDate && selectedEndDate && selectedReservation && user) {
      const formattedStartDate = format(selectedStartDate, "yyyy-MM-dd");
      const formattedEndDate = format(selectedEndDate, "yyyy-MM-dd");

      const isValidDateRange = (startDate: Date, endDate: Date) => {
        return startDate <= endDate;
      };

      if (!isValidDateRange(selectedStartDate, selectedEndDate)) {
        alert("End time cannot be earlier than start time!");
        return;
      }
      setStartDate(selectedStartDate);
      setEndDate(selectedEndDate);

      try {
        let url = '';
        if (isAdmin && user.userId) {
            url = `http://localhost:8080/api/reservations/admin/${selectedReservation.id}/dates?adminUserId=${user.userId}`;
        } else {
            url = `http://localhost:8080/api/reservations/${selectedReservation.id}/dates`;
        }
        
        const response = await axios.put(
          url,
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
            reservation.id === selectedReservation.id
              ? { ...reservation, startDate: formattedStartDate, endDate: formattedEndDate }
              : reservation
          )
        );
        alert("Reservation updated!");
        closeDialog();
        if (isAdmin && selectedCustomer) {
          fetchReservations(selectedCustomer.userId, user.userId); // Refresh for admin
        } else if (!isAdmin) {
          fetchReservations(user.userId, undefined); // Refresh for regular user
        }
      } catch (error) {
        console.error("Error updating reservation:", error);
        alert("Failed to update reservation!");
      }
    }
  };

  const handleStartDateChange = (date: Date | null) => {
    if (date) {
      setSelectedStartDate(date);
      if (selectedEndDate && date > selectedEndDate) {
        setSelectedEndDate(date);
      }
    }
  };

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
      {isAdmin && (
        <div className="p-5 border-b border-gray-200 dark:border-white/[0.05]">
          {/* <h2 className="text-xl font-semibold text-gray-800 dark:text-white/90 mb-4">Admin User Search</h2> */}
          <div className="relative mb-4">
            <input
              type="text"
              value={searchQuery}
              onChange={handleSearchChange}
              className="w-full p-2 text-gray-700 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Search customers by name "
            />
            {searchResults.length > 0 && (
              <div className="absolute z-10 w-full mt-1 top-full bg-white rounded-md shadow-lg max-h-60 overflow-auto">
                {searchResults.map((customer) => (
                  <div
                    key={customer.userId}
                    onClick={() => handleCustomerSelect(customer)}
                    className="px-4 py-2 hover:bg-blue-100 cursor-pointer"
                  >
                    <div className="font-medium">{customer.accountName}</div>
                    <div className="text-sm text-gray-500">{customer.phoneNumber}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
          {selectedCustomer && (
            <div className="mt-4 p-3 bg-blue-100 rounded-md">
              <div className="text-sm text-gray-600">Selected Customer:</div>
              <div className="mt-1">
                <div className="font-medium">{selectedCustomer.accountName}</div>
                <div className="text-sm text-gray-500">{selectedCustomer.phoneNumber}</div>
              </div>
            </div>
          )}
          {/* {!selectedCustomer && (
            <p className="text-gray-500">Please search for and select a customer to view their reservations.</p>
          )} */}
        </div>
      )}
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
                <TableRow key={reservation.id}>
                  <TableCell className="px-5 py-4 sm:px-6 text-start">{reservation.locker.site}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.locker.capacity}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.locker.usability ? "Yes" : "No"}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.startDate}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">{reservation.endDate}</TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    {reservation.barcode ? (
                      (() => {
                        const endDate = new Date(reservation.endDate);
                        const today = new Date();
                        endDate.setHours(0, 0, 0, 0);
                        today.setHours(0, 0, 0, 0);
                        
                        return endDate < today ? (
                          <span className="text-red-500">Expired</span>
                        ) : (
                          <div className="relative">
                            <button
                              className="flex w-full items-center justify-center gap-2 rounded-full border border-gray-300 bg-white px-4 py-3 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200 lg:inline-flex lg:w-auto"
                              onClick={() => {
                                if (activeBarcodeId === reservation.id) {
                                  setActiveBarcodeId(null);
                                } else {
                                  setActiveBarcodeId(reservation.id);
                                }
                              }}
                            >
                              {activeBarcodeId === reservation.id ? 'Hide Barcode' : 'Show Barcode'}
                            </button>
                            {activeBarcodeId === reservation.id && (
                              <img
                                src={`data:image/png;base64,${reservation.barcode}`}
                                alt="Reservation Barcode"
                                className="w-[300px] h-[150px] fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50"
                              />
                            )}
                          </div>
                        );
                      })()
                    ) : (
                      "N/A"
                    )}
                  </TableCell>
                  <TableCell className="px-4 py-3 text-gray-500 text-start text-theme-sm dark:text-gray-400">
                    <button
                      onClick={() => handleDelete(reservation.id)}
                      disabled={!isAdmin && (() => {
                        const endDate = new Date(reservation.endDate);
                        const today = new Date();
                        endDate.setHours(0, 0, 0, 0);
                        today.setHours(0, 0, 0, 0);
                        return endDate < today;
                      })()}
                      className={`px-4 py-2 rounded mr-2 ${((endDate, today) => {
                        endDate.setHours(0,0,0,0);
                        today.setHours(0,0,0,0);
                        return (!isAdmin && endDate < today) ? "bg-gray-300 text-gray-500 cursor-not-allowed" : "bg-red-500 text-white hover:bg-red-600";
                      })(new Date(reservation.endDate), new Date())}`}
                    >
                      Delete
                    </button>
                    <button
                      onClick={() => openEditDialog(reservation)}
                      disabled={(() => {
                        const endDate = new Date(reservation.endDate);
                        const today = new Date();
                        endDate.setHours(0, 0, 0, 0);
                        today.setHours(0, 0, 0, 0);
                        return endDate < today;
                      })()}
                      className={`px-4 py-2 rounded mr-2 ${((endDate, today) => {
                        endDate.setHours(0,0,0,0);
                        today.setHours(0,0,0,0);
                        return (endDate < today) ? "bg-gray-300 text-gray-500 cursor-not-allowed" : "bg-blue-500 text-white hover:bg-blue-600";
                      })(new Date(reservation.endDate), new Date())}`}
                    >
                      Update
                    </button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </div>

      <Modal isOpen={showDialog} onClose={closeDialog} className="max-w-[700px] m-4">
        <div className="relative w-full max-w-[700px] rounded-3xl bg-white p-4 dark:bg-gray-900 lg:p-11">
          <div className="px-2 pr-14">
            <h3 className="mb-2 text-2xl font-semibold text-gray-800 dark:text-white/90">
              Update Reservation
            </h3>
            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400 lg:mb-7">
              Adjusting dates can only be done within the originally selected range
            </p>
          </div>
          <form className="flex flex-col">
            <div className="px-2 pb-3">
              <div className="grid grid-cols-1 gap-x-3 gap-y-5 lg:grid-cols-2">
                <div><label>Start Date</label>
                  <DatePicker
                    selected={selectedStartDate}
                    onChange={handleStartDateChange}
                    minDate={startDate || undefined}
                    maxDate={endDate || undefined}
                    dateFormat="yyyy/MM/dd"
                    className="block w-full p-2 text-center text-gray-700 border border-gray-300 rounded-md"
                  />
                </div>
                <div><label>End Date</label>
                  <DatePicker
                    selected={selectedEndDate}
                    onChange={handleEndDateChange}
                    minDate={startDate || undefined}
                    maxDate={endDate || undefined}
                    dateFormat="yyyy/MM/dd"
                    className="block w-full p-2 text-center text-gray-700 border border-gray-300 rounded-md"
                  />
                </div>
              </div>
            </div>
            <div className="text-right">
              <button
                onClick={handleEdit}
                type="button"
                className="mt-4 px-6 py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg"
              >
                Update
              </button>
            </div>
          </form>
        </div>
      </Modal>
    </div>
  );
}
