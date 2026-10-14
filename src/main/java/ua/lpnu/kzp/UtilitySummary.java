package ua.lpnu.kzp;

public record UtilitySummary(
        int validCount,
        double totalConsumption,
        double totalCost,
        double maxConsumption
) {
    public UtilitySummary {
        if (validCount < 0 || totalConsumption < 0 || totalCost < 0 || maxConsumption < 0) {
            throw new IllegalArgumentException("Показники не можуть бути від'ємними");
        }
    }
}