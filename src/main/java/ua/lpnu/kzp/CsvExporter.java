package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Універсальний рефлексивний експортер анотованих полів класу у UTF-8 CSV.
 */
public final class CsvExporter {

    private CsvExporter() {
    }

    /**
     * Записує анотовані поля записів у CSV-файл із кодуванням UTF-8.
     *
     * @param <T>     тип записів
     * @param path    шлях до файлу
     * @param type    клас об'єктів
     * @param records список записів для експорту
     * @throws DataStorageException якщо клас не має анотованих полів, немає доступу або сталася помилка вводу-виводу
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records) throws DataStorageException {
        Objects.requireNonNull(path, "Шлях не може бути null");
        Objects.requireNonNull(type, "Тип не може бути null");
        Objects.requireNonNull(records, "Записи не можуть бути null");

        List<Field> fields = annotatedFields(type);
        if (fields.isEmpty()) {
            throw new DataStorageException("Клас " + type.getSimpleName() + " не має анотованих CSV-полів");
        }

        String header = fields.stream()
                .map(field -> escape(field.getAnnotation(CsvColumn.class).value()))
                .collect(Collectors.joining(","));

        StringBuilder csv = new StringBuilder(header)
                .append(System.lineSeparator());

        for (T record : records) {
            if (record == null) {
                throw new DataStorageException("Список містить null-запис");
            }
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    values.add(escape(field.get(record)));
                } catch (IllegalAccessException exception) {
                    throw new DataStorageException(
                            "Не вдалося прочитати поле " + field.getName(), exception);
                }
            }
            csv.append(String.join(",", values))
                    .append(System.lineSeparator());
        }

        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося записати CSV: " + path, exception);
        }
    }

    private static <T> List<Field> annotatedFields(Class<T> type) throws DataStorageException {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            fields.addAll(Arrays.stream(current.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(CsvColumn.class))
                    .toList());
        }
        fields.sort(Comparator.comparing(Field::getName));
        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                throw new DataStorageException("Немає доступу до поля " + field.getName());
            }
        }
        return List.copyOf(fields);
    }

    private static String escape(Object value) {
        String text = Objects.toString(value, "");
        boolean quoted = text.contains(",")
                || text.contains("\"")
                || text.contains("\r")
                || text.contains("\n");
        return quoted
                ? "\"" + text.replace("\"", "\"\"") + "\""
                : text;
    }
}