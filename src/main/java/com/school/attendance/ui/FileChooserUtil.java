package com.school.attendance.ui;

import java.io.File;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Pure JavaFX export dialog utility.
 *
 * Runs 100% on the JavaFX Application Thread with zero Swing/AWT components.
 * This completely prevents the AWT-GTK thread deadlocks that cause application hangs on Linux.
 */
public final class FileChooserUtil {

    private FileChooserUtil() {}

    /**
     * Shows the save dialog without an explicit owner window.
     */
    public static File showSaveDialog(
        String title,
        String defaultName,
        String description,
        String extension
    ) {
        return showSaveDialog(null, title, defaultName, description, extension);
    }

    /**
     * Shows a modal in-app JavaFX dialog for choosing the export destination.
     *
     * @param owner       parent window (can be null)
     * @param title       dialog title
     * @param defaultName suggested filename (e.g. "report.csv")
     * @param description file format description (e.g. "CSV Files")
     * @param extension   file extension without leading dot (e.g. "csv")
     * @return chosen File, or null if cancelled
     */
    public static File showSaveDialog(
        Window owner,
        String title,
        String defaultName,
        String description,
        String extension
    ) {
        final String safeExt = (extension != null) ? extension.replace(".", "").trim() : "";
        final String safeName = (defaultName != null && !defaultName.isBlank())
            ? defaultName.trim()
            : "export." + safeExt;

        File defaultDir = getDefaultExportDirectory();

        Dialog<File> dialog = new Dialog<>();
        dialog.setTitle(title != null ? title : "Export File");

        if (owner != null) {
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
        } else {
            dialog.initModality(Modality.APPLICATION_MODAL);
        }

        try {
            String css = FileChooserUtil.class.getResource("/css/app.css").toExternalForm();
            dialog.getDialogPane().getStylesheets().add(css);
        } catch (Exception ignored) {}

        // Header
        Label headerTitle = new Label("Export " + (description != null ? description : "File"));
        headerTitle.getStyleClass().add("page-title");
        headerTitle.setStyle("-fx-font-size: 18px;");

        Label headerSub = new Label("Choose the file name and destination folder below.");
        headerSub.getStyleClass().add("page-subtitle");

        VBox headerBox = new VBox(4, headerTitle, headerSub);

        // File name input
        Label nameLabel = new Label("FILE NAME");
        nameLabel.getStyleClass().add("stat-title");

        TextField nameInput = new TextField(safeName);
        nameInput.setPrefHeight(40);
        nameInput.setStyle("-fx-font-size: 13px;");

        VBox nameBox = new VBox(5, nameLabel, nameInput);

        // Destination folder input
        Label folderLabel = new Label("DESTINATION FOLDER");
        folderLabel.getStyleClass().add("stat-title");

        TextField folderInput = new TextField(defaultDir.getAbsolutePath());
        folderInput.setPrefHeight(40);
        folderInput.setStyle("-fx-font-size: 13px;");
        HBox.setHgrow(folderInput, Priority.ALWAYS);

        Button browseBtn = new Button("Browse...");
        browseBtn.getStyleClass().add("secondary-button");
        browseBtn.setPrefHeight(40);
        browseBtn.setOnAction(e -> {
            try {
                DirectoryChooser dc = new DirectoryChooser();
                dc.setTitle("Select Destination Folder");
                File current = new File(folderInput.getText().trim());
                if (current.isDirectory()) {
                    dc.setInitialDirectory(current);
                }
                Window win = dialog.getDialogPane().getScene() != null
                    ? dialog.getDialogPane().getScene().getWindow()
                    : owner;
                File chosen = dc.showDialog(win);
                if (chosen != null) {
                    folderInput.setText(chosen.getAbsolutePath());
                }
            } catch (Throwable ignored) {}
        });

        HBox folderRow = new HBox(8, folderInput, browseBtn);
        folderRow.setAlignment(Pos.CENTER_LEFT);

        // Quick folder shortcuts
        HBox shortcuts = new HBox(8);
        shortcuts.setAlignment(Pos.CENTER_LEFT);
        Label quickLabel = new Label("Quick select:");
        quickLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7b8794; -fx-font-weight: bold;");
        shortcuts.getChildren().add(quickLabel);

        addQuickFolderButton(shortcuts, "Downloads", getDownloadsDir(), folderInput);
        addQuickFolderButton(shortcuts, "Documents", getDocumentsDir(), folderInput);
        addQuickFolderButton(shortcuts, "Desktop", getDesktopDir(), folderInput);
        addQuickFolderButton(shortcuts, "Home", getUserHomeDir(), folderInput);

        VBox folderBox = new VBox(5, folderLabel, folderRow, shortcuts);

        // Error message label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px; -fx-font-weight: bold;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Main content layout
        VBox content = new VBox(14, headerBox, nameBox, folderBox, errorLabel);
        content.setPrefWidth(480);
        content.setPadding(new Insets(10, 10, 10, 10));
        dialog.getDialogPane().setContent(content);

        // Buttons
        ButtonType exportButtonType = new ButtonType("Export", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(exportButtonType, cancelButtonType);

        Button exportBtn = (Button) dialog.getDialogPane().lookupButton(exportButtonType);
        exportBtn.getStyleClass().add("primary-button");

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelBtn.getStyleClass().add("secondary-button");

        // Validate on Export click before closing
        exportBtn.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameInput.getText().trim();
            if (name.isEmpty()) {
                errorLabel.setText("Please enter a file name.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
                return;
            }

            String folderPath = folderInput.getText().trim();
            if (folderPath.isEmpty()) {
                errorLabel.setText("Please specify a destination folder.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
                return;
            }

            File folder = new File(folderPath);
            if (!folder.exists() && !folder.mkdirs()) {
                errorLabel.setText("Unable to create directory: " + folderPath);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
                return;
            }

            if (!folder.canWrite()) {
                errorLabel.setText("Directory is not writable: " + folderPath);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(btnType -> {
            if (btnType == exportButtonType) {
                String name = nameInput.getText().trim();
                if (name.isEmpty()) name = safeName;
                if (!safeExt.isEmpty() && !name.toLowerCase().endsWith("." + safeExt.toLowerCase())) {
                    name = name + "." + safeExt;
                }
                File folder = new File(folderInput.getText().trim());
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                return new File(folder, name).getAbsoluteFile();
            }
            return null;
        });

        Optional<File> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private static void addQuickFolderButton(HBox container, String label, File folder, TextField folderInput) {
        if (folder == null || !folder.exists()) return;
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: #e8edf2; -fx-text-fill: #34495e; -fx-font-size: 11px; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-cursor: hand;");
        btn.setOnAction(e -> folderInput.setText(folder.getAbsolutePath()));
        container.getChildren().add(btn);
    }

    private static File getDefaultExportDirectory() {
        File downloads = getDownloadsDir();
        if (downloads.isDirectory()) return downloads;
        File documents = getDocumentsDir();
        if (documents.isDirectory()) return documents;
        return getUserHomeDir();
    }

    private static File getDownloadsDir() {
        return new File(System.getProperty("user.home", "."), "Downloads");
    }

    private static File getDocumentsDir() {
        return new File(System.getProperty("user.home", "."), "Documents");
    }

    private static File getDesktopDir() {
        return new File(System.getProperty("user.home", "."), "Desktop");
    }

    private static File getUserHomeDir() {
        return new File(System.getProperty("user.home", "."));
    }
}
