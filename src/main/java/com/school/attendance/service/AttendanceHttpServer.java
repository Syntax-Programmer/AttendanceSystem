package com.school.attendance.service;

import com.school.attendance.model.AttendanceStatus;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.Executors;

/**
 * Lightweight HTTP server that allows phones to submit QR scan results.
 *
 * Workflow:
 *   1. Faculty displays QR code for a student (showing roll number).
 *   2. Phone user scans QR code and is directed to http://<server-ip>:8765/mark?roll=<rollNo>
 *   3. This server marks the student present and returns a JSON response.
 *
 * Endpoints:
 *   GET  /mark?roll=<rollNo>[&status=PRESENT|ABSENT|LATE]
 *        Marks attendance. Default status: PRESENT.
 *        Returns: {"success": true, "message": "..."} or {"success": false, "error": "..."}
 *
 *   GET  /ping
 *        Returns {"status": "ok"} — for connectivity check.
 *
 * The server runs on port 8765 by default.
 * It is started via startServer() and stopped via stopServer().
 */
public class AttendanceHttpServer {

    public static final int PORT = 8765;

    private HttpServer server;
    private final AttendanceService attendanceService;

    public AttendanceHttpServer() {
        StudentRepository studentRepository = new StudentRepository();
        AttendanceRepository attendanceRepository = new AttendanceRepository();
        this.attendanceService = new AttendanceService(studentRepository, attendanceRepository);
    }

    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/mark", this::handleMark);
        server.createContext("/ping", this::handlePing);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("[AttendanceHttpServer] Started on port " + PORT);
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("[AttendanceHttpServer] Stopped.");
        }
    }

    public boolean isRunning() {
        return server != null;
    }

    private void handleMark(HttpExchange exchange) throws IOException {
        // CORS headers for mobile browser access
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"success\":false,\"error\":\"Method not allowed\"}");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            sendJson(exchange, 400, "{\"success\":false,\"error\":\"Missing parameters\"}");
            return;
        }

        String rollParam = getQueryParam(query, "roll");
        String statusParam = getQueryParam(query, "status");

        if (rollParam == null || rollParam.isBlank()) {
            sendJson(exchange, 400, "{\"success\":false,\"error\":\"Missing roll parameter\"}");
            return;
        }

        int rollNo;
        try {
            rollNo = Integer.parseInt(rollParam.trim());
            if (rollNo <= 0) throw new NumberFormatException("non-positive");
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"success\":false,\"error\":\"Invalid roll number\"}");
            return;
        }

        AttendanceStatus status = AttendanceStatus.PRESENT;
        if (statusParam != null && !statusParam.isBlank()) {
            try {
                status = AttendanceStatus.valueOf(statusParam.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400,
                    "{\"success\":false,\"error\":\"Invalid status. Use PRESENT, ABSENT, or LATE\"}");
                return;
            }
        }

        try {
            // Check student exists
            var student = attendanceService.findStudent(rollNo);
            if (student.isEmpty()) {
                sendJson(exchange, 404,
                    "{\"success\":false,\"error\":\"Student with roll number " + rollNo + " not found\"}");
                return;
            }
            String studentName = student.get().getName();

            // Mark or update attendance for today
            attendanceService.markOrUpdateAttendance(rollNo, LocalDate.now(), status);

            String msg = "Attendance marked: " + studentName + " (" + rollNo + ") -> " + status.name();
            sendJson(exchange, 200,
                "{\"success\":true,\"message\":\"" + escapeJson(msg) + "\"," +
                "\"student\":\"" + escapeJson(studentName) + "\"," +
                "\"roll\":" + rollNo + "," +
                "\"status\":\"" + status.name() + "\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500,
                "{\"success\":false,\"error\":\"Database error: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handlePing(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        sendJson(exchange, 200, "{\"status\":\"ok\",\"service\":\"AttendanceServer\"}");
    }

    private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getQueryParam(String query, String paramName) {
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(paramName)) {
                return kv[1];
            }
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
