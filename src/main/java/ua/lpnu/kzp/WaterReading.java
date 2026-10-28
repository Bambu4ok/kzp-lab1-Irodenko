package ua.lpnu.kzp;

/**
 * Підтип для показників водопостачання.
 */
public final class WaterReading extends UtilityReading {

    public WaterReading(String name, double consumption, String date, double tariff) {
        super(name, consumption, date, tariff, UtilityKind.WATER);
    }

    @Override
    public double calculateCost() {
        return getConsumption() * getTariff();
    }
}