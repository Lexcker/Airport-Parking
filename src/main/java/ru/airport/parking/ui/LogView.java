package ru.airport.parking.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ru.airport.parking.service.LogService;

import java.awt.Desktop;
import java.io.File;


public class LogView extends VBox {

    private final LogService logService;
    private final ObservableList<String> logItems = FXCollections.observableArrayList();
    private final ListView<String> listView;

    public LogView(LogService logService) {
        this.logService = logService;

        setSpacing(8);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 4px; -fx-border-color: #CBD5E1; -fx-border-radius: 4px;");

        // Заголовок и кнопка
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Журнал событий");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#0F172A"));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnOpenFile = new Button("Открыть файл");
        btnOpenFile.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #1E293B; -fx-font-size: 11.5px; -fx-cursor: hand; -fx-padding: 3 10 3 10; -fx-border-color: #CBD5E1; -fx-border-radius: 3px; -fx-background-radius: 3px;");
        btnOpenFile.setOnAction(e -> openLogFile());

        header.getChildren().addAll(title, spacer, btnOpenFile);

        // Список записей
        listView = new ListView<>(logItems);
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setStyle("-fx-background-color: #F8FAFC; -fx-control-inner-background: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 3px; -fx-background-radius: 3px;");

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setFont(Font.font("Consolas", 11.5));
                    if (item.contains("Итог дня") || item.contains("Начало записей нового дня")) {
                        setTextFill(Color.web("#0F172A"));
                        setFont(Font.font("Consolas", FontWeight.BOLD, 11.5));
                    } else {
                        setTextFill(Color.web("#334155"));
                    }
                    setStyle("-fx-background-color: transparent; -fx-padding: 3 6 3 6; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        logItems.addAll(logService.getMessageHistory());

        logService.addListener(line -> {
            logItems.add(line);
            listView.scrollTo(logItems.size() - 1);
        });

        getChildren().addAll(header, listView);
    }

    private void openLogFile() {
        try {
            File f = logService.getLogFile();
            if (f.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            }
        } catch (Exception e) {
            System.err.println("Не удалось открыть файл: " + e.getMessage());
        }
    }
}