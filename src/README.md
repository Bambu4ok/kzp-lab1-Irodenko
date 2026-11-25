[System.IO.File]::WriteAllText("$PWD\README.md", @"
# Кросплатформні засоби програмування — Python-модулі (Варіант 9)

Репозиторій містить Python-пакети для обробки комунальних показників із CSV-файлів:
- **kzp_lab06** (ЛР № 6): процедурна та функціональна обробка CSV із використанням словників, множин і *args/**kwargs.
- **kzp_lab07** (ЛР № 7): об'єктно-орієнтована модель із базовим класом `Reading`, властивостями `@property` і `setter`-валідацією, підтипами `ElectricityReading` та `WaterReading`, протоколом `BillableReading` і незмінним підсумком `@dataclass(frozen=True) UtilitySummary`.

## Швидкий старт

``````powershell
python -m pip install -e ".[dev]"
python -m pytest
ruff check
ruff format --check
python -m kzp_lab07.cli data/input.csv reports/summary_lab07.txt