package ru.airport.parking.ui;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ru.airport.parking.model.Car;
import ru.airport.parking.model.ParkingLot;
import ru.airport.parking.model.ParkingSpot;
import ru.airport.parking.service.ParkingService;

import java.util.HashMap;
import java.util.Map;

/**
 * Интерактивная область симуляции в светлой теме:
 * 1. Верхняя зона — поле с 12 доступными автомобилями (вид сверху).
 * 2. Нижняя зона — охраняемая парковка на 20 пронумерованных мест с дорожной разметкой.
 */
public class ParkingSimulationPane extends Pane {

    public static final double PANE_WIDTH = 780.0;
    public static final double PANE_HEIGHT = 580.0;

    // Геометрия верхней зоны (Зона ожидания — 12 мест: 2 ряда по 6)
    public static final double WAITING_AREA_X = 15.0;
    public static final double WAITING_AREA_Y = 12.0;
    public static final double WAITING_AREA_W = 750.0;
    public static final double WAITING_AREA_H = 195.0;

    // Геометрия парковки (20 мест: 2 ряда по 10 мест с проездом посередине)
    public static final double LOT_X = 15.0;
    public static final double LOT_Y = 222.0;
    public static final double LOT_W = 750.0;
    public static final double LOT_H = 345.0;

    public static final double SPOT_WIDTH = 67.0;
    public static final double SPOT_HEIGHT = 105.0;

    private final ParkingService parkingService;
    private final ParkingLot parkingLot;

    // Графика парковочных мест
    private final Map<Integer, Rectangle> spotRectangles = new HashMap<>();
    private final Map<Integer, Text> spotStatusTexts = new HashMap<>();

    // Связка моделей Car и их визуальных узлов CarView
    private final Map<Car, CarView> carViews = new HashMap<>();

    // Состояние перетаскивания
    private CarView activeDraggingCarView = null;
    private double dragAnchorX = 0;
    private double dragAnchorY = 0;
    private double originalX = 0;
    private double originalY = 0;
    private boolean isDraggingParkedCar = false;
    private int originSpotNumber = -1;

    // Подсветка статуса при перетаскивании
    private Rectangle lotBorderHighlight;
    private Label dragStatusHint;

    public ParkingSimulationPane(ParkingService parkingService) {
        this.parkingService = parkingService;
        this.parkingLot = parkingService.getParkingLot();

        setPrefSize(PANE_WIDTH, PANE_HEIGHT);
        setMinSize(PANE_WIDTH, PANE_HEIGHT);
        setMaxSize(PANE_WIDTH, PANE_HEIGHT);
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        // Мягкая тень панели
        setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.05)));

        initLayout();
        refreshAllViews();

        parkingService.addStateChangeListener(this::refreshAllViews);
    }

    private void initLayout() {
        parkingLot.setLotBounds(LOT_X, LOT_Y, LOT_W, LOT_H);

        // 1. Отрисовка Зоны ожидания
        drawWaitingArea();

        // 2. Отрисовка Парковки на 20 мест
        drawParkingLotArea();

        // Индикатор подсказки перетаскивания
        dragStatusHint = new Label();
        dragStatusHint.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        dragStatusHint.setTextFill(Color.WHITE);
        dragStatusHint.setStyle("-fx-background-color: rgba(15, 23, 42, 0.92); -fx-padding: 4 10 4 10; -fx-background-radius: 6;");
        dragStatusHint.setVisible(false);
        dragStatusHint.setMouseTransparent(true);
        dragStatusHint.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.25)));
        getChildren().add(dragStatusHint);
    }

    private void drawWaitingArea() {
        Rectangle bg = new Rectangle(WAITING_AREA_X, WAITING_AREA_Y, WAITING_AREA_W, WAITING_AREA_H);
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setFill(Color.web("#F8FAFC"));
        bg.setStroke(Color.web("#CBD5E1"));
        bg.setStrokeWidth(1.2);

        Text title = new Text("ЗОНА ОЖИДАНИЯ АЭРОПОРТА — 12 ДОСТУПНЫХ АВТОМОБИЛЕЙ (ЗАЖМИТЕ ЛКМ ДЛЯ ВЪЕЗДА)");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        title.setFill(Color.web("#334155"));
        title.setX(WAITING_AREA_X + 16);
        title.setY(WAITING_AREA_Y + 19);

        getChildren().addAll(bg, title);

        // 12 ячеек ожидания (2 ряда по 6)
        double startX = WAITING_AREA_X + 16;
        double startY = WAITING_AREA_Y + 28;
        double cellW = 119;
        double cellH = 80;

        for (int i = 0; i < 12; i++) {
            int row = i / 6;
            int col = i % 6;
            double slotX = startX + col * cellW;
            double slotY = startY + row * (cellH + 2);

            Rectangle slot = new Rectangle(slotX, slotY, cellW - 10, cellH);
            slot.setArcWidth(8);
            slot.setArcHeight(8);
            slot.setFill(Color.web("#FFFFFF"));
            slot.setStroke(Color.web("#E2E8F0"));
            slot.setStrokeLineCap(StrokeLineCap.ROUND);
            slot.getStrokeDashArray().addAll(5.0, 4.0);

            Text slotLabel = new Text("ГОТОВ " + (i + 1));
            slotLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 9));
            slotLabel.setFill(Color.web("#94A3B8"));
            slotLabel.setX(slotX + 33);
            slotLabel.setY(slotY + 44);

            getChildren().addAll(slot, slotLabel);
        }
    }

    private void drawParkingLotArea() {
        // Светлое асфальтовое покрытие парковки
        Rectangle asphalt = new Rectangle(LOT_X, LOT_Y, LOT_W, LOT_H);
        asphalt.setArcWidth(12);
        asphalt.setArcHeight(12);
        asphalt.setFill(Color.web("#F1F5F9"));
        asphalt.setStroke(Color.web("#2563EB"));
        asphalt.setStrokeWidth(2.0);

        // Рамка подсветки выезда за пределы парковки
        lotBorderHighlight = new Rectangle(LOT_X, LOT_Y, LOT_W, LOT_H);
        lotBorderHighlight.setArcWidth(12);
        lotBorderHighlight.setArcHeight(12);
        lotBorderHighlight.setFill(Color.TRANSPARENT);
        lotBorderHighlight.setStroke(Color.web("#10B981"));
        lotBorderHighlight.setStrokeWidth(3.0);
        lotBorderHighlight.setVisible(false);
        lotBorderHighlight.setMouseTransparent(true);

        Text lotTitle = new Text("🅿 ПЛАТНАЯ ОХРАНЯЕМАЯ ПАРКОВКА АЭРОПОРТА — 20 ПРОНУМЕРОВАННЫХ МЕСТ");
        lotTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lotTitle.setFill(Color.web("#1D4ED8"));
        lotTitle.setX(LOT_X + 16);
        lotTitle.setY(LOT_Y + 20);

        getChildren().addAll(asphalt, lotBorderHighlight, lotTitle);

        // Проезжая часть посередине
        double roadY = LOT_Y + 138;
        double roadH = 68;
        Rectangle road = new Rectangle(LOT_X + 2, roadY, LOT_W - 4, roadH);
        road.setFill(Color.web("#E2E8F0"));
        getChildren().add(road);

        // Осевая разметка дороги (желтый пунктир)
        Line centerLine = new Line(LOT_X + 15, roadY + roadH / 2.0, LOT_X + LOT_W - 15, roadY + roadH / 2.0);
        centerLine.setStroke(Color.web("#EAB308"));
        centerLine.setStrokeWidth(2.5);
        centerLine.getStrokeDashArray().addAll(14.0, 10.0);
        getChildren().add(centerLine);

        // Стрелки направления движения
        Text arrowRight = new Text("➡  ПРОЕЗД И ЗОНА МАНЕВРИРОВАНИЯ  ➡");
        arrowRight.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        arrowRight.setFill(Color.web("#64748B"));
        arrowRight.setX(LOT_X + 235);
        arrowRight.setY(roadY + roadH / 2.0 + 4);
        getChildren().add(arrowRight);

        // Размещение 20 парковочных мест:
        // Ряд 1: места 1-10 (вверху)
        // Ряд 2: места 11-20 (внизу)
        double startX = LOT_X + 14;
        double row1Y = LOT_Y + 30;
        double row2Y = roadY + roadH + 4;
        double stepX = (LOT_W - 28) / 10.0;

        for (int i = 1; i <= 20; i++) {
            ParkingSpot spot = parkingLot.getSpot(i);
            int col = (i <= 10) ? (i - 1) : (i - 11);
            double sy = (i <= 10) ? row1Y : row2Y;
            double sx = startX + col * stepX;

            spot.setBounds(sx, sy, SPOT_WIDTH, SPOT_HEIGHT);

            // Карточка парковочного места
            Rectangle spotBox = new Rectangle(sx, sy, SPOT_WIDTH, SPOT_HEIGHT);
            spotBox.setArcWidth(6);
            spotBox.setArcHeight(6);
            spotBox.setFill(Color.web("#FFFFFF"));
            spotBox.setStroke(Color.web("#CBD5E1"));
            spotBox.setStrokeWidth(1.4);
            spotBox.setStrokeType(StrokeType.INSIDE);

            // Номер места
            Text numText = new Text("№ " + i);
            numText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
            numText.setFill(Color.web("#334155"));
            numText.setX(sx + 20);
            numText.setY((i <= 10) ? (sy + 16) : (sy + SPOT_HEIGHT - 8));

            // Статус места
            Text statusText = new Text("СВОБОДНО");
            statusText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8.5));
            statusText.setFill(Color.web("#10B981"));
            statusText.setX(sx + 7);
            statusText.setY((i <= 10) ? (sy + 28) : (sy + SPOT_HEIGHT - 20));

            final int spotNum = i;
            spotBox.setCursor(Cursor.HAND);
            spotBox.setOnMouseClicked(e -> {
                parkingService.setSelectedSpot(parkingLot.getSpot(spotNum));
            });

            spotRectangles.put(i, spotBox);
            spotStatusTexts.put(i, statusText);

            getChildren().addAll(spotBox, numText, statusText);
        }
    }

    public void refreshAllViews() {
        for (CarView cv : carViews.values()) {
            getChildren().remove(cv);
        }
        carViews.clear();

        // 1. Отображаем 12 доступных автомобилей в Зоне ожидания (2 ряда по 6)
        double waitStartX = WAITING_AREA_X + 16;
        double waitStartY = WAITING_AREA_Y + 28;
        double waitCellW = 119;
        double waitCellH = 80;

        var available = parkingService.getAvailableCars();
        for (int i = 0; i < available.size(); i++) {
            Car car = available.get(i);
            CarView cv = new CarView(car);

            int row = i / 6;
            int col = i % 6;
            double posX = waitStartX + col * waitCellW + (waitCellW - 10 - CarView.CAR_WIDTH) / 2.0;
            double posY = waitStartY + row * (waitCellH + 2) + (waitCellH - CarView.CAR_HEIGHT) / 2.0;

            cv.relocate(posX, posY);
            setupIncomingCarDragAndDrop(cv, car, posX, posY);

            carViews.put(car, cv);
            getChildren().add(cv);
        }

        // 2. Отображаем припаркованные автомобили на местах 1-20
        for (int i = 1; i <= 20; i++) {
            ParkingSpot spot = parkingLot.getSpot(i);
            Rectangle spotBox = spotRectangles.get(i);
            Text statusText = spotStatusTexts.get(i);

            boolean isSelected = (parkingService.getSelectedSpot() != null &&
                    parkingService.getSelectedSpot().getSpotNumber() == i);

            if (spot.isOccupied()) {
                Car car = spot.getCar();
                CarView cv = new CarView(car);

                double carX = spot.getX() + (spot.getWidth() - CarView.CAR_WIDTH) / 2.0;
                double carY = spot.getY() + (spot.getHeight() - CarView.CAR_HEIGHT) / 2.0;
                cv.relocate(carX, carY);

                setupParkedCarDragAndDrop(cv, spot, carX, carY);

                carViews.put(car, cv);
                getChildren().add(cv);

                spotBox.setFill(Color.web("#F8FAFC"));
                spotBox.setStroke(isSelected ? Color.web("#F59E0B") : Color.web("#94A3B8"));
                spotBox.setStrokeWidth(isSelected ? 2.5 : 1.2);
                statusText.setText("ЗАНЯТО");
                statusText.setFill(Color.web("#EF4444"));
            } else {
                spotBox.setFill(Color.web("#FFFFFF"));
                spotBox.setStroke(isSelected ? Color.web("#F59E0B") : Color.web("#CBD5E1"));
                spotBox.setStrokeWidth(isSelected ? 2.5 : 1.4);
                statusText.setText("СВОБОДНО");
                statusText.setFill(Color.web("#10B981"));
            }
        }
    }

    private void setupIncomingCarDragAndDrop(CarView cv, Car car, double originalX, double originalY) {
        cv.setCursor(Cursor.OPEN_HAND);

        cv.setOnMousePressed(e -> {
            cv.setCursor(Cursor.CLOSED_HAND);
            cv.toFront();
            dragStatusHint.toFront();

            dragAnchorX = e.getX();
            dragAnchorY = e.getY();
            this.originalX = originalX;
            this.originalY = originalY;
            this.activeDraggingCarView = cv;
            this.isDraggingParkedCar = false;
            cv.setHighlighted(true);
        });

        cv.setOnMouseDragged(e -> {
            double currentX = cv.getLayoutX() + e.getX() - dragAnchorX;
            double currentY = cv.getLayoutY() + e.getY() - dragAnchorY;
            cv.relocate(currentX, currentY);

            double centerX = currentX + CarView.CAR_WIDTH / 2.0;
            double centerY = currentY + CarView.CAR_HEIGHT / 2.0;
            ParkingSpot targetSpot = parkingLot.findSpotAt(centerX, centerY);

            updateDragHoverStatus(targetSpot, currentX, currentY, false);
        });

        cv.setOnMouseReleased(e -> {
            cv.setCursor(Cursor.OPEN_HAND);
            cv.setHighlighted(false);
            dragStatusHint.setVisible(false);
            resetSpotHighlights();

            double currentX = cv.getLayoutX() + e.getX() - dragAnchorX;
            double currentY = cv.getLayoutY() + e.getY() - dragAnchorY;
            double centerX = currentX + CarView.CAR_WIDTH / 2.0;
            double centerY = currentY + CarView.CAR_HEIGHT / 2.0;

            ParkingSpot targetSpot = parkingLot.findSpotAt(centerX, centerY);

            if (targetSpot != null && !targetSpot.isOccupied()) {
                boolean parked = parkingService.tryParkCar(car, targetSpot.getSpotNumber());
                if (!parked) {
                    cv.relocate(originalX, originalY);
                }
            } else {
                cv.relocate(originalX, originalY);
            }
        });
    }

    private void setupParkedCarDragAndDrop(CarView cv, ParkingSpot spot, double originalX, double originalY) {
        cv.setCursor(Cursor.HAND);

        cv.setOnMouseClicked(e -> {
            parkingService.setSelectedSpot(spot);
        });

        cv.setOnMousePressed(e -> {
            parkingService.setSelectedSpot(spot);
            cv.setCursor(Cursor.CLOSED_HAND);
            cv.toFront();
            dragStatusHint.toFront();

            dragAnchorX = e.getX();
            dragAnchorY = e.getY();
            this.originalX = originalX;
            this.originalY = originalY;
            this.activeDraggingCarView = cv;
            this.isDraggingParkedCar = true;
            this.originSpotNumber = spot.getSpotNumber();
            cv.setHighlighted(true);
        });

        cv.setOnMouseDragged(e -> {
            double currentX = cv.getLayoutX() + e.getX() - dragAnchorX;
            double currentY = cv.getLayoutY() + e.getY() - dragAnchorY;
            cv.relocate(currentX, currentY);

            boolean isOutside = parkingLot.isEntirelyOutsideLot(currentX, currentY, CarView.CAR_WIDTH, CarView.CAR_HEIGHT);
            updateDragHoverStatus(null, currentX, currentY, isOutside);
        });

        cv.setOnMouseReleased(e -> {
            cv.setCursor(Cursor.HAND);
            cv.setHighlighted(false);
            dragStatusHint.setVisible(false);
            lotBorderHighlight.setVisible(false);
            resetSpotHighlights();

            double currentX = cv.getLayoutX() + e.getX() - dragAnchorX;
            double currentY = cv.getLayoutY() + e.getY() - dragAnchorY;

            boolean isOutside = parkingLot.isEntirelyOutsideLot(currentX, currentY, CarView.CAR_WIDTH, CarView.CAR_HEIGHT);

            if (isOutside) {
                parkingService.exitCar(originSpotNumber);
            } else {
                cv.relocate(originalX, originalY);
            }
        });
    }

    private void updateDragHoverStatus(ParkingSpot hoveredSpot, double carX, double carY, boolean isOutsideLot) {
        resetSpotHighlights();

        if (isDraggingParkedCar) {
            lotBorderHighlight.setVisible(true);
            if (isOutsideLot) {
                lotBorderHighlight.setStroke(Color.web("#10B981"));
                dragStatusHint.setText("✓ Отпустите для выезда и оплаты");
                dragStatusHint.setStyle("-fx-background-color: #059669; -fx-padding: 4 10 4 10; -fx-background-radius: 6;");
            } else {
                lotBorderHighlight.setStroke(Color.web("#EF4444"));
                dragStatusHint.setText("Вынесите машину целиком за границу для выезда");
                dragStatusHint.setStyle("-fx-background-color: #DC2626; -fx-padding: 4 10 4 10; -fx-background-radius: 6;");
            }
            dragStatusHint.relocate(carX + 10, carY - 26);
            dragStatusHint.setVisible(true);
            return;
        }

        if (hoveredSpot != null) {
            Rectangle rect = spotRectangles.get(hoveredSpot.getSpotNumber());
            if (!hoveredSpot.isOccupied()) {
                rect.setStroke(Color.web("#10B981"));
                rect.setStrokeWidth(2.5);
                dragStatusHint.setText("Место №" + hoveredSpot.getSpotNumber() + " свободно");
                dragStatusHint.setStyle("-fx-background-color: #059669; -fx-padding: 4 10 4 10; -fx-background-radius: 6;");
            } else {
                rect.setStroke(Color.web("#EF4444"));
                rect.setStrokeWidth(2.5);
                dragStatusHint.setText("Место №" + hoveredSpot.getSpotNumber() + " занято!");
                dragStatusHint.setStyle("-fx-background-color: #DC2626; -fx-padding: 4 10 4 10; -fx-background-radius: 6;");
            }
            dragStatusHint.relocate(carX + 10, carY - 26);
            dragStatusHint.setVisible(true);
        } else {
            dragStatusHint.setVisible(false);
        }
    }

    private void resetSpotHighlights() {
        for (int i = 1; i <= 20; i++) {
            ParkingSpot s = parkingLot.getSpot(i);
            Rectangle r = spotRectangles.get(i);
            boolean isSelected = (parkingService.getSelectedSpot() != null &&
                    parkingService.getSelectedSpot().getSpotNumber() == i);

            if (s.isOccupied()) {
                r.setStroke(isSelected ? Color.web("#F59E0B") : Color.web("#94A3B8"));
                r.setStrokeWidth(isSelected ? 2.5 : 1.2);
            } else {
                r.setStroke(isSelected ? Color.web("#F59E0B") : Color.web("#CBD5E1"));
                r.setStrokeWidth(isSelected ? 2.5 : 1.4);
            }
        }
    }
}
