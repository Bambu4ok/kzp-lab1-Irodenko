## Лабораторна робота № 6 — Обробка даних засобами Python (Варіант 9)

### Предметна область та формат CSV
Предметна область: **Комунальні показники**.  
Вхідний файл `data/input.csv` (кодування UTF-8) містить поля `meter,date,value,tariff`:
- `meter` — назва лічильника (підтримується екранування ком у лапках, наприклад `"Вода, холодна"`).
- `date` — дата у форматі ISO `YYYY-MM-DD`.
- `value`, `tariff` — невід'ємні дійсні числа (`float`).

### Структура модулів (`src/kzp_lab06/`)
- `errors.py` — клас винятку `DataError(ValueError)`.
- `loader.py` — читання CSV через `csv.DictReader` (`encoding="utf-8"`, `newline=""`), перевірка заголовка та типізована валідація.
- `analysis.py` — відбір записів (`value > threshold`), множина унікальних лічильників, словник сум за лічильниками, обчислення загальної вартості й максимуму, функція `summarize_metrics` з `*args` та `**kwargs`.
- `reporting.py` — детерміноване форматування звіту та запис у UTF-8 через `with` і `Path.open`.
- `cli.py` — інтерфейс командного рядка (`kzp-lab06`) з кодами завершення `0` (успіх) і `2` (помилка вхідних даних).

### Команди встановлення, перевірки та запуску
```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -e ".[dev]"
.\.venv\Scripts\python.exe -m pytest
.\.venv\Scripts\ruff.exe check
.\.venv\Scripts\ruff.exe format --check
.\.venv\Scripts\python.exe -m kzp_lab06.cli data\input.csv reports\summary.txt
.\.venv\Scripts\python.exe -m build