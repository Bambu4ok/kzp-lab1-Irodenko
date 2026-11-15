# КЗП: Лабораторна робота № 5 — Параметризоване сховище, винятки й CSV-експорт (Варіант 9)

Консольний Java-застосунок для обліку, потокового аналізу та рефлексивного CSV-експорту комунальних показників (`UtilityReadingRecord`).

## Функціональність (ЛР1–ЛР5)
- Поліморфна модель комунальних показників (`UtilityReading`, `ElectricityReading`, `WaterReading`).
- Потокові запити через Stream API (`UtilityReport`: `filter`, `map`, `groupingBy`, `summarizingDouble`, `sorted + limit`, `Optional`).
- Типобезпечне параметризоване сховище `Repository<T>` із незмінним знімком `all()` та пошуком за предикатом `find(Predicate<? super T>)`[cite: 9].
- Власна анотація `@CsvColumn` (`RUNTIME`, `FIELD`) та універсальний рефлексивний експортер `CsvExporter` у UTF-8 із детермінованим порядком стовпців (`дата,лічильник,тариф,значення`)[cite: 9, 10, 11].
- Власний виняток `DataStorageException` зі збереженням першопричини (`cause`) та документний CSV-парсер `CsvParser` із підтримкою екранування ком, лапок і перенесень рядка[cite: 8, 11].

## Збірка та запуск
```bash
.\mvnw.cmd test
.\mvnw.cmd package
java -jar target/kzp-lab1-1.0-SNAPSHOT-jar-with-dependencies.jar