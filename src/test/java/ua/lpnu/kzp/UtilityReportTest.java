package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtilityReportTest {

    private final List<UtilityReading> sampleReadings = List.of(
            new ElectricityReading("Електроенергія", 150.0, "2026-09-05", 4.32),
            new WaterReading("Вода", 45.0, "2026-09-02", 30.0),
            new ElectricityReading("Електроенергія", 150.0, "2026-09-01", 4.32),
            new ElectricityReading("Газ", 100.0, "2026-09-03", 7.99),
            new WaterReading("Вода гаряча", 220.0, "2026-09-04", 80.0),
            new ElectricityReading("Нічний тариф", 80.0, "2026-09-06", 2.16)
    );

    @Test
    void filtersStrictlyAboveThreshold() {
        List<UtilityReading> filtered = UtilityReport.filterAboveThreshold(sampleReadings, 100.0);
        assertEquals(3, filtered.size());
        assertTrue(filtered.stream().allMatch(r -> r.getConsumption() > 100.0));
    }

    @Test
    void mapsToConsumptionValues() {
        List<Double> consumptions = UtilityReport.mapToConsumptions(sampleReadings);
        assertEquals(List.of(150.0, 45.0, 150.0, 100.0, 220.0, 80.0), consumptions);
    }

    @Test
    void groupsAndSumsByMeterName() {
        Map<String, Double> grouped = UtilityReport.sumConsumptionByMeter(sampleReadings);
        assertEquals(300.0, grouped.get("Електроенергія"), 0.0001);
        assertEquals(45.0, grouped.get("Вода"), 0.0001);
        assertEquals(220.0, grouped.get("Вода гаряча"), 0.0001);
        assertEquals(100.0, grouped.get("Газ"), 0.0001);
        assertEquals(80.0, grouped.get("Нічний тариф"), 0.0001);
    }

    @Test
    void calculatesSummaryStatisticsMatchingLegacyLoop() {
        DoubleSummaryStatistics stats = UtilityReport.consumptionStatistics(sampleReadings);

        double legacySum = 0.0;
        double legacyMax = Double.NEGATIVE_INFINITY;
        for (UtilityReading r : sampleReadings) {
            legacySum += r.getConsumption();
            if (r.getConsumption() > legacyMax) {
                legacyMax = r.getConsumption();
            }
        }

        assertEquals(6, stats.getCount());
        assertEquals(legacySum, stats.getSum(), 0.0001);
        assertEquals(legacyMax, stats.getMax(), 0.0001);
        assertEquals(45.0, stats.getMin(), 0.0001);
    }

    @Test
    void top5SortsByConsumptionDescThenDateAscAndTruncates() {
        List<UtilityReading> top5 = UtilityReport.topReadingsByConsumption(sampleReadings, 5);
        assertEquals(5, top5.size());

        assertEquals(220.0, top5.get(0).getConsumption(), 0.0001);
        assertEquals(150.0, top5.get(1).getConsumption(), 0.0001);
        assertEquals("2026-09-01", top5.get(1).getDate());
        assertEquals(150.0, top5.get(2).getConsumption(), 0.0001);
        assertEquals("2026-09-05", top5.get(2).getDate());
        assertEquals(100.0, top5.get(3).getConsumption(), 0.0001);
        assertEquals(80.0, top5.get(4).getConsumption(), 0.0001);

        assertTrue(UtilityReport.topReadingsByConsumption(sampleReadings, 0).isEmpty());
        assertEquals(6, UtilityReport.topReadingsByConsumption(sampleReadings, 20).size());
        assertThrows(IllegalArgumentException.class, () -> UtilityReport.topReadingsByConsumption(sampleReadings, -1));
    }

    @Test
    void optionalSearchFindsFirstAndHandlesMissing() {
        Optional<UtilityReading> found = UtilityReport.findFirstByMeter(sampleReadings, "Електроенергія");
        assertTrue(found.isPresent());
        assertEquals("2026-09-05", found.get().getDate());

        Optional<UtilityReading> missing = UtilityReport.findFirstByMeter(sampleReadings, "Невідомий лічильник");
        assertTrue(missing.isEmpty());
    }

    @Test
    void handlesEmptyInputAndDoesNotMutateOriginalList() {
        List<UtilityReading> snapshot = List.copyOf(sampleReadings);

        UtilityReport.filterAboveThreshold(sampleReadings, 50.0);
        UtilityReport.mapToConsumptions(sampleReadings);
        UtilityReport.sumConsumptionByMeter(sampleReadings);
        UtilityReport.consumptionStatistics(sampleReadings);
        UtilityReport.topReadingsByConsumption(sampleReadings, 5);
        UtilityReport.findFirstByMeter(sampleReadings, "Вода");

        assertEquals(snapshot, sampleReadings);

        assertTrue(UtilityReport.filterAboveThreshold(List.of(), 50.0).isEmpty());
        assertEquals(0, UtilityReport.consumptionStatistics(List.of()).getCount());
        assertTrue(UtilityReport.findFirstByMeter(List.of(), "Вода").isEmpty());
    }
}