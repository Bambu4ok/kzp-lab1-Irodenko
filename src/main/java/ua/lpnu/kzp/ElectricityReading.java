package ua.lpnu.kzp;

/**
 * Підтип для показників електроенергії.
 */
public final class ElectricityReading extends UtilityReading {

    public ElectricityReading(String name, double consumption, String date, double tariff) {
        super(name, consumption, date, tariff, UtilityKind.ELECTRICITY);
    }

    @Override
    public double calculateCost() {
        return getConsumption() * getTariff();
    }
}