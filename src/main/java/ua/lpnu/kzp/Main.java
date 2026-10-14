package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--help".equals(args[0])) {
            System.out.println("Використання: java -jar lab01.jar [--help] [--version] [--input <файл>] [--output <файл>]");
            return;
        }

        if (args.length > 0 && "--version".equals(args[0])) {
            System.out.println("Версія: 2.0.0");
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
                UtilityReading reading = UtilityReading.fromCsv(line);
                readings.add(reading);
            } catch (IllegalArgumentException e) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: %s", i + 1, e.getMessage()));
            }
        }

        int validCount = readings.size();
        double totalConsumption = readings.stream().mapToDouble(UtilityReading::getConsumption).sum();
        double totalCost = readings.stream().mapToDouble(UtilityReading::calculateCost).sum();
        double maxConsumption = readings.stream()
                .mapToDouble(UtilityReading::getConsumption)
                .max()
                .orElse(0.0);

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

        return sb.toString();
    }
}