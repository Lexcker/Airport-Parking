package ru.airport.parking.model;

import java.util.Objects;

/**
 * Парковочное место.
 * Парковка содержит 20 пронумерованных мест (№1 - №20).
 */
public class ParkingSpot {
    private final int spotNumber;
    private Car car;

    // Геометрические координаты на парковке (для отрисовки и попадания ЛКМ)
    private double x;
    private double y;
    private double width;
    private double height;

    public ParkingSpot(int spotNumber) {
        this.spotNumber = spotNumber;
        this.car = null;
    }

    public int getSpotNumber() {
        return spotNumber;
    }

    public boolean isOccupied() {
        return car != null;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void clear() {
        this.car = null;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setBounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Проверяет, попадает ли точка (например, центр машины) внутрь парковочного места.
     */
    public boolean contains(double px, double py) {
        return px >= x && px <= (x + width) && py >= y && py <= (y + height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return spotNumber == that.spotNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spotNumber);
    }

    @Override
    public String toString() {
        return "ParkingSpot №" + spotNumber + (isOccupied() ? " [Занято: " + car.getLicensePlate() + "]" : " [Свободно]");
    }
}
