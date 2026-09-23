# School Attendance System — TODO

## ✅ COMPLETED FEATURES

### Faculty Management
- [x] Faculty profile table in database (faculty table linked to users via user_id)
- [x] Faculty model (Faculty.java) with full profile fields
- [x] FacultyRepository with CRUD operations
- [x] FacultyService with transactional faculty+user creation (rolls back both on failure)
- [x] Faculty creation form: username, password, full profile (name, DOB, phone, email, gender, qualification, address)
- [x] Password hashing with BCrypt on faculty creation
- [x] Username uniqueness validation
- [x] Reset faculty password (Management side)
- [x] Reset faculty username (Management side) with uniqueness check
- [x] Delete faculty (Management side)
- [x] Faculty login unchanged and working
- [x] Faculty assignments preserved

### Student Management
- [x] parent_email column added to students table
- [x] Student model includes parentEmail field
- [x] StudentRepository reads/writes parent_email
- [x] Add student form includes Parent Email field
- [x] Edit student dialog includes Parent Email field
- [x] Student profile displays Parent Email
- [x] Student table shows Parent Email column

### Faculty / Class / Student Selection (Management)
- [x] Management can select a faculty member
- [x] Management can view faculty's assigned classes (from faculty_assignments table)
- [x] Management can select a class/section from faculty's assignments
- [x] Management sees all students in selected class/section
- [x] Shows roll number, name, class, section, parent info, parent email
- [x] Handles: no faculty selected, no assignments, no students, errors

### Barcode / QR System
- [x] BarcodeService: Code 128 (USB scanner) + QR code generation (ZXing)
- [x] BulkBarcodeScreen: faculty → class → student selection workflow
- [x] Generate all barcodes for a class with one click
- [x] Each barcode card shows name, roll, Code128, QR code
- [x] BarcodePdfService: real PDF with Code128 + QR per student (OpenPDF)
- [x] PDF export with FileChooser, meaningful filenames
- [x] QrCodeScreen: per-student QR generation, Save as PNG

### Attendance Scanning
- [x] Management AttendanceScreen: USB barcode/keyboard wedge input (Enter triggers find)
- [x] FacultyAttendanceScreen: table-based marking per student
- [x] AttendanceService: validation, duplicate prevention
- [x] All attendance goes through AttendanceService
- [x] Duplicate attendance handled with clear message (not re-sent email)

### Phone QR Scanning
- [x] AttendanceHttpServer: built-in HTTP server on port 8765
- [x] GET /mark?roll=N[&status=X] — marks attendance via browser
- [x] GET /ping — connectivity check
- [x] QR encodes full URL when server running (phone scans → browser marks attendance)
- [x] QR encodes plain roll number when server not running (USB scanner fallback)
- [x] Server start/stop controls in BulkBarcodeScreen and QrCodeScreen
- [x] Phone instructions documented on QrCodeScreen

### Parent Email Notifications
- [x] EmailService with SMTP configuration via .env
- [x] AttendanceScreen (Management): sends email after marking attendance
- [x] FacultyAttendanceScreen: sends email after marking attendance
- [x] AttendanceHttpServer: sends email after phone-scan attendance
- [x] Email sent ONLY after attendance successfully saved
- [x] Duplicate attendance → no email sent
- [x] Missing parent_email → saves attendance, logs, shows UI note
- [x] SMTP not configured → saves attendance, logs, shows UI note
- [x] SMTP failure → attendance saved, failure logged, shows UI note
- [x] Credentials via .env (SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_FROM)

### Database / Schema
- [x] schema.sql updated: IF NOT EXISTS, faculty table, parent_email column
- [x] Migration comments provided in schema.sql for existing installs
- [x] Existing data preserved

### UI/UX
- [x] All new screens use existing CSS classes (card, primary-button, etc.)
- [x] Clear validation errors in dialogs
- [x] Empty state messages
- [x] Barcode overflow handled with FlowPane + ScrollPane
- [x] All buttons are functional (no placeholders)

### Build
- [x] mvn clean compile → BUILD SUCCESS (37 files, no errors)
- [x] Minor unchecked warning in FacultyAttendanceScreen (standard JavaFX pattern, harmless)

## KNOWN LIMITATIONS
- SMTP email: SMTP_USERNAME must be set in .env to enable sending
- Phone QR scanning: requires phone and computer on same Wi-Fi network
- No FXML-based UI (uses programmatic JavaFX throughout, by design)
- FacultyAttendanceScreen uses markOrUpdateAttendance (allows re-marking same date)
  while AttendanceScreen (Management) uses strict markAttendance (no re-marking)
  — this is intentional; faculty may need to correct a same-day entry

## NO REMAINING TASKS
All requirements from the specification are implemented.
