package ru.airport.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Расчёт оплаты за хранение автомобиля на парковке (Задание 4).
 *
 * Правила тарификации:
 * 1. Первые 20 минут включительно бесплатны (0 ₽).
 * 2. При стоянке более 20 минут и до 12 часов включительно каждый начатый час стоит 200 ₽.
 *    Отсчёт начинается с въезда, бесплатные минуты не вычитаются.
 * 3. При стоянке свыше 12 часов вся длительность пересчитывается по 200 ₽ за каждые начатые сутки.
 *    Сутки означают 24 часа с момента въезда. Почасовая сумма к суточной оплате не прибавляется.
 * 4. Тариф выбирается автоматически. После превышения 12 часов сумма снижается с 2 400 ₽ до 200 ₽
 *    (учебное правило проекта).
 */
public class TariffCalculator {

    public static final int HOURLY_RATE = 200;  // 200 ₽ за начатый час
    public static final int DAILY_RATE = 200;   // 200 ₽ за начатые сутки
    public static final long FREE_PERIOD_SECONDS = 20 * 60; // 20 минут = 1200 секунд
    public static final long TWELVE_HOURS_SECONDS = 12 * 3600; // 12 часов = 43200 секунд
    public static final long DAY_SECONDS = 24 * 3600; // 24 часа = 86400 секунд

    /**
     * Рассчитывает стоимость стоянки по времени въезда и выезда.
     */
    public static TariffResult calculate(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            return new TariffResult(0, Duration.ZERO, "Не определено", 0);
        }

        Duration duration = Duration.between(entryTime, exitTime);
        long totalSeconds = duration.getSeconds();
        if (totalSeconds < 0) {
            totalSeconds = 0;
            duration = Duration.ZERO;
        }

        // 1. Первые 20 минут включительно бесплатны
        if (totalSeconds <= FREE_PERIOD_SECONDS) {
            return new TariffResult(0, duration, "Бесплатный период (до 20 мин)", 0);
        }

        // 2. Более 20 минут и до 12 часов включительно — 200 ₽ за каждый начатый час с момента въезда
        if (totalSeconds <= TWELVE_HOURS_SECONDS) {
            int startedHours = (int) ((totalSeconds + 3599) / 3600);
            int cost = startedHours * HOURLY_RATE;
            return new TariffResult(cost, duration, "Почасовой тариф (200 ₽/ч)", startedHours);
        }

        // 3. Свыше 12 часов — 200 ₽ за каждые начатые сутки (24 часа с въезда)
        int startedDays = (int) ((totalSeconds + DAY_SECONDS - 1) / DAY_SECONDS);
        int cost = startedDays * DAILY_RATE;
        return new TariffResult(cost, duration, "Суточный тариф (200 ₽/сут)", startedDays);
    }
}
