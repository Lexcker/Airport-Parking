package ru.airport.parking.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ru.airport.parking.model.Car;
import ru.airport.parking.model.ParkingSpot;
import ru.airport.parking.model.SimulationClock;
import ru.airport.parking.model.TariffResult;
import ru.airport.parking.service.ParkingService;

/**
 * Панель детальной информации о выбранном парковочном месте в светлой теме (Задание 5).
 */
public class SelectedSpotInfoView extends VBox {

    private final ParkingService parkingService;

    private final Label titleLabel;
    private final Label spotNumberLabel;
    private final Label carPlateLabel;
    private final Circle carColorDot;
    private final Label entryTimeLabel;
    private final Label durationLabel;
    private final Label currentCostLabel;
    private final Label tariffTypeLabel;
    private final Label emptyHintLabel;
    private final GridPane detailsGrid;

    public SelectedSpotInfoView(ParkingService parkingService) {
        this.parkingService = parkingService;

        setSpacing(10);
        setPadding(new Insets(14));
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");
        setEffect(new DropShadow(6, Color.rgb(0, 0, 0, 0.04)));

        titleLabel = new Label("ИНФОРМАЦИЯ О МЕСТЕ");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web("#1E293B"));

        emptyHintLabel = new Label("Выберите место на парковке (клик ЛКМ)");
        emptyHintLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        emptyHintLabel.setTextFill(Color.web("#64748B"));
        emptyHintLabel.setWrapText(true);

        detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(8);

        spotNumberLabel = createValueLabel("#0F172A", 13, FontWeight.BOLD);
        carPlateLabel = createValueLabel("#0F172A", 13, FontWeight.BOLD);
        carColorDot = new Circle(6);
        entryTimeLabel = createValueLabel("#334155", 12, FontWeight.NORMAL);
        durationLabel = createValueLabel("#0284C7", 13, FontWeight.BOLD);
        currentCostLabel = createValueLabel("#059669", 14, FontWeight.EXTRA_BOLD);
        tariffTypeLabel = createValueLabel("#64748B", 11.5, FontWeight.NORMAL);

        int r = 0;
        addGridRow(detailsGrid, r++, "Номер места:", spotNumberLabel);

        HBox plateBox = new HBox(6, carColorDot, carPlateLabel);
        plateBox.setAlignment(Pos.CENTER_LEFT);
        addGridRow(detailsGrid, r++, "Автомобиль:", plateBox);

        addGridRow(detailsGrid, r++, "Время въезда:", entryTimeLabel);
        addGridRow(detailsGrid, r++, "Длительность:", durationLabel);
        addGridRow(detailsGrid, r++, "Текущая стоимость:", currentCostLabel);
        addGridRow(detailsGrid, r++, "Тариф:", tariffTypeLabel);

        getChildren().addAll(titleLabel, emptyHintLabel, detailsGrid);

        parkingService.getClock().addTickListener(time -> updateView());
        parkingService.addStateChangeListener(this::updateView);

        updateView();
    }

    private void addGridRow(GridPane grid, int row, String title, javafx.scene.Node valueNode) {
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        titleLbl.setTextFill(Color.web("#64748B"));
        grid.add(titleLbl, 0, row);
        grid.add(valueNode, 1, row);
    }

    private Label createValueLabel(String hexColor, double size, FontWeight weight) {
        Label lbl = new Label("-");
        lbl.setFont(Font.font("Segoe UI", weight, size));
        lbl.setTextFill(Color.web(hexColor));
        return lbl;
    }

    public void updateView() {
        ParkingSpot spot = parkingService.getSelectedSpot();
        if (spot == null) {
            emptyHintLabel.setText("Выберите место на парковке (клик ЛКМ)");
            emptyHintLabel.setVisible(true);
            emptyHintLabel.setManaged(true);
            detailsGrid.setVisible(false);
            detailsGrid.setManaged(false);
            return;
        }

        emptyHintLabel.setVisible(false);
        emptyHintLabel.setManaged(false);
        detailsGrid.setVisible(true);
        detailsGrid.setManaged(true);

        spotNumberLabel.setText("Место № " + spot.getSpotNumber());

        if (spot.isOccupied()) {
            Car car = spot.getCar();
            carPlateLabel.setText(car.getLicensePlate());
            carColorDot.setFill(Color.web(car.getBodyColor()));
            carColorDot.setVisible(true);

            if (car.getEntryTime() != null) {
                entryTimeLabel.setText(car.getEntryTime().format(SimulationClock.FULL_FORMATTER));

                TariffResult tariff = parkingService.getCurrentTariffForSpot(spot);
                if (tariff != null) {
                    durationLabel.setText(tariff.getFormattedDuration());
                    currentCostLabel.setText(tariff.getCost() + " ₽");
                    tariffTypeLabel.setText(tariff.getRateType());
                } else {
                    durationLabel.setText("-");
                    currentCostLabel.setText("0 ₽");
                    tariffTypeLabel.setText("-");
                }
            } else {
                entryTimeLabel.setText("-");
                durationLabel.setText("-");
                currentCostLabel.setText("0 ₽");
                tariffTypeLabel.setText("-");
            }
        } else {
            carPlateLabel.setText("Свободно");
            carColorDot.setVisible(false);
            entryTimeLabel.setText("—");
            durationLabel.setText("—");
            currentCostLabel.setText("0 ₽");
            tariffTypeLabel.setText("Свободно");
        }
    }
}
