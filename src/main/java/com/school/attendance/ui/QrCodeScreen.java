package com.school.attendance.ui;

import com.google.zxing.WriterException;
import com.school.attendance.model.Student;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.service.AttendanceHttpServer;
import com.school.attendance.service.BarcodeService;
import com.school.attendance.service.StudentService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 * QR Code management screen.
 *
 * Features:
 * - Search student by roll number
 * - Display QR code for the student
 *   - When HTTP server is running: QR encodes the full URL (http://ip:port/mark?roll=N)
 *     so phone can scan it and directly mark attendance via browser.
 *   - When server is not running: QR encodes just the roll number (for USB scanner workflow).
 * - Save QR code as PNG
 * - Start/Stop built-in HTTP server for phone-based QR scanning
 * - Keyboard/USB barcode scanner input (scanners send roll number + Enter)
 */
public class QrCodeScreen extends VBox {

    private final StudentService studentService;
    private final BarcodeService barcodeService;
    private AttendanceHttpServer httpServer;

    private final TextField rollInput = new TextField();
    private final ImageView qrImageView = new ImageView();
    private final Label studentInfoLabel = new Label();
    private final Label serverStatusLabel = new Label("Server: Not running");
    private final Label serverUrlLabel = new Label();
    private Button startServerButton;
    private Button stopServerButton;
    private Button saveButton;

    // Last found student for QR display
    private Student currentStudent;
    // Server IP for URL-encoded QR
    private String serverIp = null;

    public QrCodeScreen() {
        this.studentService = new StudentService(new StudentRepository());
        this.barcodeService = new BarcodeService();

        getStyleClass().add("content-area");
        setSpacing(20);

        Label title = new Label("QR Code Management");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Generate student QR codes and manage phone scanning");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(5, title, subtitle);

        HBox mainContent = new HBox(25);
        mainContent.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        VBox leftPanel = buildSearchPanel();
        VBox rightPanel = buildServerPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        mainContent.getChildren().addAll(leftPanel, rightPanel);

        getChildren().addAll(header, mainContent);
    }

    private VBox buildSearchPanel() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(550);

        Label cardTitle = new Label("GENERATE QR CODE");
        cardTitle.getStyleClass().add("stat-title");

        Label rollLabel = new Label("Roll Number");
        rollInput.setPromptText("Enter roll number...");
        rollInput.setPrefHeight(42);

        Button searchButton = new Button("Generate QR");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(42);
        searchButton.setOnAction(e -> generateQr());
        rollInput.setOnAction(e -> generateQr());

        HBox searchRow = new HBox(10, rollInput, searchButton);
        HBox.setHgrow(rollInput, Priority.ALWAYS);

        studentInfoLabel.getStyleClass().add("page-subtitle");
        studentInfoLabel.setWrapText(true);

        qrImageView.setFitWidth(250);
        qrImageView.setFitHeight(250);
        qrImageView.setPreserveRatio(true);
        qrImageView.setVisible(false);

        saveButton = new Button("Save QR as PNG");
        saveButton.getStyleClass().add("secondary-button");
        saveButton.setOnAction(e -> saveQrPng());
        saveButton.setVisible(false);

        VBox qrBox = new VBox(10, qrImageView, saveButton);
        qrBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(
            cardTitle,
            new VBox(5, rollLabel, searchRow),
            studentInfoLabel,
            qrBox
        );

        return card;
    }

    private VBox buildServerPanel() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(350);

        Label cardTitle = new Label("PHONE SCANNING SERVER");
        cardTitle.getStyleClass().add("stat-title");

        Label desc = new Label(
            "Start the HTTP server so phones can scan QR codes to mark attendance.\n\n" +
            "Workflow:\n" +
            "1. Start server below\n" +
            "2. Generate QR for a student\n" +
            "3. Student scans QR code with phone camera\n" +
            "4. Phone browser opens attendance URL\n" +
            "5. Attendance is marked automatically\n\n" +
            "Phone and computer must be on the same network."
        );
        desc.setWrapText(true);
        desc.getStyleClass().add("page-subtitle");

        serverStatusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        serverUrlLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #08a88a;");
        serverUrlLabel.setWrapText(true);

        startServerButton = new Button("Start Server");
        startServerButton.getStyleClass().add("primary-button");
        startServerButton.setMaxWidth(Double.MAX_VALUE);
        startServerButton.setOnAction(e -> startHttpServer());

        stopServerButton = new Button("Stop Server");
        stopServerButton.getStyleClass().add("danger-button");
        stopServerButton.setMaxWidth(Double.MAX_VALUE);
        stopServerButton.setDisable(true);
        stopServerButton.setOnAction(e -> stopHttpServer());

        Label keyboardTitle = new Label("KEYBOARD/USB SCANNER");
        keyboardTitle.getStyleClass().add("stat-title");
        keyboardTitle.setPadding(new Insets(10, 0, 0, 0));

        Label keyboardDesc = new Label(
            "USB/Bluetooth barcode scanners work as keyboard wedges.\n" +
            "They send roll number + Enter automatically.\n" +
            "Use the Attendance screen and type/scan into the roll number field."
        );
        keyboardDesc.setWrapText(true);
        keyboardDesc.getStyleClass().add("page-subtitle");

        card.getChildren().addAll(
            cardTitle, desc,
            serverStatusLabel, serverUrlLabel,
            startServerButton, stopServerButton,
            new Separator(),
            keyboardTitle, keyboardDesc
        );

        return card;
    }

    private void generateQr() {
        String text = rollInput.getText().trim();
        if (text.isEmpty()) return;

        int rollNo;
        try {
            rollNo = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            studentInfoLabel.setText("Please enter a valid roll number.");
            return;
        }

        try {
            Optional<Student> result = studentService.findStudent(rollNo);
            if (result.isEmpty()) {
                studentInfoLabel.setText("Student with roll number " + rollNo + " not found.");
                qrImageView.setVisible(false);
                if (saveButton != null) saveButton.setVisible(false);
                return;
            }
            currentStudent = result.get();
            studentInfoLabel.setText(
                currentStudent.getName() +
                " | Class " + currentStudent.getClassNumber() +
                " - " + currentStudent.getSection()
            );

            // If server is running, encode the full URL so phone can scan → attend directly
            String qrContent;
            if (httpServer != null && httpServer.isRunning() && serverIp != null) {
                qrContent = "http://" + serverIp + ":" + AttendanceHttpServer.PORT + "/mark?roll=" + rollNo;
                studentInfoLabel.setText(
                    studentInfoLabel.getText() +
                    "\n✓ QR encodes scan URL (server running)"
                );
            } else {
                qrContent = String.valueOf(rollNo);
                studentInfoLabel.setText(
                    studentInfoLabel.getText() +
                    "\n(Start server for phone-scannable QR)"
                );
            }

            BufferedImage qrImage = barcodeService.generateQrCodeFromString(qrContent, 250);
            WritableImage fxImage = SwingFXUtils.toFXImage(qrImage, null);
            qrImageView.setImage(fxImage);
            qrImageView.setVisible(true);
            if (saveButton != null) saveButton.setVisible(true);

        } catch (WriterException e) {
            e.printStackTrace();
            studentInfoLabel.setText("Error generating QR code: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            studentInfoLabel.setText("Error: " + e.getMessage());
        }
    }

    private void saveQrPng() {
        if (currentStudent == null || qrImageView.getImage() == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Code");
        fileChooser.setInitialFileName("qr_roll_" + currentStudent.getRollNo() + ".png");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG Images", "*.png")
        );

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        try {
            String qrContent;
            if (httpServer != null && httpServer.isRunning() && serverIp != null) {
                qrContent = "http://" + serverIp + ":" + AttendanceHttpServer.PORT
                    + "/mark?roll=" + currentStudent.getRollNo();
            } else {
                qrContent = String.valueOf(currentStudent.getRollNo());
            }
            BufferedImage image = barcodeService.generateQrCodeFromString(qrContent, 300);
            ImageIO.write(image, "PNG", file);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("QR Code Saved");
            alert.setHeaderText(null);
            alert.setContentText("QR code saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save QR code: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void startHttpServer() {
        try {
            httpServer = new AttendanceHttpServer();
            httpServer.startServer();
            serverIp = InetAddress.getLocalHost().getHostAddress();
            serverStatusLabel.setText("Server: RUNNING on port " + AttendanceHttpServer.PORT);
            serverStatusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #08a88a;");

            serverUrlLabel.setText(
                "Mark URL: http://" + serverIp + ":" + AttendanceHttpServer.PORT + "/mark?roll=<rollNo>\n" +
                "Test: http://" + serverIp + ":" + AttendanceHttpServer.PORT + "/ping\n\n" +
                "Now re-generate QR codes to get phone-scannable QRs."
            );

            startServerButton.setDisable(true);
            stopServerButton.setDisable(false);

            // Refresh QR if a student is already displayed
            if (currentStudent != null) {
                generateQr();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Server Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to start server: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void stopHttpServer() {
        if (httpServer != null) {
            httpServer.stopServer();
            httpServer = null;
        }
        serverIp = null;
        serverStatusLabel.setText("Server: Not running");
        serverStatusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        serverUrlLabel.setText("");
        startServerButton.setDisable(false);
        stopServerButton.setDisable(true);
    }
}
