package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Main {
    private static final double CONSUMPTION_THRESHOLD = 100.0;
    private static final int TOP_LIMIT = 5;

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--help".equals(args[0])) {
            System.out.println("Використання: java -jar lab01.jar [--help] [--version] [--input <файл>] [--output <файл>]");
            return;
        }
        if (args.length > 0 && "--version".equals(args[0])) {
            System.out.println("Версія: 4.0.0");
            return;
        }
        Path inputPath = Path.of("data", "input.csv");
        Path outputPath = Path.of("out", "report.txt");

        for (int i = 0; i < args.length; i++) {
            if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = Path.of(args[i + 1]);
            }
            if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputPath = Path.of(args[i + 1]);
            }
        }

        try {
            List<String> lines = FileReport.readLines(inputPath);
            String report = processData(lines);
            System.out.print(report);
            FileReport.writeReport(outputPath, report);
        } catch (IOException e) {
            System.err.printf(Locale.ROOT, "Помилка при роботі з файлом: %s%n", e.getMessage());
        }
    }

    public static String processData(List<String> lines) {
        List<UtilityReading> readings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                readings.add(UtilityReading.fromCsv(line));
            } catch (IllegalArgumentException e) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: %s", i + 1, e.getMessage()));
            }
        }

        DoubleSummaryStatistics stats = UtilityReport.consumptionStatistics(readings);
        int validCount = (int) stats.getCount();
        double totalConsumption = stats.getSum();
        double maxConsumption = validCount > 0 ? stats.getMax() : 0.0;
        double totalCost = readings.stream()
                .mapToDouble(UtilityReading::calculateCost)
                .sum();

        UtilitySummary summary = new UtilitySummary(validCount, totalConsumption, totalCost, maxConsumption);

        StringBuilder sb = new StringBuilder();
        sb.append("=== ЗВІТ ПО КОМУНАЛЬНИХ ПОКАЗНИКАХ ===\n");
        sb.append(String.format(Locale.ROOT, "1. Кількість коректних записів: %d%n", summary.validCount()));
        sb.append(String.format(Locale.ROOT, "2. Сумарне споживання: %.2f%n", summary.totalConsumption()));
        sb.append(String.format(Locale.ROOT, "3. Загальна вартість: %.2f грн%n", summary.totalCost()));
        sb.append(String.format(Locale.ROOT, "4. Найбільше споживання: %.2f%n", summary.maxConsumption()));
        sb.append(String.format(Locale.ROOT, "Помилок/пропущено рядків: %d%n", errors.size()));

        if (!errors.isEmpty()) {
            sb.append("Деталі помилок:\n");
            for (String err : errors) {
                sb.append(" - ").append(err).append("\n");
            }
        }

        sb.append("\n=== ПОТОКОВА АНАЛІТИКА (STREAM API) ===\n");
        List<UtilityReading> aboveThreshold = UtilityReport.filterAboveThreshold(readings, CONSUMPTION_THRESHOLD);
        sb.append(String.format(Locale.ROOT, "Записів понад поріг (%.2f): %d%n", CONSUMPTION_THRESHOLD, aboveThreshold.size()));

        Map<String, Double> byMeter = UtilityReport.sumConsumptionByMeter(readings);
        String groupedStr = byMeter.entrySet().stream()
                .map(e -> String.format(Locale.ROOT, "%s=%.2f", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
        sb.append(String.format(Locale.ROOT, "Сума за лічильниками: %s%n", groupedStr));

        List<UtilityReading> top5 = UtilityReport.topReadingsByConsumption(readings, TOP_LIMIT);
        String topStr = top5.stream()
                .map(r -> String.format(Locale.ROOT, "%s (%.2f від %s)", r.getName(), r.getConsumption(), r.getDate()))
                .collect(Collectors.joining("; "));
        sb.append(String.format(Locale.ROOT, "Топ-%d за споживанням: [%s]%n", TOP_LIMIT, topStr));

        Optional<UtilityReading> waterSearch = UtilityReport.findFirstByMeter(readings, "Вода");
        sb.append(String.format(Locale.ROOT, "Пошук 'Вода': %s%n",
                waterSearch.map(UtilityReading::toString).orElse("Не знайдено")));

        return sb.toString();
    }
}