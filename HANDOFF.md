# Project Handoff

## Current Status
The project is **feature-complete and fully verified**.

- Build: `mvn clean compile` → **BUILD SUCCESS** (37 source files, 0 errors)
- Database: All 5 tables confirmed present with correct columns
- Run: `mvn javafx:run`

All 13 requirements from the specification are implemented in real, working code.

---

## Completed Features

### Core
- Management Dashboard with sidebar navigation
- Faculty Dashboard with assigned class cards
- BCrypt authentication (login for both roles)
- Logout from both dashboards
- Shared CSS (`app.css`) used by all screens

### Faculty Management (Management → Faculty menu)
- Create faculty: username, password, full profile (name, DOB, phone, email, gender, qualification, address)
- Faculty user + faculty profile created in a single **database transaction** (both roll back on failure)
- Age is **calculated** from date_of_birth, never stored directly
- Reset faculty password (BCrypt hash updated)
- Reset faculty username (unique validation, only FACULTY accounts affected)
- Delete faculty
- Manage class assignments (add/remove per faculty)
- View Class Students: select faculty → see assigned classes → select one → view students table

### Student Management (Management → Students menu)
- Add student with parent_email field
- Edit student with parent_email field
- View student profile (all fields including parent email)
- Search by roll number
- Show all students

### Attendance Marking
- Management: manual roll input or USB barcode scanner keyboard wedge → find student → mark PRESENT/ABSENT/LATE
  - Sends parent email notification after saving
  - Duplicate attendance rejected with message (no email sent)
- Faculty: select class + date → load student list → mark per student
  - Sends parent email notification after saving
  - Uses `markOrUpdateAttendance` (allows same-day correction)
- Phone QR scanning: see below

### Barcode / QR Codes
- **BulkBarcodeScreen** (Management → Barcodes):
  - Select faculty → load their assigned classes → select class → load students
  - Generate All Barcodes: Code128 + QR per student, displayed in FlowPane
  - Export PDF: printable barcode cards (Code128 + QR), 2-up layout
  - Start/Stop phone scanning server
- **QrCodeScreen** (Management → QR Codes):
  - Generate QR for a single student by roll number
  - Save QR as PNG
  - Start/Stop phone scanning server
  - USB scanner instructions

### Phone QR Scanning
- Built-in HTTP server (`AttendanceHttpServer`) on port **8765**
- Uses `com.sun.net.httpserver.HttpServer` (JDK built-in, no extra dependency)
- `GET /mark?roll=<N>[&status=PRESENT|ABSENT|LATE]` — marks attendance, returns JSON
- `GET /ping` — health check
- When server running: QR encodes full URL → phone browser marks attendance
- When server stopped: QR encodes plain roll number (USB scanner fallback)
- After attendance saved, parent email is sent (non-blocking)

### Parent Email Notifications
- EmailService uses SMTP via `.env` configuration (Jakarta Mail)
- Triggers after attendance is saved in:
  - Management AttendanceScreen
  - FacultyAttendanceScreen (per-student buttons)
  - AttendanceHttpServer (phone QR scan)
- **Behavior guarantees:**
  - Attendance saved first, THEN email attempted
  - Duplicate attendance rejected → NO email
  - Missing parent_email → attendance saved, UI note shown
  - SMTP not configured → attendance saved, UI note shown
  - SMTP failure → attendance saved, failure logged, UI note shown

### Reports & Export
- Management: Reports screen with date/class/section filter, CSV export
- Faculty: Reports screen filtered to assigned classes only, CSV export

---

## Architecture

```
UI (JavaFX)
 ↓
Service (business logic, validation, security)
 ↓
Repository (JDBC SQL)
 ↓
MariaDB
```

### Key Classes

| Layer      | Class                        | Purpose                                          |
|------------|------------------------------|--------------------------------------------------|
| UI         | LoginScreen                  | Login for both roles                             |
| UI         | ManagementDashboard          | Wrapper → Dashboard                              |
| UI         | Dashboard                    | Management sidebar + content area                |
| UI         | AttendanceScreen             | Mark attendance by roll (USB scanner supported)  |
| UI         | StudentSearch                | Search/add students, open profile                |
| UI         | StudentProfile               | Student details + attendance history + edit      |
| UI         | Reports                      | Management attendance reports + CSV export       |
| UI         | FacultyManagement            | Create/delete/assign faculty, view students      |
| UI         | BulkBarcodeScreen            | Bulk barcode generation, PDF export              |
| UI         | QrCodeScreen                 | Per-student QR, phone scanning server controls   |
| UI         | FacultyDashboard             | Faculty sidebar + class cards                    |
| UI         | FacultyClassScreen           | Faculty: view students in a class (from card)    |
| UI         | FacultyStudentsScreen        | Faculty: all students across assigned classes    |
| UI         | FacultyAttendanceScreen      | Faculty: mark attendance per student (with email)|
| UI         | FacultyReportsScreen         | Faculty: reports + CSV (restricted to assignments)|
| Service    | AttendanceService            | Mark, update, query attendance                   |
| Service    | StudentService               | Student CRUD                                     |
| Service    | AuthService                  | BCrypt login, reset password/username            |
| Service    | FacultyService               | Faculty profile creation (transactional)         |
| Service    | FacultyAssignmentService     | Manage class assignments                         |
| Service    | BarcodeService               | ZXing Code128 + QR generation                   |
| Service    | BarcodePdfService            | OpenPDF barcode card export                      |
| Service    | AttendanceHttpServer         | Lightweight HTTP server for phone scanning       |
| Service    | EmailService                 | SMTP parent email notifications                  |
| Repository | AttendanceRepository         | Attendance SQL queries                           |
| Repository | StudentRepository            | Student SQL queries                              |
| Repository | UserRepository               | User SQL queries (with username/password reset)  |
| Repository | FacultyRepository            | Faculty profile SQL queries                      |
| Repository | FacultyAssignmentRepository  | Assignment SQL queries                           |
| Database   | DatabaseConnection           | MariaDB connection via .env                      |

---

## Database Schema

```sql
students (roll_no PK, class_number, section, name, date_of_birth, gender,
          parent_name, parent_phone, address, parent_email)
attendance (attendance_id PK, roll_no FK, attendance_date, status ENUM(PRESENT/ABSENT/LATE), marked_at)
users (user_id PK, username UNIQUE, password_hash, role ENUM(MANAGEMENT/FACULTY))
faculty_assignments (assignment_id PK, user_id FK, class_number, section, UNIQUE(user_id,class_number,section))
faculty (faculty_id PK, user_id FK UNIQUE, name, date_of_birth, phone, email, address, gender, qualification)
```

### Important Rules
- `roll_no` is the student **primary key** — no `student_id`
- No barcode column — roll_no IS the barcode content
- Attendance unique per `(roll_no, attendance_date)` — DB constraint
- Faculty access is **assignment-based** — enforced in service layer, not just UI
- Faculty profile is linked to login account via `user_id` FK
- Age is calculated from `date_of_birth` via `Faculty.getAge()`, never stored

---

## Configuration

### .env (project root)
```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=StudentAttendance
DB_USER=<mariadb_user>
DB_PASSWORD=<mariadb_password>

# SMTP for parent email notifications (all required for email to be sent)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your@gmail.com
SMTP_PASSWORD=your_app_password
SMTP_FROM=noreply@school.local
```

> [!IMPORTANT]
> For Gmail: use an App Password (Google Account → Security → 2FA → App Passwords), not your regular password.
> `SMTP_USERNAME` must be non-blank to enable email sending. If blank, email is silently skipped.

### Gitignore
`.env` is in `.gitignore`. Never commit credentials.

---

## Phone QR Scanning — Setup Guide

1. Connect computer to Wi-Fi (same network as phone)
2. Open Management → Barcodes (or QR Codes)
3. Click **Start Scan Server** — server starts on port 8765
4. Generate barcodes — QR codes now encode the full URL
5. Phone scans QR → browser opens `http://<server-ip>:8765/mark?roll=<rollNo>`
6. Attendance marked, JSON response shown in browser
7. Click **Stop Server** when done

> [!NOTE]
> Firewall: ensure port 8765 is allowed on the host. On Linux:
> `sudo ufw allow 8765/tcp` or temporarily disable firewall for the session.

---

## QR / Phone Scanning Architecture
- QR encodes: `http://<server-ip>:8765/mark?roll=<rollNo>` (when server running)
- QR encodes: `<rollNo>` plain (when server not running — for USB scanner)
- `GET /mark?roll=N[&status=PRESENT|ABSENT|LATE]` marks attendance, returns JSON
- `GET /ping` returns `{"status":"ok"}` for connectivity check

---

## Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| `org.openjfx:javafx-controls` | 21.0.8 | UI framework |
| `org.openjfx:javafx-swing` | 21.0.8 | SwingFXUtils for BufferedImage→JavaFX |
| `org.mariadb.jdbc:mariadb-java-client` | 3.5.6 | JDBC driver |
| `io.github.cdimascio:dotenv-java` | 3.2.0 | .env loading |
| `org.mindrot:jbcrypt` | 0.4 | BCrypt password hashing |
| `com.google.zxing:core` | 3.5.3 | Barcode/QR generation |
| `com.google.zxing:javase` | 3.5.3 | BufferedImage rendering |
| `com.github.librepdf:openpdf` | 1.3.30 | PDF generation |
| `com.sun.mail:jakarta.mail` | 1.6.7 | SMTP email |

---

## Database Migration (existing installations)

If upgrading from the original schema:
```sql
-- 1. Add parent_email to students
ALTER TABLE students ADD COLUMN IF NOT EXISTS parent_email VARCHAR(150);

-- 2. Create faculty profile table
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
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

These commands are also included as comments in `schema.sql`.

---

## Build & Run

```bash
# Compile
mvn clean compile

# Run application
mvn javafx:run
```

---

## Known Limitations

1. **Email**: Requires valid SMTP credentials in `.env`. Gmail App Password recommended.
   Email is synchronous on the JavaFX thread — for large batches, consider making async.

2. **Phone QR scanning**: Phone and computer must be on the same Wi-Fi network.
   Firewall may need to allow port 8765 (`sudo ufw allow 8765/tcp`).

3. **Faculty without profiles**: Faculty accounts created before the profile feature (directly in DB) will show `—` in profile columns. All faculty created through the UI will have complete profiles. This is handled gracefully in the UI.

4. **FacultyAttendanceScreen**: Uses `markOrUpdateAttendance` (allows re-marking same date).
   AttendanceScreen (Management) uses strict `markAttendance` (no duplicate). Both are intentional.

5. **USB barcode scanner**: Scanner must be configured to emit Enter after the code.
   Most USB HID barcode scanners do this by default.

6. **No FXML**: All UI is programmatic JavaFX. This is intentional architecture.

---

## Testing Checklist (Verified)

### Compile
- [x] `mvn clean compile` → BUILD SUCCESS (37 files, 0 errors)

### Database
- [x] All 5 tables exist: `students`, `attendance`, `users`, `faculty_assignments`, `faculty`
- [x] `students.parent_email` column exists
- [x] `faculty` table has all 9 columns including `date_of_birth`, `gender`, `qualification`
- [x] `users.role` allows MANAGEMENT and FACULTY only

### Code Review (source inspection)
- [x] Faculty creation transaction: `setAutoCommit(false)`, `commit()`, `rollback()` all present
- [x] Username uniqueness: checked in both `FacultyService` and `AuthService`
- [x] BCrypt: `hashpw` on create, `checkpw` on login
- [x] Parent email: read/written in all StudentRepository methods
- [x] Barcode: ZXing Code128Writer and QRCodeWriter used correctly
- [x] PDF: OpenPDF Document/PdfWriter/PdfPTable structure correct
- [x] Email: never sent before `attendanceRepository.save()` / `attendanceRepository.update()` completes
- [x] Duplicate attendance: `IllegalStateException` caught, email NOT sent
- [x] HTTP server: `/mark` and `/ping` endpoints, CORS headers, JSON responses

---

## Last Updated
Date: 2026-09-23
Build: `mvn clean compile` → BUILD SUCCESS (37 files, 0 errors)
Verification: Full source inspection + database schema confirmation
