package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void testProcessDataWithValidAndInvalidLines() {
        List<String> inputLines = List.of(
            "Електроенергія; 100.0; 2026-09-01; 4.0",
            "Газ; 50.0; 2026-09-02; 8.0",
            "Вода; -10.0; 2026-09-03; 30.0",
            "Світло; текст; 2026-09-04; 4.0"
        );

        String result = Main.processData(inputLines);

        assertTrue(result.contains("1. Кількість коректних записів: 2"));
        assertTrue(result.contains("2. Сумарне споживання: 150.00"));
        assertTrue(result.contains("3. Загальна вартість: 800.00 грн"));
        assertTrue(result.contains("4. Найбільше споживання: 100.00"));
        assertTrue(result.contains("Помилок/пропущено рядків: 2"));
    }
}