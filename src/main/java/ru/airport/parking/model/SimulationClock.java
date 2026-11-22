package ru.airport.parking.model;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class SimulationClock {

    public static final LocalDateTime START_TIME = LocalDateTime.of(2030, 1, 1, 8, 0, 0);
    public static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FunctionalInterface
    public interface DayTransitionListener {
        void onDayCompleted(LocalDate completedDay, LocalDate newDay);
    }

    private LocalDateTime currentTime;
    private double accumulatedSecondsFraction;
    private double speedFactor = 1.0; // 1.0 = норма (1 сек реала = 1 мин учебного времени)
    private boolean paused = false;

    private final List<Consumer<LocalDateTime>> tickListeners = new ArrayList<>();
    private final List<DayTransitionListener> dayTransitionListeners = new ArrayList<>();

    private AnimationTimer timer;
    private long lastNanoTime = 0;

    public SimulationClock() {
        this.currentTime = START_TIME;
    }

    public synchronized LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public synchronized String getFormattedDateTime() {
        return currentTime.format(FULL_FORMATTER);
    }

    public synchronized String getFormattedDate() {
        return currentTime.format(DATE_FORMATTER);
    }

    public synchronized String getFormattedTime() {
        return currentTime.format(TIME_FORMATTER);
    }

    public void addTickListener(Consumer<LocalDateTime> listener) {
        tickListeners.add(Objects.requireNonNull(listener));
    }

    public void addDayTransitionListener(DayTransitionListener listener) {
        dayTransitionListeners.add(Objects.requireNonNull(listener));
    }


    public void start() {
        if (timer != null) {
            timer.stop();
        }
        lastNanoTime = 0;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNanoTime == 0) {
                    lastNanoTime = now;
                    return;
                }

                double elapsedRealSeconds = (now - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = now;

                if (!paused && elapsedRealSeconds > 0) {
                    // 1 реальная секунда при speedFactor 1.0 = 60 учебных секунд
                    double simSecondsToAdd = elapsedRealSeconds * 60.0 * speedFactor;
                    stepSeconds(simSecondsToAdd);
                }
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Шаг времени на заданное количество учебных секунд.
     */
    public synchronized void stepSeconds(double simSeconds) {
        accumulatedSecondsFraction += simSeconds;
        long wholeSeconds = (long) accumulatedSecondsFraction;
        if (wholeSeconds > 0) {
            accumulatedSecondsFraction -= wholeSeconds;
            advanceInternal(wholeSeconds);
        }
    }

    /**
     * Прямое ускоренное продвижение времени (для защиты проекта и тестирования).
     */
    public synchronized void advanceSeconds(long seconds) {
        if (seconds <= 0) return;
        advanceInternal(seconds);
    }

    public synchronized void advanceMinutes(long minutes) {
        advanceSeconds(minutes * 60);
    }

    public synchronized void advanceHours(long hours) {
        advanceSeconds(hours * 3600);
    }

    public synchronized void advanceDays(long days) {
        advanceSeconds(days * 86400);
    }

    private void advanceInternal(long secondsToAdd) {
        LocalDateTime oldTime = currentTime;
        LocalDateTime targetTime = currentTime.plusSeconds(secondsToAdd);

        // Проверяем смену суток
        LocalDate currentDay = oldTime.toLocalDate();
        LocalDate targetDay = targetTime.toLocalDate();

        if (targetDay.isAfter(currentDay)) {
            LocalDate dayCursor = currentDay;
            while (dayCursor.isBefore(targetDay)) {
                LocalDate nextDay = dayCursor.plusDays(1);
                notifyDayTransition(dayCursor, nextDay);
                dayCursor = nextDay;
            }
        }

        currentTime = targetTime;
        notifyTick(currentTime);
    }

    private void notifyDayTransition(LocalDate completedDay, LocalDate newDay) {
        for (DayTransitionListener listener : dayTransitionListeners) {
            try {
                listener.onDayCompleted(completedDay, newDay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyTick(LocalDateTime time) {
        for (Consumer<LocalDateTime> listener : tickListeners) {
            try {
                listener.accept(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }

    public synchronized void togglePause() {
        this.paused = !this.paused;
    }

    public synchronized double getSpeedFactor() {
        return speedFactor;
    }

    public synchronized void setSpeedFactor(double speedFactor) {
        this.speedFactor = Math.max(0.1, speedFactor);
    }
}
