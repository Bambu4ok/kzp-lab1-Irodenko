package ua.lpnu.kzp;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * Плоский запис комунального показника для рефлексивного CSV-експорту та round-trip (Варіант 9).
 * Порядок полів за Java-іменами: date, meter, tariff, value.
 */
public final class UtilityReadingRecord {

    @CsvColumn("лічильник")
    private final String meter;

    @CsvColumn("дата")
    private final LocalDate date;

    @CsvColumn("значення")
    private final double value;

    @CsvColumn("тариф")
    private final double tariff;

    /**
     * Створює валідований запис комунального показника.
     *
     * @param meter  назва лічильника
     * @param date   дата зняття показника
     * @param value  значення споживання
     * @param tariff тариф за одиницю
     */
    public UtilityReadingRecord(String meter, LocalDate date, double value, double tariff) {
        if (meter == null || meter.isBlank()) {
            throw new IllegalArgumentException("Назва лічильника не може бути порожньою");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата не може бути null");
        }
        if (value < 0 || tariff < 0 || !Double.isFinite(value) || !Double.isFinite(tariff)) {
            throw new IllegalArgumentException("Значення та тариф мають бути невід'ємними числами");
        }
        this.meter = meter;
        this.date = date;
        this.value = value;
        this.tariff = tariff;
    }

    /**
     * Відновлює запис із одного CSV-рядка.
     *
     * @param line рядок у порядку стовпців експорту: дата,лічильник,тариф,значення
     * @return відновлений об'єкт UtilityReadingRecord
     * @throws DataStorageException якщо формат рядка або типів некоректний
     */
    public static UtilityReadingRecord fromCsv(String line) throws DataStorageException {
        List<String> fields = CsvParser.parse(line);
        return fromFields(fields);
    }

    /**
     * Відновлює запис із розібраного списку полів (порядок: date, meter, tariff, value).
     *
     * @param fields список із 4 значень полів
     * @return об'єкт UtilityReadingRecord
     * @throws DataStorageException якщо кількість полів не дорівнює 4 або не вдалося розпізнати дату/число
     */
    public static UtilityReadingRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields.size() != 4) {
            throw new DataStorageException("Очікується 4 поля CSV, отримано: " + fields.size());
        }
        try {
            LocalDate parsedDate = LocalDate.parse(fields.get(0));
            String parsedMeter = fields.get(1);
            double parsedTariff = Double.parseDouble(fields.get(2));
            double parsedValue = Double.parseDouble(fields.get(3));
            return new UtilityReadingRecord(parsedMeter, parsedDate, parsedValue, parsedTariff);
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new DataStorageException("Некоректні дані в рядку комунального показника", exception);
        }
    }

    public String getMeter() {
        return meter;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public double getTariff() {
        return tariff;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UtilityReadingRecord that)) {
            return false;
        }
        return Double.compare(that.value, value) == 0
                && Double.compare(that.tariff, tariff) == 0
                && meter.equals(that.meter)
                && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meter, date, value, tariff);
    }

    @Override
    public String toString() {
        return "UtilityReadingRecord{" +
                "meter='" + meter + '\'' +
                ", date=" + date +
                ", value=" + value +
                ", tariff=" + tariff +
                '}';
    }
}