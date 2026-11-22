package ru.airport.parking.model;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Генератор случайных уникальных номеров и автомобилей.
 * Гарантирует уникальность номеров автомобилей в пределах одного запуска программы.
 */
public class LicensePlateGenerator {
    private static final Random RANDOM = new Random();
    private static final Set<String> USED_PLATES = new HashSet<>();

    private static final String[] LETTERS = {
            "A", "B", "C", "E", "H", "K", "M", "O", "P", "T", "X", "Y"
    };

    private static final String[] PALETTE = {
            "#E53935", // Red
            "#1E88E5", // Blue
            "#43A047", // Green
            "#FB8C00", // Orange
            "#8E24AA", // Purple
            "#00ACC1", // Cyan
            "#D81B60", // Magenta
            "#FDD835", // Yellow
            "#3949AB", // Indigo
            "#546E7A", // Blue Grey
            "#FFFFFF", // White
            "#212121"  // Dark Grey / Black
    };

    public static synchronized String generateUniquePlate() {
        while (true) {
            String plate;
            int style = RANDOM.nextInt(3);
            if (style == 0) {
                // Style: 1ABC234 (как в примере задания)
                plate = String.format("%d%s%s%s%03d",
                        1 + RANDOM.nextInt(9),
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        RANDOM.nextInt(1000));
            } else if (style == 1) {
                // Style: A123BC (стандартный номер)
                plate = String.format("%s%03d%s%s",
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        RANDOM.nextInt(1000),
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        LETTERS[RANDOM.nextInt(LETTERS.length)]);
            } else {
                // Style: 7XYZ789
                plate = String.format("%d%s%s%s%03d",
                        RANDOM.nextInt(10),
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        LETTERS[RANDOM.nextInt(LETTERS.length)],
                        100 + RANDOM.nextInt(900));
            }

            if (!USED_PLATES.contains(plate)) {
                USED_PLATES.add(plate);
                return plate;
            }
        }
    }

    public static Car createRandomCar() {
        String id = UUID.randomUUID().toString();
        String plate = generateUniquePlate();
        String color = PALETTE[RANDOM.nextInt(PALETTE.length)];
        Car.CarType[] types = Car.CarType.values();
        Car.CarType type = types[RANDOM.nextInt(types.length)];
        return new Car(id, plate, color, "#1A252C", type);
    }

    public static synchronized void reset() {
        USED_PLATES.clear();
    }
}
