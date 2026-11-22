package ru.airport.parking.model;

import java.time.LocalDate;

/**
 * Итоги работы парковки за одни учебные сутки.
 */
public class DaySummary {
    private final LocalDate date;
    private final int entriesCount;
    private final int exitsCount;
    private final int revenue;

    public DaySummary(LocalDate date, int entriesCount, int exitsCount, int revenue) {
        this.date = date;
        this.entriesCount = entriesCount;
        this.exitsCount = exitsCount;
        this.revenue = revenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getEntriesCount() {
        return entriesCount;
    }

    public int getExitsCount() {
        return exitsCount;
    }

    public int getRevenue() {
        return revenue;
    }

    @Override
    public String toString() {
        return String.format("Итог дня (%s): въехало %d, выехало %d. Выручка: %d ₽.",
                date, entriesCount, exitsCount, revenue);
    }
}
