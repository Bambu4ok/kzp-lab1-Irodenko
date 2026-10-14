package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilityReadingTest {

    @Test
    void fromCsvCreatesValidEntity() {
        UtilityReading reading = UtilityReading.fromCsv("Електроенергія; 100.0; 2026-09-01; 4.32");
        assertEquals("Електроенергія", reading.getName());
        assertEquals(100.0, reading.getConsumption(), 0.0001);
        assertEquals("2026-09-01", reading.getDate());
        assertEquals(4.32, reading.getTariff(), 0.0001);
        assertEquals(432.0, reading.calculateCost(), 0.0001);
    }

    @Test
    void constructorRejectsNegativeConsumption() {
        assertThrows(IllegalArgumentException.class,
                () -> new UtilityReading("Газ", -50.0, "2026-09-01", 7.99));
    }

    @Test
    void constructorRejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new UtilityReading("   ", 100.0, "2026-09-01", 4.32));
    }

    @Test
    void fromCsvRejectsInvalidNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> UtilityReading.fromCsv("Вода; текст; 2026-09-01; 30.0"));
    }

    @Test
    void fromCsvRejectsWrongFieldCount() {
        assertThrows(IllegalArgumentException.class,
                () -> UtilityReading.fromCsv("Вода; 100.0; 2026-09-01"));
    }

    @Test
    void recordUsesValueEquality() {
        UtilitySummary first = new UtilitySummary(2, 150.0, 800.0, 100.0);
        UtilitySummary second = new UtilitySummary(2, 150.0, 800.0, 100.0);
        assertEquals(first, second);
    }
}