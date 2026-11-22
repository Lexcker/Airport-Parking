package ru.airport.parking.model;

import java.time.Duration;

/**
 * Результат тарификации стоянки автомобиля.
 */
public class TariffResult {
    private final int cost;
    private final Duration duration;
    private final String rateType;
    private final int billableUnits;

    public TariffResult(int cost, Duration duration, String rateType, int billableUnits) {
        this.cost = cost;
        this.duration = duration;
        this.rateType = rateType;
        this.billableUnits = billableUnits;
    }

    public int getCost() {
        return cost;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getRateType() {
        return rateType;
    }

    public int getBillableUnits() {
        return billableUnits;
    }

    /**
     * Форматированное текстовое представление длительности хранения.
     * Например: "23 ч 48 мин 00 с" или "20 мин 01 с" или "1 дн 4 ч 1 мин 00 с".
     */
    public String getFormattedDuration() {
        long totalSec = Math.max(0, duration.getSeconds());
        long days = totalSec / 86400;
        long hours = (totalSec % 86400) / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" дн ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append(" ч ");
        }
        sb.append(minutes).append(" мин ");
        sb.append(seconds).append(" с");
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "TariffResult{" +
                "cost=" + cost + " ₽" +
                ", duration=" + getFormattedDuration() +
                ", rateType='" + rateType + '\'' +
                ", billableUnits=" + billableUnits +
                '}';
    }
}
