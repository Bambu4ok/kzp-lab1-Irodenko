package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

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
        assertInstanceOf(ElectricityReading.class, reading);
    }

    @Test
    void fromCsvCreatesWaterReading() {
        UtilityReading reading = UtilityReading.fromCsv("Вода; 50.0; 2026-09-01; 30.0");
        assertInstanceOf(WaterReading.class, reading);
        assertEquals(UtilityKind.WATER, reading.getKind());
    }

    @Test
    void constructorRejectsNegativeConsumption() {
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricityReading("Газ", -50.0, "2026-09-01", 7.99));
    }

    @Test
    void constructorRejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricityReading("   ", 100.0, "2026-09-01", 4.32));
    }

    @Test
    void equalObjectsHaveSameHashCodeAndOneSetEntry() {
        UtilityReading r1 = new ElectricityReading("Світло", 100.0, "2026-09-01", 4.0);
        UtilityReading r2 = new ElectricityReading("Світло", 100.0, "2026-09-01", 4.0);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        Set<UtilityReading> set = new HashSet<>();
        set.add(r1);
        set.add(r2);

        assertEquals(1, set.size());
    }

    @Test
    void differentSubtypesAreNotEqual() {
        UtilityReading electricity = new ElectricityReading("Вода", 50.0, "2026-09-01", 10.0);
        UtilityReading water = new WaterReading("Вода", 50.0, "2026-09-01", 10.0);

        assertNotEquals(electricity, water);
    }
}