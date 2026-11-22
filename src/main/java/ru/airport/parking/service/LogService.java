package ru.airport.parking.service;

import javafx.application.Platform;
import ru.airport.parking.model.SimulationClock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Сервис логирования событий парковки (Задание 6).
 *
 * Сообщения о въездах и выездах видны в окне и сразу сохраняются в файл.
 * Каждый запуск создаёт новый файл журнала в папке logs/. Старые файлы сохраняются.
 * По окончании 23:59 подводит итог дня, затем начинает записи следующего дня.
 * При закрытии программы записывает промежуточный итог дня.
 */
public class LogService {

    private static final DateTimeFormatter FILE_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final File logFile;
    private BufferedWriter writer;
    private final List<Consumer<String>> logListeners = new ArrayList<>();
    private final List<String> messageHistory = new ArrayList<>();
    private boolean closed = false;

    public LogService() {
        File dir = new File("logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timestamp = LocalDateTime.now().format(FILE_TS_FORMATTER);
        this.logFile = new File(dir, "parking_log_" + timestamp + ".txt");

        try {
            this.writer = new BufferedWriter(new FileWriter(logFile, StandardCharsets.UTF_8, true));
            writeLine("=== Журнал работы платной парковки аэропорта ===");
            writeLine("Сессия начата: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            writeLine("--------------------------------------------------");
        } catch (IOException e) {
            System.err.println("Ошибка создания файла журнала: " + e.getMessage());
        }
    }

    public synchronized void addListener(Consumer<String> listener) {
        logListeners.add(listener);
    }

    public File getLogFile() {
        return logFile;
    }

    public synchronized List<String> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    /**
     * Въезд: номер машины, номер места, учебная дата и время.
     * Пример: 01.01.2027 08:00:00. Автомобиль 1ABC234 заехал на место №7.
     */
    public synchronized void logEntry(LocalDateTime time, String carPlate, int spotNumber) {
        String msg = String.format("%s. Автомобиль %s заехал на место №%d.",
                time.format(SimulationClock.FULL_FORMATTER), carPlate, spotNumber);
        writeLine(msg);
    }

    /**
     * Выезд: номер машины, даты и время въезда и выезда, сумма оплаты.
     * Пример: 01.01.2027 09:00:01. Автомобиль 1ABC234 выехал. Въезд: 01.01.2027 08:00:00. Оплачено: 400 ₽.
     */
    public synchronized void logExit(LocalDateTime exitTime, String carPlate, LocalDateTime entryTime, int cost) {
        String msg = String.format("%s. Автомобиль %s выехал. Въезд: %s. Оплачено: %d ₽.",
                exitTime.format(SimulationClock.FULL_FORMATTER),
                carPlate,
                entryTime.format(SimulationClock.FULL_FORMATTER),
                cost);
        writeLine(msg);
    }

    /**
     * Итог дня по окончании 23:59.
     * Пример: 01.01.2027 23:59:59. Итог дня: въехало 1, выехало 1. Выручка: 400 ₽.
     */
    public synchronized void logDaySummary(LocalDate completedDay, int entries, int exits, int revenue) {
        String msg = String.format("%s 23:59:59. Итог дня: въехало %d, выехало %d. Выручка: %d ₽.",
                completedDay.format(SimulationClock.DATE_FORMATTER),
                entries,
                exits,
                revenue);
        writeLine(msg);
    }

    /**
     * Начало записей нового дня.
     * Пример: 02.01.2027. Начало записей нового дня.
     */
    public synchronized void logNewDay(LocalDate newDay) {
        String msg = String.format("%s. Начало записей нового дня.",
                newDay.format(SimulationClock.DATE_FORMATTER));
        writeLine(msg);
    }

    /**
     * Промежуточный итог дня при обычном закрытии программы.
     */
    public synchronized void logShutdown(LocalDateTime time, int entries, int exits, int revenue) {
        String msg = String.format("%s. Завершение работы. Промежуточный итог дня: въехало %d, выехало %d. Выручка: %d ₽.",
                time.format(SimulationClock.FULL_FORMATTER),
                entries,
                exits,
                revenue);
        writeLine(msg);
        close();
    }

    private void writeLine(String line) {
        messageHistory.add(line);
        if (writer != null && !closed) {
            try {
                writer.write(line);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("Ошибка записи в лог-файл: " + e.getMessage());
            }
        }

        // Оповещаем UI подписчиков (в потоке JavaFX Application Thread)
        for (Consumer<String> listener : logListeners) {
            if (Platform.isFxApplicationThread()) {
                listener.accept(line);
            } else {
                Platform.runLater(() -> listener.accept(line));
            }
        }
    }

    public synchronized void close() {
        if (!closed) {
            closed = true;
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
