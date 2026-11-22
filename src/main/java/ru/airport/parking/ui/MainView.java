package ru.airport.parking.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import ru.airport.parking.service.ParkingService;


public class MainView extends BorderPane {

    private final ParkingService parkingService;

    public MainView(ParkingService parkingService) {
        this.parkingService = parkingService;

        setStyle("-fx-background-color: #F1F5F9;");

        // 1. Верх: заголовок, часы, счетчики и кнопки симуляции
        HeaderAndCountersView headerView = new HeaderAndCountersView(parkingService);
        setTop(headerView);

        // 2. Центр: Игровое поле слева, Панели инспектора и журнала справа
        HBox contentBox = new HBox(12);
        contentBox.setPadding(new Insets(12, 16, 12, 16));
        contentBox.setAlignment(Pos.TOP_LEFT);

        // Левая часть: симуляция (12 машин сверху, 20 мест снизу)
        ParkingSimulationPane simulationPane = new ParkingSimulationPane(parkingService);

        // Правая часть: инспектор, последний выезд, журнал
        VBox sidePanel = new VBox(10);
        sidePanel.setPrefWidth(420);
        sidePanel.setMinWidth(400);

        SelectedSpotInfoView spotInfoView = new SelectedSpotInfoView(parkingService);
        LastExitBannerView exitBannerView = new LastExitBannerView(parkingService);
        LogView logView = new LogView(parkingService.getLogService());
        VBox.setVgrow(logView, Priority.ALWAYS);

        sidePanel.getChildren().addAll(spotInfoView, exitBannerView, logView);

        contentBox.getChildren().addAll(simulationPane, sidePanel);
        HBox.setHgrow(simulationPane, Priority.NEVER);
        HBox.setHgrow(sidePanel, Priority.ALWAYS);

        setCenter(contentBox);

        // 3. Подвал: подсказки по управлению
        HBox footer = new HBox();
        footer.setPadding(new Insets(6, 16, 6, 16));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-width: 1 0 0 0;");

        Label footerHint = new Label("Управление: зажмите ЛКМ на автомобиле сверху для въезда на свободное место. Для выезда перетащите автомобиль за границу парковки. Кликните по месту для просмотра расчёта.");
        footerHint.setFont(Font.font("Segoe UI", 11.5));
        footerHint.setTextFill(Color.web("#475569"));

        footer.getChildren().add(footerHint);
        setBottom(footer);
    }
}