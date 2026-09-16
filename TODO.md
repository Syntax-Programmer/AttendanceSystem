# School Attendance System — Remaining TODO

## Faculty
- [x] Faculty Students page
- [x] Faculty Attendance page
- [x] Faculty Reports page
- [x] Faculty attendance restricted to assigned classes
- [x] Faculty report data restricted to assigned classes
- [x] Faculty CSV attendance export
- [x] Faculty navigation fully wired

## Attendance
- [x] Attendance marking works reliably from Faculty side
- [x] Present / Absent / Late support
- [x] Duplicate attendance handled correctly
- [x] Existing attendance can be viewed/updated where appropriate
- [x] Attendance date handled correctly

## Reports
- [x] Management CSV attendance export
- [x] Faculty CSV attendance export
- [x] CSV contains useful attendance fields
- [x] CSV respects active filters
- [x] CSV file chooser/save flow works
- [x] CSV output tested

## QR / Barcode
- [x] Determine the simplest reliable QR/barcode architecture based on the existing project
- [x] Student QR/barcode generation
- [x] QR/barcode contains roll number
- [x] Scan resolves roll number to student
- [x] Scanned student can be marked present
- [x] Duplicate attendance handled
- [x] Invalid/nonexistent roll number handled
- [x] Phone scanning workflow works
- [x] Faculty can use scanning for attendance

## Final
- [x] Compile project successfully
- [x] Test Management login
- [x] Test Faculty login
- [x] Test faculty assignments
- [x] Test Faculty Students
- [x] Test Faculty Attendance
- [x] Test Faculty Reports
- [x] Test Management Reports
- [x] Test CSV export
- [x] Test QR/barcode attendance
- [x] Test logout
- [x] Fix remaining runtime/UI errors
- [x] Update HANDOFF.md with final state

---

## Notes

### QR / Phone Scanning Architecture
- The QR scanning uses a lightweight built-in HTTP server (`AttendanceHttpServer`) on port 8765.
- When server is running, QR codes encode the full URL: `http://<server-ip>:8765/mark?roll=<rollNo>`
- Phone scans QR → browser opens URL → attendance marked via GET request → JSON response
- When server is not running, QR encodes plain roll number (for USB barcode scanner wedge).
- Endpoint: `GET /mark?roll=<rollNo>[&status=PRESENT|ABSENT|LATE]`
- Endpoint: `GET /ping` — health check

### Build Status
- `mvn clean compile` → BUILD SUCCESS (no errors, minor unchecked warnings in JavaFX generics)
- All 31 source files compile successfully.
