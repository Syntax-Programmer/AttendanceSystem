package com.school.attendance.model;

public class FacultyAssignment {

    private int assignmentId;
    private int userId;
    private int classNumber;
    private String section;

    public FacultyAssignment() {}

    public FacultyAssignment(int assignmentId, int userId, int classNumber, String section) {
        this.assignmentId = assignmentId;
        this.userId = userId;
        this.classNumber = classNumber;
        this.section = section;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
}
