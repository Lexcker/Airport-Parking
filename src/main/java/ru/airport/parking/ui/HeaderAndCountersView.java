package ru.airport.parking.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ru.airport.parking.model.SimulationClock;
import ru.airport.parking.service.ParkingService;


public class HeaderAndCountersView extends VBox {

    private final ParkingService parkingService;
    private final SimulationClock clock;

    // Часы
    private final Label clockLabel;
    private final Label clockSubtitle;

    // Счётчики
    private final Label freeCountLabel;
    private final Label occupiedCountLabel;
    private final Label dailyRevenueLabel;

    // Кнопка паузы
    private final Button pauseBtn;

    private static final String BTN_DEFAULT_STYLE =
            "-fx-background-color: #FFFFFF; -fx-text-fill: #1E293B; -fx-font-size: 11.5px; " +
                    "-fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-border-color: #CBD5E1; " +
                    "-fx-border-radius: 3px; -fx-background-radius: 3px;";

    private static final String BTN_PAUSED_STYLE =
            "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-font-weight: bold; -fx-font-size: 11.5px; " +
                    "-fx-cursor: hand; -fx-padding: 4 10 4 10; -fx-border-color: #F59E0B; " +
                    "-fx-border-radius: 3px; -fx-background-radius: 3px;";

    public HeaderAndCountersView(ParkingService parkingService) {
        this.parkingService = parkingService;
        this.clock = parkingService.getClock();

        setSpacing(10);
        setPadding(new Insets(12, 16, 12, 16));
        setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-width: 0 0 1 0;");

        // 1. Верхний ряд: Название парковки слева, Часы справа
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label appTitle = new Label("СИСТЕМА УЧЁТА ХРАНЕНИЯ АВТОМОБИЛЕЙ АЭРОПОРТА");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        appTitle.setTextFill(Color.web("#0F172A"));

        Label appSub = new Label("Платная парковка терминала");
        appSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        appSub.setTextFill(Color.web("#64748B"));
        titleBox.getChildren().addAll(appTitle, appSub);

        HBox topSpacer = new HBox();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        // Блок часов
        VBox clockBox = new VBox(2);
        clockBox.setAlignment(Pos.CENTER_RIGHT);

        clockLabel = new Label(clock.getFormattedDateTime());
        clockLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        clockLabel.setTextFill(Color.web("#0F172A"));
        clockLabel.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 3 10 3 10; -fx-background-radius: 3px; -fx-border-color: #CBD5E1; -fx-border-radius: 3px; -fx-border-width: 1;");

        clockSubtitle = new Label("Масштаб: 1 сек. = 1 мин.");
        clockSubtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10.5));
        clockSubtitle.setTextFill(Color.web("#64748B"));

        clockBox.getChildren().addAll(clockLabel, clockSubtitle);
        topRow.getChildren().addAll(titleBox, topSpacer, clockBox);

        // 2. Нижний ряд: Счётчики слева, Управление временем справа
        HBox bottomRow = new HBox(16);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        // Единая строгая панель показателей
        HBox statsPanel = new HBox(14);
        statsPanel.setAlignment(Pos.CENTER_LEFT);
        statsPanel.setPadding(new Insets(5, 12, 5, 12));
        statsPanel.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 3px; -fx-background-radius: 3px;");

        freeCountLabel = new Label("20");
        occupiedCountLabel = new Label("0");
        dailyRevenueLabel = new Label("0 ₽");

        HBox freeItem = createStatItem("Свободно мест:", freeCountLabel, "#15803D");
        HBox occupiedItem = createStatItem("Занято мест:", occupiedCountLabel, "#B45309");
        HBox revenueItem = createStatItem("Выручка за сутки:", dailyRevenueLabel, "#0F172A");

        statsPanel.getChildren().addAll(
                freeItem,
                createVerticalDivider(),
                occupiedItem,
                createVerticalDivider(),
                revenueItem
        );

        HBox bottomSpacer = new HBox();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        // Управление временем (Пауза + Быстрая перемотка)
        HBox controlsRow = new HBox(8);
        controlsRow.setAlignment(Pos.CENTER_RIGHT);

        pauseBtn = new Button("Пауза");
        pauseBtn.setStyle(BTN_DEFAULT_STYLE);
        pauseBtn.setOnAction(e -> {
            clock.togglePause();
            if (clock.isPaused()) {
                pauseBtn.setText("Возобновить");
                pauseBtn.setStyle(BTN_PAUSED_STYLE);
            } else {
                pauseBtn.setText("Пауза");
                pauseBtn.setStyle(BTN_DEFAULT_STYLE);
            }
        });

        Label jumpTitle = new Label("Быстрая перемотка:");
        jumpTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        jumpTitle.setTextFill(Color.web("#475569"));

        Button btnAdd20m = createJumpButton("+20 мин", () -> clock.advanceMinutes(20), "Проверка бесплатного периода");
        Button btnAdd1h = createJumpButton("+1 час", () -> clock.advanceHours(1), "Проверка почасового тарифа");
        Button btnAdd12h = createJumpButton("+12 часов", () -> clock.advanceHours(12), "Проверка перехода на суточный тариф");
        Button btnAdd24h = createJumpButton("+24 часа", () -> clock.advanceDays(1), "Проверка суточного тарифа и смены дня");

        controlsRow.getChildren().addAll(
                pauseBtn,
                createVerticalDivider(),
                jumpTitle,
                btnAdd20m,
                btnAdd1h,
                btnAdd12h,
                btnAdd24h
        );

        bottomRow.getChildren().addAll(statsPanel, bottomSpacer, controlsRow);

        getChildren().addAll(topRow, bottomRow);

        clock.addTickListener(t -> clockLabel.setText(clock.getFormattedDateTime()));
        parkingService.addStateChangeListener(this::updateCounters);

        updateCounters();
    }

    private HBox createStatItem(String titleText, Label valueLabel, String valueColorHex) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        title.setTextFill(Color.web("#475569"));

        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13.5));
        valueLabel.setTextFill(Color.web(valueColorHex));

        box.getChildren().addAll(title, valueLabel);
        return box;
    }

    private Separator createVerticalDivider() {
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setPrefHeight(16);
        return sep;
    }

    private Button createJumpButton(String title, Runnable action, String tooltipText) {
        Button btn = new Button(title);
        btn.setStyle(BTN_DEFAULT_STYLE);
        btn.setTooltip(new Tooltip(tooltipText));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    public void updateCounters() {
        int free = parkingService.getParkingLot().getFreeCount();
        int occupied = parkingService.getParkingLot().getOccupiedCount();
        freeCountLabel.setText(String.valueOf(free));
        occupiedCountLabel.setText(String.valueOf(occupied));
        dailyRevenueLabel.setText(parkingService.getDailyRevenue() + " ₽");
    }
}