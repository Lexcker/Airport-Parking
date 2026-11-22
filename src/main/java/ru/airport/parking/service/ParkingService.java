package ru.airport.parking.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.airport.parking.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Основной сервис управления парковкой.
 * Связывает модель парковки, часы, журнал и финансовые показатели.
 */
public class ParkingService {

    public static final int INCOMING_CARS_COUNT = 12;

    private final ParkingLot parkingLot;
    private final SimulationClock clock;
    private final LogService logService;

    // Список доступных автомобилей на исходном поле (всегда 12 штук)
    private final ObservableList<Car> availableCars = FXCollections.observableArrayList();

    // Счётчики за текущие сутки
    private int dailyRevenue = 0;
    private int dailyEntriesCount = 0;
    private int dailyExitsCount = 0;

    // Счётчики за весь запуск
    private int totalRevenue = 0;
    private int totalEntriesCount = 0;
    private int totalExitsCount = 0;

    // Информация о последнем выезде (Задание 5)
    public record LastExitRecord(String licensePlate, LocalDateTime entryTime, LocalDateTime exitTime, int paidCost, String rateType) {}
    private LastExitRecord lastExitRecord = null;

    // Выбранное место для детального просмотра
    private ParkingSpot selectedSpot = null;

    // Слушатели изменений состояния
    private final List<Runnable> stateChangeListeners = new ArrayList<>();

    public ParkingService(ParkingLot parkingLot, SimulationClock clock, LogService logService) {
        this.parkingLot = Objects.requireNonNull(parkingLot);
        this.clock = Objects.requireNonNull(clock);
        this.logService = Objects.requireNonNull(logService);

        // Заполняем поле 12 автомобилями при старте
        for (int i = 0; i < INCOMING_CARS_COUNT; i++) {
            availableCars.add(LicensePlateGenerator.createRandomCar());
        }

        // Подписываемся на смену суток в учебных часах
        this.clock.addDayTransitionListener(this::handleDayTransition);
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public SimulationClock getClock() {
        return clock;
    }

    public LogService getLogService() {
        return logService;
    }

    public ObservableList<Car> getAvailableCars() {
        return availableCars;
    }

    public synchronized int getDailyRevenue() {
        return dailyRevenue;
    }

    public synchronized int getTotalRevenue() {
        return totalRevenue;
    }

    public synchronized int getDailyEntriesCount() {
        return dailyEntriesCount;
    }

    public synchronized int getDailyExitsCount() {
        return dailyExitsCount;
    }

    public synchronized int getTotalEntriesCount() {
        return totalEntriesCount;
    }

    public synchronized int getTotalExitsCount() {
        return totalExitsCount;
    }

    public synchronized LastExitRecord getLastExitRecord() {
        return lastExitRecord;
    }

    public synchronized ParkingSpot getSelectedSpot() {
        return selectedSpot;
    }

    public synchronized void setSelectedSpot(ParkingSpot spot) {
        this.selectedSpot = spot;
        notifyStateChanged();
    }

    public void addStateChangeListener(Runnable listener) {
        stateChangeListeners.add(listener);
    }

    private void notifyStateChanged() {
        for (Runnable listener : stateChangeListeners) {
            listener.run();
        }
    }

    /**
     * Попытка въезда автомобиля на место spotNumber (Задание 2).
     * Если место свободно, фиксируется время въезда, автомобиль размещается,
     * на исходном поле появляется новый автомобиль (всегда 12 машин),
     * событие заносится в журнал и обновляются счётчики.
     */
    public synchronized boolean tryParkCar(Car car, int spotNumber) {
        if (car == null) return false;
        ParkingSpot spot = parkingLot.getSpot(spotNumber);
        if (spot.isOccupied()) {
            return false;
        }

        LocalDateTime now = clock.getCurrentTime();
        boolean parked = parkingLot.parkCar(spotNumber, car, now);
        if (parked) {
            // Удаляем въехавший автомобиль из доступных и добавляем новый (всегда 12!)
            availableCars.remove(car);
            availableCars.add(LicensePlateGenerator.createRandomCar());

            dailyEntriesCount++;
            totalEntriesCount++;

            logService.logEntry(now, car.getLicensePlate(), spotNumber);
            notifyStateChanged();
            return true;
        }
        return false;
    }

    /**
     * Выезд автомобиля с парковочного места (Задание 2, 4, 5, 6).
     * Рассчитывает оплату, освобождает место, относит выручку к текущим суткам,
     * регистрирует событие в журнале и обновляет данные о последнем выезде.
     */
    public synchronized boolean exitCar(int spotNumber) {
        ParkingSpot spot = parkingLot.getSpot(spotNumber);
        if (!spot.isOccupied()) {
            return false;
        }

        Car car = spot.getCar();
        LocalDateTime exitTime = clock.getCurrentTime();
        LocalDateTime entryTime = car.getEntryTime();

        TariffResult tariff = TariffCalculator.calculate(entryTime, exitTime);
        int cost = tariff.getCost();

        // Освобождаем место
        parkingLot.releaseCar(spotNumber);

        // Обновляем выручку и счётчики
        dailyRevenue += cost;
        totalRevenue += cost;
        dailyExitsCount++;
        totalExitsCount++;

        // Сохраняем информацию о последнем выезде
        this.lastExitRecord = new LastExitRecord(car.getLicensePlate(), entryTime, exitTime, cost, tariff.getRateType());

        // Логируем событие выезда
        logService.logExit(exitTime, car.getLicensePlate(), entryTime, cost);

        // Сбрасываем выбор места, если освободили его
        if (selectedSpot != null && selectedSpot.getSpotNumber() == spotNumber) {
            selectedSpot = null;
        }

        notifyStateChanged();
        return true;
    }

    /**
     * Получить актуальный расчёт стоимости стоянки для выбранного места в реальном времени.
     */
    public synchronized TariffResult getCurrentTariffForSpot(ParkingSpot spot) {
        if (spot == null || !spot.isOccupied() || spot.getCar().getEntryTime() == null) {
            return null;
        }
        return TariffCalculator.calculate(spot.getCar().getEntryTime(), clock.getCurrentTime());
    }

    /**
     * Обработка перехода на новые учебные сутки (Задание 6).
     */
    private synchronized void handleDayTransition(LocalDate completedDay, LocalDate newDay) {
        // Записываем итог прошедшего дня
        logService.logDaySummary(completedDay, dailyEntriesCount, dailyExitsCount, dailyRevenue);

        // Сбрасываем суточные счетчики (общие продолжают накапливаться)
        dailyRevenue = 0;
        dailyEntriesCount = 0;
        dailyExitsCount = 0;

        // Начинаем записи нового дня
        logService.logNewDay(newDay);

        notifyStateChanged();
    }

    /**
     * Завершение работы приложения: сохраняем промежуточный итог текущего дня.
     */
    public synchronized void shutdown() {
        clock.stop();
        logService.logShutdown(clock.getCurrentTime(), dailyEntriesCount, dailyExitsCount, dailyRevenue);
    }
}
