package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    @TempDir
    Path tempDir;

    // Допоміжний довільний клас для доведення універсальності CsvExporter
    private static final class SimpleTag {
        @CsvColumn("тег")
        private final String tag;

        private SimpleTag(String tag) {
            this.tag = tag;
        }
    }

    @Test
    void repositoryIsGenericAndReturnsImmutableSnapshot() {
        Repository<UtilityReadingRecord> repo = new Repository<>();
        repo.add(new UtilityReadingRecord("Електроенергія", LocalDate.of(2026, 9, 1), 250.0, 4.32));
        repo.add(new UtilityReadingRecord("Вода", LocalDate.of(2026, 9, 2), 15.0, 30.0));

        List<UtilityReadingRecord> snapshot = repo.all();
        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(new UtilityReadingRecord("Газ", LocalDate.of(2026, 9, 3), 50.0, 7.99)));

        List<UtilityReadingRecord> found = repo.find(r -> r.getMeter().equals("Вода"));
        assertEquals(1, found.size());
        assertEquals(15.0, found.get(0).getValue(), 0.0001);
    }

    @Test
    void exportEscapesSpecialCharsAndRoundTripRestoresAllRecords() throws Exception {
        // 5 записів, включаючи запис із комою, лапками та перенесенням рядка у назві лічильника
        List<UtilityReadingRecord> original = List.of(
                new UtilityReadingRecord("Електроенергія", LocalDate.of(2026, 9, 1), 250.0, 4.32),
                new UtilityReadingRecord("Вода, \"Холодна\"\nквартира 12", LocalDate.of(2026, 9, 2), 15.5, 30.0),
                new UtilityReadingRecord("Газ", LocalDate.of(2026, 9, 3), 120.0, 7.99),
                new UtilityReadingRecord("Опалення", LocalDate.of(2026, 9, 4), 40.0, 45.0),
                new UtilityReadingRecord("Нічний тариф", LocalDate.of(2026, 9, 5), 95.0, 2.16)
        );

        Path csvFile = tempDir.resolve("utility_roundtrip.csv");
        CsvExporter.write(csvFile, UtilityReadingRecord.class, original);

        String csvContent = Files.readString(csvFile, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(csvContent);

        // 1 заголовок + 5 записів = 6 логічних записів (попри багаторядкове поле)
        assertEquals(6, rows.size());
        // Перевірка детермінованого заголовка (date, meter, tariff, value)
        assertEquals("дата,лічильник,тариф,значення", String.join(",", rows.get(0)));
        assertTrue(csvContent.contains("\"Вода, \"\"Холодна\"\"\nквартира 12\""));

        List<UtilityReadingRecord> restored = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            restored.add(UtilityReadingRecord.fromFields(rows.get(i)));
        }

        assertEquals(original, restored);
    }

    @Test
    void handlesEmptyListAndArbitraryAnnotatedClass() throws Exception {
        Path emptyCsv = tempDir.resolve("empty.csv");
        CsvExporter.write(emptyCsv, UtilityReadingRecord.class, List.of());

        String content = Files.readString(emptyCsv, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(content);
        assertEquals(1, rows.size());
        assertEquals(List.of("дата", "лічильник", "тариф", "значення"), rows.get(0));

        // Перевірка універсальності експортера на іншому класі
        Path tagCsv = tempDir.resolve("tags.csv");
        CsvExporter.write(tagCsv, SimpleTag.class, List.of(new SimpleTag("Лічильник №1")));
        List<List<String>> tagRows = CsvParser.parseDocument(Files.readString(tagCsv, StandardCharsets.UTF_8));
        assertEquals(List.of("тег"), tagRows.get(0));
        assertEquals(List.of("Лічильник №1"), tagRows.get(1));
    }

    @Test
    void rejectsMalformedCsvAndWrapsCause() {
        // Неправильна кількість полів
        assertThrows(DataStorageException.class,
                () -> UtilityReadingRecord.fromCsv("2026-09-01,Вода,30.0"));

        // Некоректне число з перевіркою cause
        DataStorageException numberEx = assertThrows(DataStorageException.class,
                () -> UtilityReadingRecord.fromCsv("2026-09-01,Вода,30.0,не_число"));
        assertNotNull(numberEx.getCause());

        // Незакриті лапки
        assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("2026-09-01,\"Вода,30.0,15.0"));
    }

    @Test
    void wrapsIoExceptionWithCauseWhenWritingToDirectory() throws Exception {
        Path dirAsFile = tempDir.resolve("folder_not_file");
        Files.createDirectory(dirAsFile);

        DataStorageException ex = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(dirAsFile, UtilityReadingRecord.class, List.of()));
        assertInstanceOf(IOException.class, ex.getCause());
    }
}