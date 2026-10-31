package com.hotel.model;

/** Room entity. Status is one of: Available, Booked, Occupied. */
public class Room {
    private int roomId;
    private String roomNumber = "";
    private String roomType = "Single";
    private double price;
    private int capacity;
    private String status = "Available";

    public Room() { }
    public Room(int roomId, String roomNumber, String roomType, double price, int capacity, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.capacity = capacity;
        this.status = status;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Used in combo boxes. */
    @Override
    public String toString() { return roomNumber + " - " + roomType + " (Rs." + price + ")"; }
}
