-- OPTIONAL: the app creates all of this automatically on first run.
CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

CREATE TABLE IF NOT EXISTS admin (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,      -- SHA-256 hash
  full_name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS rooms (
  room_id INT AUTO_INCREMENT PRIMARY KEY,
  room_number VARCHAR(10) NOT NULL UNIQUE,
  room_type VARCHAR(20) NOT NULL,      -- Single, Double, Deluxe, Suite
  price DECIMAL(10,2) NOT NULL,
  capacity INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'Available'  -- Available, Booked, Occupied
);
CREATE TABLE IF NOT EXISTS customers (
  customer_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  email VARCHAR(100),
  address VARCHAR(255),
  id_proof VARCHAR(100)
);
CREATE TABLE IF NOT EXISTS bookings (
  booking_id INT AUTO_INCREMENT PRIMARY KEY,
  booking_code VARCHAR(20) UNIQUE,
  customer_id INT NOT NULL,
  room_id INT NOT NULL,
  check_in DATE NOT NULL,
  check_out DATE NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  final_bill DECIMAL(10,2) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'Booked', -- Booked, Checked-In, Checked-Out, Cancelled
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);
CREATE TABLE IF NOT EXISTS payments (
  payment_id INT AUTO_INCREMENT PRIMARY KEY,
  receipt_no VARCHAR(20) UNIQUE,
  booking_id INT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  method VARCHAR(10) NOT NULL,         -- Cash, UPI, Card
  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);
-- Default login: admin / admin123 (created by the app)
