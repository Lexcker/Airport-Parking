package ru.airport.parking.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Модель автомобиля.
 * Хранит уникальный идентификатор, уникальный регистрационный номер на крыше,
 * параметры внешнего вида (вид сверху) и время въезда на парковку.
 */
public class Car {
    private final String id;
    private final String licensePlate;
    private final String bodyColor;
    private final String windowColor;
    private final CarType carType;
    private LocalDateTime entryTime;

    public enum CarType {
        SEDAN,
        SUV,
        HATCHBACK,
        SPORT
    }

    public Car(String id, String licensePlate, String bodyColor, String windowColor, CarType carType) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.licensePlate = Objects.requireNonNull(licensePlate, "licensePlate must not be null");
        this.bodyColor = Objects.requireNonNull(bodyColor, "bodyColor must not be null");
        this.windowColor = windowColor != null ? windowColor : "#1A252C";
        this.carType = carType != null ? carType : CarType.SEDAN;
    }

    public String getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getBodyColor() {
        return bodyColor;
    }

    public String getWindowColor() {
        return windowColor;
    }

    public CarType getCarType() {
        return carType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(id, car.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Car{" +
                "plate='" + licensePlate + '\'' +
                ", color='" + bodyColor + '\'' +
                ", type=" + carType +
                ", entryTime=" + entryTime +
                '}';
    }
}
