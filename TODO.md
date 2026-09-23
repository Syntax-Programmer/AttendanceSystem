# School Attendance System — TODO

## ✅ ALL REQUIREMENTS IMPLEMENTED AND VERIFIED

**Last verified:** 2026-09-23
**Build:** `mvn clean compile` → BUILD SUCCESS (37 source files, 0 errors)
**Database:** All tables confirmed present and correct in MariaDB.

---

## COMPLETED FEATURES (fully inspected and verified)

### Faculty Management
- [x] Faculty profile table in database (`faculty` table linked to `users` via `user_id`)
- [x] `Faculty.java` model with: `facultyId`, `userId`, `name`, `dateOfBirth`, `phone`, `email`, `address`, `gender`, `qualification`
- [x] `Faculty.getAge()` — calculates age from date_of_birth at runtime, never stored
- [x] `FacultyRepository` — full CRUD: `save`, `findByUserId`, `findAll`, `update`, `deleteByUserId`
- [x] `FacultyService.createFacultyWithProfile()` — **single database transaction** using `setAutoCommit(false)`; both user and profile rolled back on failure
- [x] Faculty creation form: username, password, full profile (name, DOB, phone, email, gender, qualification, address) in one dialog
- [x] Password hashing with BCrypt on faculty creation
- [x] Username uniqueness validation (checked before transaction opens)
- [x] Reset faculty password (`FacultyManagement` → `AuthService.resetPassword()`)
- [x] Reset faculty username (`FacultyManagement` → `AuthService.resetUsername()` with uniqueness check, only affects FACULTY accounts)
- [x] Delete faculty (only `FACULTY` role rows deleted)
- [x] Faculty login unchanged and working
- [x] Faculty assignments preserved (ON DELETE CASCADE)

### Student Management
- [x] `parent_email VARCHAR(150)` column in `students` table (confirmed in DB)
- [x] `Student.java` model includes `parentEmail` field with getter/setter
- [x] `StudentRepository.save()` writes `parent_email` (10th parameter)
- [x] `StudentRepository.update()` writes `parent_email`
- [x] `StudentRepository.findByRollNo()`, `findAll()`, `findByClassAndSection()`, `findByAssignedClasses()` all read `parent_email` via `mapRow()`
- [x] Add student form includes Parent Email field (`StudentSearch.showAddStudentDialog()`)
- [x] Edit student dialog includes Parent Email field (`StudentProfile.showEditDialog()`)
- [x] Student profile displays Parent Email (`StudentProfile.createStudentInfo()`)
- [x] Student table shows Parent Email column (`StudentSearch.createTableColumns()`)

### Faculty / Class / Student Selection (Management)
- [x] Management can select a faculty member (ComboBox in `FacultyManagement.showClassStudentsDialog()`)
- [x] Management can view faculty's assigned classes (from `faculty_assignments` table via `FacultyAssignmentService.getAssignments()`)
- [x] Management can select a class/section from faculty's assignments
- [x] Management sees all students in selected class/section (via `StudentService.getStudentsByClassAndSection()`)
- [x] Shows roll number, name, class, section, parent name, parent phone, parent email
- [x] Handles: no faculty selected, no assignments, no students, database errors

### Barcode / QR System
- [x] `BarcodeService`: `generateCode128()` (ZXing Code128Writer), `generateQrCodeFromString()` (ZXing QRCodeWriter)
- [x] `BarcodeService.parseRollNumber()` — validates scanned input
- [x] `BulkBarcodeScreen`: faculty → class → student selection workflow
- [x] "Generate All Barcodes" — Code128 + QR per student in a FlowPane (handles large classes, no overflow)
- [x] Each barcode card shows name, roll, class/section, Code128 image, QR image
- [x] `BarcodePdfService` — real PDF via OpenPDF with Code128 + QR images, 2-up layout, student info
- [x] PDF export with `FileChooser`, meaningful filename (`barcodes_class{N}_{S}.pdf`)
- [x] `QrCodeScreen`: per-student QR generation, Save as PNG, server controls with instructions

### Attendance Scanning
- [x] `AttendanceScreen` (Management): `rollNumberField.setOnAction()` handles Enter from USB barcode scanner
- [x] Student found → displayed → mark PRESENT/ABSENT/LATE buttons
- [x] `FacultyAttendanceScreen`: table-based marking per student for assigned classes
- [x] All attendance goes through `AttendanceService` (no bypass)
- [x] Duplicate attendance: `markAttendance()` throws `IllegalStateException` → shown to user → no email sent
- [x] `markOrUpdateAttendance()` used by faculty (allows same-day correction — intentional)

### Phone QR Scanning
- [x] `AttendanceHttpServer`: built-in HTTP server using `com.sun.net.httpserver.HttpServer` on port 8765
- [x] `GET /mark?roll=N[&status=X]` — marks attendance, returns JSON
- [x] `GET /ping` — `{"status":"ok"}` health check
- [x] CORS headers for mobile browser access
- [x] QR encodes full URL when server running → phone scans → browser marks attendance
- [x] QR encodes plain roll number when server not running (USB scanner fallback)
- [x] Start/Stop controls in `BulkBarcodeScreen` and `QrCodeScreen`
- [x] Phone workflow documented on QrCodeScreen

### Parent Email Notifications
- [x] `EmailService` — SMTP via Jakarta Mail, configured from `.env`
- [x] `isConfigured()` — returns false if `SMTP_USERNAME` is blank (email silently skipped)
- [x] `sendAttendanceNotification()` — formats student info, sends plain-text email
- [x] Called from: `AttendanceScreen`, `FacultyAttendanceScreen`, `AttendanceHttpServer`
- [x] Email sent ONLY after attendance successfully saved
- [x] Duplicate attendance → no email sent
- [x] Missing parent_email → attendance saved, UI note shown ("No parent email configured.")
- [x] SMTP not configured → attendance saved, UI note shown ("Email not configured.")
- [x] SMTP failure → attendance saved, exception caught and logged, UI note shown ("Email delivery failed")
- [x] Credentials via `.env`: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_FROM`

### Database / Schema
- [x] `schema.sql`: `IF NOT EXISTS` for all tables; `parent_email` in students; `faculty` table defined
- [x] Migration comments provided in `schema.sql` for existing installs (`ALTER TABLE` and `CREATE TABLE IF NOT EXISTS`)
- [x] Actual MariaDB database confirmed to have all tables and columns

### UI/UX
- [x] All new screens use existing CSS classes (`card`, `primary-button`, `secondary-button`, `danger-button`, `page-title`, etc.)
- [x] Clear validation errors in dialogs (`errorLabel` in all forms)
- [x] Empty state messages (table placeholders, status labels)
- [x] Barcode overflow handled with `FlowPane` + `ScrollPane`
- [x] No placeholder/fake buttons — all buttons are functional

### Build
- [x] `mvn clean compile` → BUILD SUCCESS (37 source files, 0 errors, 0 warnings except one harmless unchecked in FacultyAttendanceScreen)

---

## KNOWN LIMITATIONS (documented in HANDOFF.md)

1. **Email**: Requires valid SMTP credentials in `.env`. Gmail App Password recommended.
   Email is synchronous on JavaFX thread — for large simultaneous mark events, consider async.
2. **Phone QR scanning**: Phone and computer must be on same Wi-Fi. Firewall may need port 8765 allowed.
3. **Faculty existing in DB without profiles**: 3 faculty users (`ad`, `add`, `f1`) were created before the faculty profile feature. They have no `faculty` table rows. The app shows `—` for their profile fields. New faculty created through the UI will have full profiles.
4. **FacultyAttendanceScreen unchecked warning**: Harmless — standard JavaFX `TableCell` pattern with generics.
5. **FacultyAttendanceScreen uses `markOrUpdateAttendance`**: Faculty can re-mark same day (correction). Management AttendanceScreen uses strict `markAttendance` (no duplicate). Both are intentional.

## NO REMAINING TASKS
All requirements from the specification have been implemented and verified.
