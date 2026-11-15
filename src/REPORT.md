# Звіт до лабораторної роботи № 4
**з дисципліни «Кросплатформні засоби програмування»**

**Тема:** Потокова обробка даних у Java-проєкті  
**Виконав:** студент групи КІ-301 Іроденко Р. І.  
**Прийняв:** Іванов Ю. С.  
**Варіант:** 9 (Комунальні показники)  
**ОС:** Windows 11  

---

## 1. Мета роботи
Замінити основні циклові обчислення в наявній предметній моделі на декларативні запити Stream API (відбір, перетворення, групування, статистика, складене сортування та `Optional`-пошук), зберігши зовнішню поведінку програми та сумісність із попередніми тестами.

---

## 2. Порівняння циклової та потокової реалізацій

| Завдання / Ознака | ЛР3 (циклова реалізація) | ЛР4 (Stream API) |
| :--- | :--- | :--- |
| **Зведена статистика** | Накопичення `sum`, `max`, `count` у циклі `for` | `UtilityReport.consumptionStatistics()` через `Collectors.summarizingDouble` |
| **Загальна вартість** | Накопичення через цикл | `readings.stream().mapToDouble(UtilityReading::calculateCost).sum()` |
| **Відбір за порогом** | Ручний перебір з `if (consumption > 100)` | `UtilityReport.filterAboveThreshold()` через `.filter(...)` |
| **Перетворення** | Створення нового списку в циклі | `UtilityReport.mapToConsumptions()` через `.map(UtilityReading::getConsumption)` |
| **Групування** | Ручне заповнення `Map` | `UtilityReport.sumConsumptionByMeter()` через `Collectors.groupingBy(..., TreeMap::new, summingDouble(...))` |
| **Top-5 записів** | Копіювання списку, сортування та обрізання | `UtilityReport.topReadingsByConsumption()` через складений `Comparator` + `.sorted().limit(5)` |
| **Пошук запису** | Повернення `null` або прапорець у циклі | `UtilityReport.findFirstByMeter()` повертає `Optional<UtilityReading>` |

---

## 3. Опис конвеєрів та компаратора
- **Константи:** поріг відбору `CONSUMPTION_THRESHOLD = 100.0`, обмеження вибірки `TOP_LIMIT = 5`.
- **Складений компаратор:** `Comparator.comparingDouble(UtilityReading::getConsumption).reversed().thenComparing(UtilityReading::getDate)` — спочатку впорядковує записи за спаданням споживання, а за однакового споживання — за датою за зростанням.
- **Порожній вхід:** для порожньої колекції `DoubleSummaryStatistics` повертає `count = 0`, `sum = 0.0` (у звіті `max` приводиться до `0.0`), а пошук повертає `Optional.empty()`.

---

## 4. Результати тестування
Усі 14 модульних тестів (7 збережених із ЛР2–ЛР3 + 7 нових у `UtilityReportTest`) успішно проходять (`BUILD SUCCESS`):
- `filtersStrictlyAboveThreshold` — перевірка строгого відбору понад поріг (межа `100.0` не потрапляє).
- `mapsToConsumptionValues` — перевірка перетворення об'єктів у значення споживання.
- `groupsAndSumsByMeterName` — перевірка групування та сумування дубльованих лічильників.
- `calculatesSummaryStatisticsMatchingLegacyLoop` — доведення еквівалентності Stream API та класичного циклу.
- `top5SortsByConsumptionDescThenDateAscAndTruncates` — перевірка обох критеріїв компаратора, усікання 6-го запису, `limit(0)` та від'ємного ліміту.
- `optionalSearchFindsFirstAndHandlesMissing` — повернення першого збігу при дублікатах та `Optional.empty()` при відсутності.
- `handlesEmptyInputAndDoesNotMutateOriginalList` — перевірка незмінності вхідної колекції та порожнього списку.

---

## 5. Висновки
У ході лабораторної роботи № 4 циклову обробку комунальних показників перенесено до декларативних запитів Stream API у класі `UtilityReport`. Усі 14 тестів успішно проходять, а зовнішній формат звіту повністю сумісний із попередніми лабораторними роботами.