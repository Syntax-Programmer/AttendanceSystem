package com.school.attendance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Attendance {

    private long attendanceId;
    private int rollNo;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private LocalDateTime markedAt;

    public Attendance() {}

    public Attendance(int rollNo, LocalDate attendanceDate, AttendanceStatus status) {
        this.rollNo = rollNo;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getRollNo() {
        return rollNo;
    }

    public void setRollNo(int rollNo) {
        this.rollNo = rollNo;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
}
