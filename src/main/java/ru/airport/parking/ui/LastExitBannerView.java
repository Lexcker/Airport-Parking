package ru.airport.parking.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ru.airport.parking.model.SimulationClock;
import ru.airport.parking.service.ParkingService;

/**
 * Блок отображения последнего выезда автомобиля в светлой теме (Задание 5).
 */
public class LastExitBannerView extends VBox {

    private final ParkingService parkingService;

    private final Label emptyLabel;
    private final GridPane detailsGrid;
    private final Label plateLabel;
    private final Label entryLabel;
    private final Label exitLabel;
    private final Label paidLabel;

    public LastExitBannerView(ParkingService parkingService) {
        this.parkingService = parkingService;

        setSpacing(10);
        setPadding(new Insets(14));
        setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");
        setEffect(new DropShadow(6, Color.rgb(0, 0, 0, 0.04)));

        Label title = new Label("ПОСЛЕДНИЙ ВЫЕЗД И ОПЛАТА");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        title.setTextFill(Color.web("#1E293B"));

        emptyLabel = new Label("Выездов в текущей сессии еще не было");
        emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        emptyLabel.setTextFill(Color.web("#64748B"));

        detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(6);

        plateLabel = new Label("-");
        plateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        plateLabel.setTextFill(Color.web("#0F172A"));

        entryLabel = new Label("-");
        entryLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        entryLabel.setTextFill(Color.web("#334155"));

        exitLabel = new Label("-");
        exitLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        exitLabel.setTextFill(Color.web("#334155"));

        paidLabel = new Label("0 ₽");
        paidLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 14));
        paidLabel.setTextFill(Color.web("#059669"));

        detailsGrid.add(createTitle("Автомобиль:"), 0, 0);
        detailsGrid.add(plateLabel, 1, 0);

        detailsGrid.add(createTitle("Время въезда:"), 0, 1);
        detailsGrid.add(entryLabel, 1, 1);

        detailsGrid.add(createTitle("Время выезда:"), 0, 2);
        detailsGrid.add(exitLabel, 1, 2);

        detailsGrid.add(createTitle("Оплачено:"), 0, 3);
        detailsGrid.add(paidLabel, 1, 3);

        getChildren().addAll(title, emptyLabel, detailsGrid);

        detailsGrid.setVisible(false);
        detailsGrid.setManaged(false);

        parkingService.addStateChangeListener(this::updateView);
    }

    private Label createTitle(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lbl.setTextFill(Color.web("#64748B"));
        return lbl;
    }

    private void updateView() {
        ParkingService.LastExitRecord rec = parkingService.getLastExitRecord();
        if (rec == null) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            detailsGrid.setVisible(false);
            detailsGrid.setManaged(false);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        detailsGrid.setVisible(true);
        detailsGrid.setManaged(true);

        plateLabel.setText(rec.licensePlate());
        entryLabel.setText(rec.entryTime().format(SimulationClock.FULL_FORMATTER));
        exitLabel.setText(rec.exitTime().format(SimulationClock.FULL_FORMATTER));
        paidLabel.setText(rec.paidCost() + " ₽ (" + rec.rateType() + ")");
    }
}
