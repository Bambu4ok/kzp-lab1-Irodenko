package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Набір чистих потокових запитів (Stream API) для аналізу комунальних показників (Варіант 9).
 */
public final class UtilityReport {

    private UtilityReport() {
    }

    /** 1. Відбір (filter): показники, де споживання суворо перевищує поріг. */
    public static List<UtilityReading> filterAboveThreshold(List<UtilityReading> readings, double threshold) {
        return streamOf(readings)
                .filter(reading -> reading.getConsumption() > threshold)
                .toList();
    }

    /** 2. Перетворення (map): список значень споживання. */
    public static List<Double> mapToConsumptions(List<UtilityReading> readings) {
        return streamOf(readings)
                .map(UtilityReading::getConsumption)
                .toList();
    }

    /** 3. Групування (groupingBy): сумарне споживання за кожним лічильником. */
    public static Map<String, Double> sumConsumptionByMeter(List<UtilityReading> readings) {
        return streamOf(readings)
                .collect(Collectors.groupingBy(
                        UtilityReading::getName,
                        TreeMap::new,
                        Collectors.summingDouble(UtilityReading::getConsumption)
                ));
    }

    /** 4. Зведена статистика (summarizingDouble): count, min, max, sum, average. */
    public static DoubleSummaryStatistics consumptionStatistics(List<UtilityReading> readings) {
        return streamOf(readings)
                .collect(Collectors.summarizingDouble(UtilityReading::getConsumption));
    }

    /** 5. Складене сортування та обмеження (sorted + limit): top-N за споживанням (спаданням) і датою (зростанням). */
    public static List<UtilityReading> topReadingsByConsumption(List<UtilityReading> readings, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним");
        }
        Comparator<UtilityReading> byConsumptionDescThenDateAsc = Comparator
                .comparingDouble(UtilityReading::getConsumption)
                .reversed()
                .thenComparing(UtilityReading::getDate);

        return streamOf(readings)
                .sorted(byConsumptionDescThenDateAsc)
                .limit(limit)
                .toList();
    }

    /** Пошук (Optional): перший запис за назвою лічильника. */
    public static Optional<UtilityReading> findFirstByMeter(List<UtilityReading> readings, String meterName) {
        Objects.requireNonNull(meterName, "Назва лічильника не може бути null");
        return streamOf(readings)
                .filter(reading -> reading.getName().equals(meterName))
                .findFirst();
    }

    private static Stream<UtilityReading> streamOf(List<UtilityReading> readings) {
        return Objects.requireNonNull(readings, "Список показників не може бути null").stream();
    }
}