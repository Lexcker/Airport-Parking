package ru.airport.parking.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Модель парковки аэропорта.
 * Содержит 20 пронумерованных мест (№1 - №20).
 * При запуске все места свободны.
 */
public class ParkingLot {
    public static final int TOTAL_SPOTS = 20;

    private final List<ParkingSpot> spots;

    // Границы территории парковки
    private double lotX;
    private double lotY;
    private double lotWidth;
    private double lotHeight;

    public ParkingLot() {
        this.spots = new ArrayList<>(TOTAL_SPOTS);
        for (int i = 1; i <= TOTAL_SPOTS; i++) {
            spots.add(new ParkingSpot(i));
        }
    }

    public List<ParkingSpot> getSpots() {
        return Collections.unmodifiableList(spots);
    }

    public ParkingSpot getSpot(int spotNumber) {
        if (spotNumber < 1 || spotNumber > TOTAL_SPOTS) {
            throw new IllegalArgumentException("Номер места должен быть от 1 до 20, получено: " + spotNumber);
        }
        return spots.get(spotNumber - 1);
    }

    public int getOccupiedCount() {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (spot.isOccupied()) {
                count++;
            }
        }
        return count;
    }

    public int getFreeCount() {
        return TOTAL_SPOTS - getOccupiedCount();
    }

    /**
     * Разместить автомобиль на месте с фиксацией времени въезда.
     * Занятое место не принимает другую машину.
     */
    public boolean parkCar(int spotNumber, Car car, LocalDateTime entryTime) {
        ParkingSpot spot = getSpot(spotNumber);
        if (spot.isOccupied()) {
            return false;
        }
        car.setEntryTime(entryTime);
        spot.setCar(car);
        return true;
    }

    /**
     * Освободить место и вернуть автомобиль.
     */
    public Car releaseCar(int spotNumber) {
        ParkingSpot spot = getSpot(spotNumber);
        Car car = spot.getCar();
        spot.clear();
        return car;
    }

    /**
     * Поиск места, на котором стоит указанный автомобиль.
     */
    public ParkingSpot findSpotByCar(Car car) {
        if (car == null) return null;
        for (ParkingSpot spot : spots) {
            if (car.equals(spot.getCar())) {
                return spot;
            }
        }
        return null;
    }

    /**
     * Поиск места по координатам центра автомобиля.
     */
    public ParkingSpot findSpotAt(double px, double py) {
        for (ParkingSpot spot : spots) {
            if (spot.contains(px, py)) {
                return spot;
            }
        }
        return null;
    }

    public void setLotBounds(double x, double y, double width, double height) {
        this.lotX = x;
        this.lotY = y;
        this.lotWidth = width;
        this.lotHeight = height;
    }

    public double getLotX() {
        return lotX;
    }

    public double getLotY() {
        return lotY;
    }

    public double getLotWidth() {
        return lotWidth;
    }

    public double getLotHeight() {
        return lotHeight;
    }

    /**
     * Проверяет, вынесен ли прямоугольник автомобиля ЦЕЛИКОМ за пределы парковки.
     * (Задание 2: Для выезда машину нужно целиком вынести за границу парковки).
     */
    public boolean isEntirelyOutsideLot(double carX, double carY, double carW, double carH) {
        double carRight = carX + carW;
        double carBottom = carY + carH;
        double lotRight = lotX + lotWidth;
        double lotBottom = lotY + lotHeight;

        // Прямоугольники не пересекаются вовсе
        return carRight < lotX || carX > lotRight || carBottom < lotY || carY > lotBottom;
    }
}
