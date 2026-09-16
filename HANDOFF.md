# Project Handoff

## Current Status
The project is **feature-complete**. All planned features are implemented and compile successfully.
Run with: `mvn javafx:run`

## Completed
- Management Dashboard with sidebar navigation
- Management: Student search, attendance marking, reports (with CSV export), faculty management
- Faculty login and Dashboard with assigned class cards
- Faculty: Students page (only shows students from assigned classes)
- Faculty: Attendance page (select class/date, mark PRESENT/ABSENT/LATE, duplicate handled)
- Faculty: Reports page (filter by date/class/section, CSV export, stats)
- Faculty: Open Class screen (from dashboard class cards)
- CSV export for both Management and Faculty reports (FileChooser, proper escaping)
- QR code generation per student (ZXing)
- HTTP server for phone-based QR scanning (port 8765, JSON responses)
- Phone scanning: QR encodes full URL when server running → phone browser marks attendance
- USB barcode scanner support (keyboard wedge → roll number field)
- BCrypt authentication
- Logout from both dashboards

## Architecture

```
UI (JavaFX)
 ↓
Service (business logic, security checks)
 ↓
Repository (JDBC SQL)
 ↓
MariaDB
```

### Key Classes

| Layer      | Class                          | Purpose                                      |
|------------|--------------------------------|----------------------------------------------|
| UI         | LoginScreen                    | Login for both roles                         |
| UI         | ManagementDashboard            | Wrapper → Dashboard                          |
| UI         | Dashboard                      | Management sidebar + content area            |
| UI         | AttendanceScreen               | Management: mark attendance by roll number   |
| UI         | StudentSearch                  | Management: search/add students              |
| UI         | Reports                        | Management: attendance reports + CSV export  |
| UI         | FacultyManagement              | Management: create/delete/assign faculty     |
| UI         | QrCodeScreen                   | QR generation + HTTP server control          |
| UI         | FacultyDashboard               | Faculty sidebar + class cards                |
| UI         | FacultyClassScreen             | Faculty: view students in a class (from card)|
| UI         | FacultyStudentsScreen          | Faculty: all students across assigned classes|
| UI         | FacultyAttendanceScreen        | Faculty: mark attendance for assigned class  |
| UI         | FacultyReportsScreen           | Faculty: reports + CSV export (restricted)   |
| Service    | AttendanceService              | Mark, update, query attendance               |
| Service    | StudentService                 | Student CRUD                                 |
| Service    | AuthService                    | BCrypt login                                 |
| Service    | FacultyAssignmentService       | Manage class assignments                     |
| Service    | BarcodeService                 | ZXing QR generation                         |
| Service    | AttendanceHttpServer           | Lightweight HTTP server for phone scanning   |
| Repository | AttendanceRepository           | Attendance SQL queries                       |
| Repository | StudentRepository              | Student SQL queries                          |
| Repository | UserRepository                 | User SQL queries                             |
| Repository | FacultyAssignmentRepository    | Assignment SQL queries                       |
| Database   | DatabaseConnection             | MariaDB connection via .env                  |

## Important Database Rules
- `roll_no` is the student **primary key** — there is no `student_id`
- There is **no barcode field** in the students table
- Attendance is unique per `(roll_no, attendance_date)` — enforced by DB constraint
- Faculty access is **assignment-based**: faculty can only see/mark attendance for classes in `faculty_assignments`
- Assignment check is enforced in application logic (service layer), not just UI

## Database Schema Summary
```sql
students (roll_no PK, class_number, section, name, date_of_birth, gender, parent_name, parent_phone, address)
attendance (attendance_id PK, roll_no FK, attendance_date, status ENUM(PRESENT/ABSENT/LATE), marked_at)
users (user_id PK, username, password_hash, role ENUM(MANAGEMENT/FACULTY))
faculty_assignments (assignment_id PK, user_id FK, class_number, section, UNIQUE(user_id,class_number,section))
```

## QR / Phone Scanning Architecture
- **BarcodeService**: generates QR codes using ZXing
- **AttendanceHttpServer**: lightweight `com.sun.net.httpserver.HttpServer` on port 8765
- **Workflow**:
  1. Management opens QR Codes screen
  2. Starts HTTP server
  3. Enters student roll number → QR generates with URL: `http://<server-ip>:8765/mark?roll=<rollNo>`
  4. Student/teacher scans QR with phone → phone browser hits the URL
  5. Server marks attendance (PRESENT by default), returns JSON
  6. Optional: add `&status=ABSENT` or `&status=LATE` to URL
- **GET /mark?roll=N[&status=X]** — marks attendance, returns `{"success":true,"message":"..."}`
- **GET /ping** — health check, returns `{"status":"ok"}`
- Phone and computer must be on the same network (WiFi)

## Configuration
- `.env` file in project root: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- Default port: 8765 (defined as `AttendanceHttpServer.PORT`)

## Last Completed Task
Fixed all bugs found during code review:
1. Faculty login was missing CSS stylesheet application
2. Dashboard "View Reports" quick action navigated to placeholder instead of Reports screen
3. FacultyDashboard had duplicate `Scene` import
4. QrCodeScreen now encodes full URL in QR when server is running (phone-scannable)
5. BarcodeService added `generateQrCodeFromString()` for URL-encoded QR
6. FacultyAttendanceScreen and FacultyReportsScreen missing padding
7. Fixed deprecated `TableView.CONSTRAINED_RESIZE_POLICY` → `CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS`

## Known Issues
- None. Build is clean (31 files compile, BUILD SUCCESS).
- Minor unchecked generic warning from JavaFX `TableCell<>()` anonymous class — harmless, standard JavaFX pattern.

## Next Action
None required. The project is complete. To run: `mvn javafx:run`

## Verification
```
mvn clean compile → BUILD SUCCESS
31 source files compiled
No errors
Date: 2026-09-16
```
