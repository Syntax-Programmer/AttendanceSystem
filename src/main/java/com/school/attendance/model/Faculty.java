package com.school.attendance.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Faculty profile data linked to a users record via user_id.
 * Login credentials remain in the users table.
 */
public class Faculty {

    private int facultyId;
    private int userId;
    private String name;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private String gender;
    private String qualification;

    public Faculty() {}

    public Faculty(
        int facultyId,
        int userId,
        String name,
        LocalDate dateOfBirth,
        String phone,
        String email,
        String address,
        String gender,
        String qualification
    ) {
        this.facultyId = facultyId;
        this.userId = userId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.qualification = qualification;
    }

    // --- Getters and Setters ---

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    /**
     * Calculates age from date_of_birth. Returns -1 if DOB is null.
     */
    public int getAge() {
        if (dateOfBirth == null) return -1;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
