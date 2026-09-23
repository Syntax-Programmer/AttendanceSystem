-- ==========================================================
-- School Attendance System — Database Schema
-- For fresh installations, run this entire file.
-- For existing installations, see MIGRATION section below.
-- ==========================================================

CREATE TABLE IF NOT EXISTS students (
    roll_no INT PRIMARY KEY,
    class_number TINYINT NOT NULL,
    section CHAR(1) NOT NULL,
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    parent_name VARCHAR(100),
    parent_phone VARCHAR(20),
    address TEXT,
    parent_email VARCHAR(150)           -- parent email for attendance notifications
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roll_no INT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE') NOT NULL,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (roll_no)
        REFERENCES students(roll_no),

    UNIQUE (roll_no, attendance_date)
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('MANAGEMENT', 'FACULTY') NOT NULL
);

CREATE TABLE IF NOT EXISTS faculty_assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    class_number TINYINT NOT NULL,
    section CHAR(1) NOT NULL,
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    UNIQUE (user_id, class_number, section)
);

-- Faculty profile table — linked to users via user_id
-- Stores personal information separately from login credentials.
CREATE TABLE IF NOT EXISTS faculty (
    faculty_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    phone VARCHAR(20),
    email VARCHAR(150),
    address TEXT,
    gender VARCHAR(20),
    qualification VARCHAR(100),
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- ==========================================================
-- MIGRATION — for existing installations only
-- Run these statements if upgrading from an older schema:
-- ==========================================================
--
-- 1. Add parent_email to students (if not present):
--    ALTER TABLE students ADD COLUMN IF NOT EXISTS parent_email VARCHAR(150);
--
-- 2. Create faculty table (if not present):
--    CREATE TABLE IF NOT EXISTS faculty (
--        faculty_id INT AUTO_INCREMENT PRIMARY KEY,
--        user_id INT NOT NULL UNIQUE,
--        name VARCHAR(100) NOT NULL,
--        date_of_birth DATE,
--        phone VARCHAR(20),
--        email VARCHAR(150),
--        address TEXT,
--        gender VARCHAR(20),
--        qualification VARCHAR(100),
--        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
--    );
-- ==========================================================
