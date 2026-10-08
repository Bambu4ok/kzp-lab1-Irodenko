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
            System.out.println("Версія: 1.0.0");
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
        List<String> errors = new ArrayList<>();
        int validCount = 0;
        double totalConsumption = 0.0;
        double totalCost = 0.0;
        double maxConsumption = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] fields = line.split(";", -1);
            if (fields.length != 4) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: очікується 4 поля, отримано %d", i + 1, fields.length));
                continue;
            }

            if (fields[0].isBlank() || fields[2].isBlank()) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: порожня назва лічильника або дата", i + 1));
                continue;
            }

            try {
                double value = Double.parseDouble(fields[1].trim());
                double tariff = Double.parseDouble(fields[3].trim());

                if (value < 0 || tariff < 0) {
                    errors.add(String.format(Locale.ROOT, "Рядок %d: значення споживання або тарифу від'ємне", i + 1));
                    continue;
                }

                validCount++;
                totalConsumption += value;
                totalCost += (value * tariff);
                maxConsumption = Math.max(maxConsumption, value);

            } catch (NumberFormatException e) {
                errors.add(String.format(Locale.ROOT, "Рядок %d: помилка формату числа", i + 1));
            }
        }

        if (validCount == 0) {
            maxConsumption = 0.0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ЗВІТ ПО КОМУНАЛЬНИХ ПОКАЗНИКАХ ===\n");
        sb.append(String.format(Locale.ROOT, "1. Кількість коректних записів: %d%n", validCount));
        sb.append(String.format(Locale.ROOT, "2. Сумарне споживання: %.2f%n", totalConsumption));
        sb.append(String.format(Locale.ROOT, "3. Загальна вартість: %.2f грн%n", totalCost));
        sb.append(String.format(Locale.ROOT, "4. Найбільше споживання: %.2f%n", maxConsumption));
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