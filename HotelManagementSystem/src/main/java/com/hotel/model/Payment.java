package com.hotel.model;

/** Payment entity. Method: Cash, UPI or Card. */
public class Payment {
    private int paymentId;
    private String receiptNo = "";
    private int bookingId;
    private double amount;
    private String method = "Cash";

    public Payment() { }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
