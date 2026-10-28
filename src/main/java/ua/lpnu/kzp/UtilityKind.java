package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Категорія комунальної послуги.
 */
public enum UtilityKind {
    ELECTRICITY("електроенергія"),
    WATER("водопостачання");

    private final String label;

    UtilityKind(String label) {
        this.label = Objects.requireNonNull(label, "Мітка не може бути null");
    }

    /**
     * Повертає текстову мітку категорії для звітів.
     *
     * @return мітка категорії
     */
    public String label() {
        return label;
    }
}