package com.school.attendance.ui;

import com.google.zxing.WriterException;
import com.school.attendance.model.FacultyAssignment;
import com.school.attendance.model.Student;
import com.school.attendance.model.User;
import com.school.attendance.repository.FacultyAssignmentRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.UserRepository;
import com.school.attendance.service.AttendanceHttpServer;
import com.school.attendance.service.BarcodePdfService;
import com.school.attendance.service.BarcodeService;
import com.school.attendance.service.FacultyAssignmentService;
import com.school.attendance.service.StudentService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

/**
 * Bulk Barcode Generation screen for Management.
 *
 * Workflow:
 *  1. Select faculty from dropdown.
 *  2. Select from faculty's assigned class/section.
 *  3. View all students in that class with their barcodes (Code128 + QR).
 *  4. Click "Generate All Barcodes" to generate for all students.
 *  5. Click "Export PDF" to save a printable barcode card PDF.
 *
 * Also supports single-student QR generation with server URL encoding.
 */
public class BulkBarcodeScreen extends VBox {

    private final StudentService studentService;
    private final FacultyAssignmentService assignmentService;
    private final BarcodeService barcodeService;

    private final ComboBox<User> facultySelector = new ComboBox<>();
    private final ComboBox<FacultyAssignment> classSelector = new ComboBox<>();
    private final Label statusLabel = new Label();

    // Barcode grid area
    private final FlowPane barcodePane = new FlowPane();
    private final ScrollPane barcodeScroll = new ScrollPane(barcodePane);

    // Current students for the selected class
    private List<Student> currentStudents;

    // HTTP server (shared state for QR URL encoding)
    private AttendanceHttpServer httpServer;
    private String serverIp;

    public BulkBarcodeScreen() {
        UserRepository userRepository = new UserRepository();
        FacultyAssignmentRepository assignmentRepository = new FacultyAssignmentRepository();
        StudentRepository studentRepository = new StudentRepository();

        studentService = new StudentService(studentRepository);
        assignmentService = new FacultyAssignmentService(assignmentRepository, userRepository);
        barcodeService = new BarcodeService();

        getStyleClass().add("content-area");
        setSpacing(20);
        buildUI();
        loadFaculty();
    }

    private void buildUI() {
        Label title = new Label("Bulk Barcode Generation");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Generate barcodes for an entire class — Code 128 + QR");
        subtitle.getStyleClass().add("page-subtitle");
        VBox header = new VBox(5, title, subtitle);

        VBox controlCard = buildControlCard();

        statusLabel.setStyle("-fx-text-fill: #687786; -fx-font-size: 13px;");

        // Barcode display area
        barcodePane.setHgap(15);
        barcodePane.setVgap(15);
        barcodePane.setPadding(new Insets(10));
        barcodePane.setStyle("-fx-background-color: white;");

        barcodeScroll.setFitToWidth(true);
        barcodeScroll.setPrefHeight(420);
        barcodeScroll.setStyle("-fx-background-color: white; -fx-border-color: #e8edf2;");
        VBox.setVgrow(barcodeScroll, Priority.ALWAYS);

        getChildren().addAll(header, controlCard, statusLabel, barcodeScroll);
    }

    private VBox buildControlCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label label = new Label("SELECT FACULTY AND CLASS");
        label.getStyleClass().add("stat-title");

        // Faculty selector
        facultySelector.setPromptText("Select Faculty...");
        facultySelector.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(User u) { return u == null ? "" : u.getUsername(); }
            @Override
            public User fromString(String s) { return null; }
        });
        facultySelector.setPrefWidth(200);
        facultySelector.setOnAction(e -> loadClassesForFaculty());

        // Class selector
        classSelector.setPromptText("Select Class...");
        classSelector.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(FacultyAssignment a) {
                return a == null ? "" : "Class " + a.getClassNumber() + " - " + a.getSection();
            }
            @Override
            public FacultyAssignment fromString(String s) { return null; }
        });
        classSelector.setPrefWidth(180);

        Button loadButton = new Button("Load Students");
        loadButton.getStyleClass().add("primary-button");
        loadButton.setOnAction(e -> loadStudents());

        Button generateAllButton = new Button("⚡ Generate All Barcodes");
        generateAllButton.getStyleClass().add("primary-button");
        generateAllButton.setOnAction(e -> generateAllBarcodes());

        Button exportPdfButton = new Button("📄 Export PDF");
        exportPdfButton.getStyleClass().add("secondary-button");
        exportPdfButton.setOnAction(e -> exportPdf());

        // Server controls for phone scanning
        Button startServerBtn = new Button("Start Scan Server");
        startServerBtn.getStyleClass().add("secondary-button");

        Button stopServerBtn = new Button("Stop Server");
        stopServerBtn.getStyleClass().add("danger-button");
        stopServerBtn.setDisable(true);
        stopServerBtn.setOnAction(e -> stopServer(startServerBtn, stopServerBtn));

        // Wire the start button with both button references
        startServerBtn.setOnAction(e -> startServer(startServerBtn, stopServerBtn));

        HBox row1 = new HBox(12,
            new VBox(3, new Label("Faculty"), facultySelector),
            new VBox(3, new Label("Class / Section"), classSelector),
            loadButton
        );
        row1.setAlignment(Pos.BOTTOM_LEFT);

        HBox row2 = new HBox(12, generateAllButton, exportPdfButton);
        HBox row3 = new HBox(12, new Label("Phone Scanning:"), startServerBtn, stopServerBtn);
        row3.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(label, row1, new Separator(), row2, row3);
        return card;
    }

    // ── Data Loading ──────────────────────────────────────────

    private void loadFaculty() {
        try {
            List<User> faculty = new UserRepository().findAllFaculty();
            facultySelector.setItems(FXCollections.observableArrayList(faculty));
        } catch (Exception e) {
            setStatus("Error loading faculty: " + e.getMessage());
        }
    }

    private void loadClassesForFaculty() {
        classSelector.getItems().clear();
        User selected = facultySelector.getValue();
        if (selected == null) return;
        try {
            List<FacultyAssignment> assignments = assignmentService.getAssignments(selected.getUserId());
            classSelector.setItems(FXCollections.observableArrayList(assignments));
            if (!assignments.isEmpty()) classSelector.getSelectionModel().selectFirst();
            if (assignments.isEmpty()) setStatus("This faculty has no class assignments.");
            else setStatus("Loaded " + assignments.size() + " class assignment(s).");
        } catch (Exception e) {
            setStatus("Error loading assignments: " + e.getMessage());
        }
    }

    private void loadStudents() {
        FacultyAssignment sel = classSelector.getValue();
        if (sel == null) {
            setStatus("Please select a class first.");
            return;
        }
        try {
            currentStudents = studentService.getStudentsByClassAndSection(
                sel.getClassNumber(), sel.getSection()
            );
            barcodePane.getChildren().clear();
            if (currentStudents.isEmpty()) {
                setStatus("No students found in Class " + sel.getClassNumber() + "-" + sel.getSection());
            } else {
                setStatus("Loaded " + currentStudents.size() + " student(s). Click 'Generate All Barcodes' to render.");
            }
        } catch (Exception e) {
            setStatus("Error loading students: " + e.getMessage());
        }
    }

    // ── Barcode Generation ────────────────────────────────────

    private void generateAllBarcodes() {
        if (currentStudents == null || currentStudents.isEmpty()) {
            setStatus("No students loaded. Select a class and click 'Load Students' first.");
            return;
        }
        barcodePane.getChildren().clear();
        int successCount = 0;
        int errorCount = 0;
        for (Student student : currentStudents) {
            try {
                VBox card = buildStudentBarcodeCard(student);
                barcodePane.getChildren().add(card);
                successCount++;
            } catch (Exception e) {
                errorCount++;
                Label errLabel = new Label("Error for " + student.getName() + ": " + e.getMessage());
                errLabel.setStyle("-fx-text-fill: #b91c1c;");
                barcodePane.getChildren().add(errLabel);
            }
        }
        setStatus("Generated " + successCount + " barcode(s)"
            + (errorCount > 0 ? ", " + errorCount + " error(s)." : "."));
    }

    private VBox buildStudentBarcodeCard(Student student) throws WriterException {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle(
            "-fx-background-color: white; -fx-border-color: #d1d9e0; " +
            "-fx-border-radius: 8; -fx-background-radius: 8;"
        );
        card.setPrefWidth(240);

        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        nameLabel.setWrapText(true);

        Label rollLabel = new Label("Roll: " + student.getRollNo()
            + "  |  Class " + student.getClassNumber() + "-" + student.getSection());
        rollLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #687786;");

        // Code 128 barcode image
        BufferedImage barcodeImg = barcodeService.generateCode128(student.getRollNo(), 220, 55);
        WritableImage fxBarcode = SwingFXUtils.toFXImage(barcodeImg, null);
        ImageView barcodeView = new ImageView(fxBarcode);
        barcodeView.setFitWidth(220);
        barcodeView.setFitHeight(55);
        barcodeView.setPreserveRatio(true);

        // QR code image
        String qrContent = (httpServer != null && httpServer.isRunning() && serverIp != null)
            ? "http://" + serverIp + ":" + AttendanceHttpServer.PORT + "/mark?roll=" + student.getRollNo()
            : String.valueOf(student.getRollNo());
        BufferedImage qrImg = barcodeService.generateQrCodeFromString(qrContent, 100);
        WritableImage fxQr = SwingFXUtils.toFXImage(qrImg, null);
        ImageView qrView = new ImageView(fxQr);
        qrView.setFitWidth(90);
        qrView.setFitHeight(90);

        card.getChildren().addAll(nameLabel, rollLabel, barcodeView, qrView);
        return card;
    }

    // ── PDF Export ────────────────────────────────────────────

    private void exportPdf() {
        if (currentStudents == null || currentStudents.isEmpty()) {
            setStatus("No students loaded. Load a class first.");
            return;
        }

        FacultyAssignment sel = classSelector.getValue();
        String filename = sel != null
            ? "barcodes_class" + sel.getClassNumber() + "_" + sel.getSection() + ".pdf"
            : "barcodes.pdf";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Barcode PDF");
        fileChooser.setInitialFileName(filename);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        String baseUrl = (httpServer != null && httpServer.isRunning() && serverIp != null)
            ? "http://" + serverIp + ":" + AttendanceHttpServer.PORT
            : null;

        try {
            BarcodePdfService pdfService = new BarcodePdfService();
            pdfService.generatePdf(currentStudents, file, baseUrl);
            setStatus("PDF saved to: " + file.getAbsolutePath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Exported");
            alert.setHeaderText(null);
            alert.setContentText("Barcode PDF saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("PDF export failed: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export PDF:\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    // ── Server Controls ───────────────────────────────────────

    private void startServer(Button startBtn, Button stopBtn) {
        try {
            httpServer = new AttendanceHttpServer();
            httpServer.startServer();
            serverIp = InetAddress.getLocalHost().getHostAddress();
            setStatus("Scan server running on http://" + serverIp + ":" + AttendanceHttpServer.PORT
                + " — Re-generate barcodes to get phone-scannable QRs.");
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
        } catch (Exception e) {
            setStatus("Failed to start server: " + e.getMessage());
        }
    }

    private void stopServer(Button startBtn, Button stopBtn) {
        if (httpServer != null) httpServer.stopServer();
        httpServer = null;
        serverIp = null;
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
        setStatus("Scan server stopped.");
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }
}
