package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне параметризоване сховище об'єктів у пам'яті.
 *
 * @param <T> тип елементів сховища
 */
public final class Repository<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Додає непорожній запис до сховища.
     *
     * @param item об'єкт для збереження
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null"));
    }

    /**
     * Повертає незмінний знімок усіх записів у сховищі.
     *
     * @return незмінний список елементів
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає записи, що задовольняють задану умову.
     *
     * @param condition предикат фільтрації
     * @return список знайдених записів
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "Умова не може бути null");
        return items.stream()
                .filter(condition)
                .toList();
    }
}