package com.school.attendance;

import com.school.attendance.database.DatabaseConnection;
import java.sql.Connection;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Database connection successful!");
        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
