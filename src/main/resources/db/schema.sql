CREATE TABLE students (
    roll_no INT PRIMARY KEY,
    class_number TINYINT NOT NULL,
    section CHAR(1) NOT NULL,
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    parent_name VARCHAR(100),
    parent_phone VARCHAR(20),
    address TEXT
);

CREATE TABLE attendance (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roll_no INT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE') NOT NULL,
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (roll_no)
        REFERENCES students(roll_no),

    UNIQUE (roll_no, attendance_date)
);
