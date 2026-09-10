package com.school.attendance.model;

import java.time.LocalDate;

public class Student {

    private int rollNo;
    private int classNumber;
    private String section;
    private String name;
    private LocalDate dateOfBirth;
    private String gender;
    private String parentName;
    private String parentPhone;
    private String address;

    public Student() {}

    public Student(
        int rollNo,
        int classNumber,
        String section,
        String name,
        LocalDate dateOfBirth,
        String gender,
        String parentName,
        String parentPhone,
        String address
    ) {
        this.rollNo = rollNo;
        this.classNumber = classNumber;
        this.section = section;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.address = address;
    }

    public int getRollNo() {
        return rollNo;
    }

    public void setRollNo(int rollNo) {
        this.rollNo = rollNo;
    }

    public int getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(int classNumber) {
        this.classNumber = classNumber;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
