package ru.airport.parking;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.airport.parking.model.ParkingLot;
import ru.airport.parking.model.SimulationClock;
import ru.airport.parking.service.LogService;
import ru.airport.parking.service.ParkingService;
import ru.airport.parking.ui.MainView;

/**
 * Главный класс приложения JavaFX.
 * Инициализирует модель, сервисы и графический интерфейс.
 */
public class ParkingApplication extends Application {

    private ParkingService parkingService;

    @Override
    public void start(Stage primaryStage) {
        // Инициализация модели и сервисов
        ParkingLot parkingLot = new ParkingLot();
        SimulationClock clock = new SimulationClock();
        LogService logService = new LogService();
        this.parkingService = new ParkingService(parkingLot, clock, logService);

        // Построение пользовательского интерфейса
        MainView root = new MainView(parkingService);
        Scene scene = new Scene(root, 1260, 750);
        try {
            var cssUrl = getClass().getResource("/ru/airport/parking/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {
        }

        primaryStage.setTitle("Платная охраняемая парковка аэропорта");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(720);

        // Обработка закрытия окна: фиксируем промежуточный итог дня в журнале (Задание 6)
        primaryStage.setOnCloseRequest(event -> {
            parkingService.shutdown();
        });

        // Запуск хода учебных часов (Задание 3)
        clock.start();

        primaryStage.show();
    }

    @Override
    public void stop() {
        if (parkingService != null) {
            parkingService.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
