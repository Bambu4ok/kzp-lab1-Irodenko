package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

public final class UtilityReading {
    private final String name;
    private final double consumption;
    private final String date;
    private final double tariff;

    public UtilityReading(String name, double consumption, String date, double tariff) {
        this.name = Objects.requireNonNull(name, "Назва не може бути null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("порожня назва лічильника або дата");
        }
        this.date = Objects.requireNonNull(date, "Дата не може бути null");
        if (date.isBlank()) {
            throw new IllegalArgumentException("порожня назва лічильника або дата");
        }
        if (consumption < 0 || tariff < 0) {
            throw new IllegalArgumentException("значення споживання або тарифу від'ємне");
        }
        this.consumption = consumption;
        this.tariff = tariff;
    }

    public static UtilityReading fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        String[] fields = line.split(";", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "очікується 4 поля, отримано %d", fields.length));
        }

        try {
            String name = fields[0].trim();
            double consumption = Double.parseDouble(fields[1].trim());
            String date = fields[2].trim();
            double tariff = Double.parseDouble(fields[3].trim());

            return new UtilityReading(name, consumption, date, tariff);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("помилка формату числа", e);
        }
    }

    public String getName() { return name; }
    public double getConsumption() { return consumption; }
    public String getDate() { return date; }
    public double getTariff() { return tariff; }

    public double calculateCost() {
        return consumption * tariff;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s): спожито %.2f, тариф %.2f", name, date, consumption, tariff);
    }
}