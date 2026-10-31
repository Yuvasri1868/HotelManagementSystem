package com.hotel.model;

import java.time.LocalDate;

/** Booking entity. Status: Booked, Checked-In, Checked-Out, Cancelled. */
public class Booking {
    private int bookingId;
    private String bookingCode = "";
    private int customerId;
    private int roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double totalAmount;
    private double finalBill;
    private String status = "Booked";

    public Booking() { }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getFinalBill() { return finalBill; }
    public void setFinalBill(double finalBill) { this.finalBill = finalBill; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
