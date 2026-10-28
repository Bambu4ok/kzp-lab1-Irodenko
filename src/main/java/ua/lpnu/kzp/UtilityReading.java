package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Базовий абстрактний клас комунального показника.
 */
public abstract class UtilityReading {
    private final String name;
    private final double consumption;
    private final String date;
    private final double tariff;
    private final UtilityKind kind;

    /**
     * Створює об'єкт комунального показника з валідацією спільних полів.
     *
     * @param name        назва лічильника або послуги
     * @param consumption кількість спожитого ресурсу
     * @param date        дата зняття показника
     * @param tariff      тариф за одиницю
     * @param kind        категорія послуги
     */
    protected UtilityReading(String name, double consumption, String date, double tariff, UtilityKind kind) {
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
        this.kind = Objects.requireNonNull(kind, "Категорія не може бути null");
        this.consumption = consumption;
        this.tariff = tariff;
    }

    /**
     * Фабричний метод для розбору CSV-рядка та створення конкретного підтипу.
     *
     * @param line рядок у форматі CSV
     * @return екземпляр підтипу UtilityReading
     */
    public static UtilityReading fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        String[] fields = line.split(";", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException(
                String.format(Locale.ROOT, "очікується 4 поля, отримано %d", fields.length)
            );
        }
        try {
            String name = fields[0].trim();
            double consumption = Double.parseDouble(fields[1].trim());
            String date = fields[2].trim();
            double tariff = Double.parseDouble(fields[3].trim());

            if (name.equalsIgnoreCase("Вода") || name.toLowerCase().contains("вода")) {
                return new WaterReading(name, consumption, date, tariff);
            }
            return new ElectricityReading(name, consumption, date, tariff);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("помилка формату числа", e);
        }
    }

    public String getName() {
        return name;
    }

    public double getConsumption() {
        return consumption;
    }

    public String getDate() {
        return date;
    }

    public double getTariff() {
        return tariff;
    }

    public UtilityKind getKind() {
        return kind;
    }

    /**
     * Поліморфний метод обчислення вартості споживання.
     *
     * @return вартість послуги
     */
    public abstract double calculateCost();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        UtilityReading that = (UtilityReading) other;
        return Double.compare(that.consumption, consumption) == 0
                && Double.compare(that.tariff, tariff) == 0
                && name.equals(that.name)
                && date.equals(that.date)
                && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), name, consumption, date, tariff, kind);
    }

    @Override
    public String toString() {
        return String.format(
            Locale.ROOT,
            "%s [%s] (%s): спожито %.2f, тариф %.2f",
            name, kind.label(), date, consumption, tariff
        );
    }
}