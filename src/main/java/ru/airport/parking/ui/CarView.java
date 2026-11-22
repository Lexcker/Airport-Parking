package ru.airport.parking.ui;

import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ru.airport.parking.model.Car;

/**
 * Графический узел автомобиля (вид сверху).
 * Оптимизирован для светлой темы оформления: контрастная тень, читаемый номер на крыше,
 * стекла, зеркала и фары.
 */
public class CarView extends Pane {

    public static final double CAR_WIDTH = 46.0;
    public static final double CAR_HEIGHT = 78.0;

    private final Car car;
    private final Rectangle body;
    private final DropShadow dropShadow;

    public CarView(Car car) {
        this.car = car;
        setPrefSize(CAR_WIDTH, CAR_HEIGHT);
        setMaxSize(CAR_WIDTH, CAR_HEIGHT);
        setMinSize(CAR_WIDTH, CAR_HEIGHT);

        Color mainColor = Color.web(car.getBodyColor());

        // Тень автомобиля для светлого фона
        dropShadow = new DropShadow(6, Color.rgb(15, 23, 42, 0.28));
        dropShadow.setOffsetY(3);
        setEffect(dropShadow);

        // 4 Колеса
        double tireW = 6;
        double tireH = 14;
        Rectangle wheelFL = createTire(1, 12, tireW, tireH);
        Rectangle wheelFR = createTire(CAR_WIDTH - tireW - 1, 12, tireW, tireH);
        Rectangle wheelRL = createTire(1, CAR_HEIGHT - tireH - 12, tireW, tireH);
        Rectangle wheelRR = createTire(CAR_WIDTH - tireW - 1, CAR_HEIGHT - tireH - 12, tireW, tireH);

        // Кузов автомобиля
        body = new Rectangle(3, 0, CAR_WIDTH - 6, CAR_HEIGHT);
        body.setArcWidth(16);
        body.setArcHeight(16);
        body.setFill(mainColor);
        body.setStroke(Color.rgb(15, 23, 42, 0.35));
        body.setStrokeWidth(1.2);

        // Боковые зеркала
        Rectangle mirrorL = new Rectangle(0, 20, 3.5, 6);
        mirrorL.setArcWidth(2.5);
        mirrorL.setArcHeight(2.5);
        mirrorL.setFill(mainColor.darker());

        Rectangle mirrorR = new Rectangle(CAR_WIDTH - 3.5, 20, 3.5, 6);
        mirrorR.setArcWidth(2.5);
        mirrorR.setArcHeight(2.5);
        mirrorR.setFill(mainColor.darker());

        // Передние фары
        Rectangle lightFL = new Rectangle(6, 2, 6, 3.5);
        lightFL.setArcWidth(1.5);
        lightFL.setArcHeight(1.5);
        lightFL.setFill(Color.web("#FFFDE7"));
        lightFL.setStroke(Color.rgb(0, 0, 0, 0.2));

        Rectangle lightFR = new Rectangle(CAR_WIDTH - 12, 2, 6, 3.5);
        lightFR.setArcWidth(1.5);
        lightFR.setArcHeight(1.5);
        lightFR.setFill(Color.web("#FFFDE7"));
        lightFR.setStroke(Color.rgb(0, 0, 0, 0.2));

        // Задние фонари
        Rectangle lightRL = new Rectangle(6, CAR_HEIGHT - 3.5, 7, 2.5);
        lightRL.setArcWidth(1.5);
        lightRL.setArcHeight(1.5);
        lightRL.setFill(Color.web("#EF4444"));

        Rectangle lightRR = new Rectangle(CAR_WIDTH - 13, CAR_HEIGHT - 3.5, 7, 2.5);
        lightRR.setArcWidth(1.5);
        lightRR.setArcHeight(1.5);
        lightRR.setFill(Color.web("#EF4444"));

        // Лобовое стекло
        Rectangle windshieldFront = new Rectangle(7, 14, CAR_WIDTH - 14, 13);
        windshieldFront.setArcWidth(5);
        windshieldFront.setArcHeight(5);
        windshieldFront.setFill(Color.web("#0F172A"));

        // Блик на стекле
        Rectangle glint = new Rectangle(9, 16, 3, 9);
        glint.setFill(Color.rgb(255, 255, 255, 0.4));

        // Заднее стекло
        Rectangle windshieldRear = new Rectangle(8, CAR_HEIGHT - 22, CAR_WIDTH - 16, 10);
        windshieldRear.setArcWidth(4);
        windshieldRear.setArcHeight(4);
        windshieldRear.setFill(Color.web("#0F172A"));

        // Крыша
        Rectangle roof = new Rectangle(6, 28, CAR_WIDTH - 12, CAR_HEIGHT - 51);
        roof.setArcWidth(7);
        roof.setArcHeight(7);
        roof.setFill(mainColor.deriveColor(0, 0.95, 1.15, 1.0));
        roof.setStroke(Color.rgb(0, 0, 0, 0.2));

        // Номерной знак на крыше (Задание 1)
        StackPane plateBadge = new StackPane();
        plateBadge.setLayoutX(5);
        plateBadge.setLayoutY(32);
        plateBadge.setPrefSize(CAR_WIDTH - 10, 16);

        Rectangle plateBg = new Rectangle(CAR_WIDTH - 10, 16);
        plateBg.setArcWidth(3);
        plateBg.setArcHeight(3);
        plateBg.setFill(Color.WHITE);
        plateBg.setStroke(Color.rgb(15, 23, 42, 0.8));
        plateBg.setStrokeWidth(1.0);

        Text plateText = new Text(car.getLicensePlate());
        plateText.setFont(Font.font("Consolas", FontWeight.BOLD, 9.5));
        plateText.setFill(Color.web("#0F172A"));

        plateBadge.getChildren().addAll(plateBg, plateText);
        plateBadge.setAlignment(Pos.CENTER);

        getChildren().addAll(
                wheelFL, wheelFR, wheelRL, wheelRR,
                body,
                mirrorL, mirrorR,
                lightFL, lightFR,
                lightRL, lightRR,
                windshieldFront, glint,
                windshieldRear,
                roof,
                plateBadge
        );
    }

    private Rectangle createTire(double x, double y, double w, double h) {
        Rectangle tire = new Rectangle(x, y, w, h);
        tire.setArcWidth(3);
        tire.setArcHeight(3);
        tire.setFill(Color.web("#1E293B"));
        return tire;
    }

    public Car getCar() {
        return car;
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            dropShadow.setColor(Color.web("#0284C7"));
            dropShadow.setRadius(12);
            setScaleX(1.06);
            setScaleY(1.06);
        } else {
            dropShadow.setColor(Color.rgb(15, 23, 42, 0.28));
            dropShadow.setRadius(6);
            setScaleX(1.0);
            setScaleY(1.0);
        }
    }
}
