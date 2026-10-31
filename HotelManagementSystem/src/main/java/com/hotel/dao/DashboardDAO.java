package com.hotel.dao;

/** Counts for the dashboard cards. */
public class DashboardDAO extends BaseDAO {
    public int totalRooms() throws Exception { return count("SELECT COUNT(*) FROM rooms"); }
    public int availableRooms() throws Exception { return count("SELECT COUNT(*) FROM rooms WHERE status='Available'"); }
    public int occupiedRooms() throws Exception { return count("SELECT COUNT(*) FROM rooms WHERE status='Occupied'"); }
    public int bookedRooms() throws Exception { return count("SELECT COUNT(*) FROM rooms WHERE status='Booked'"); }
    public int totalCustomers() throws Exception { return count("SELECT COUNT(*) FROM customers"); }
    public int totalBookings() throws Exception { return count("SELECT COUNT(*) FROM bookings"); }
}
