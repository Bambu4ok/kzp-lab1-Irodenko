"""Зчитування CSV та перетворення рядків на об'єкти моделі (Варіант 9)."""

from __future__ import annotations

import csv
import math
from datetime import date
from pathlib import Path

from kzp_lab07.errors import DataError
from kzp_lab07.models import ElectricityReading, Reading, WaterReading

EXPECTED_FIELDS: tuple[str, ...] = ("meter", "date", "value", "tariff")


def _parse_non_negative_float(raw: str | None, field_name: str, row_num: int) -> float:
    """Перетворює рядок на невід'ємне скінченне дійсне число."""
    if raw is None or not raw.strip():
        raise DataError(f"Рядок {row_num}: порожнє значення у полі '{field_name}'.")
    try:
        number = float(raw.strip())
    except ValueError as exc:
        raise DataError(
            f"Рядок {row_num}: некоректне число '{raw.strip()}' у полі '{field_name}'."
        ) from exc

    if not math.isfinite(number) or number < 0.0:
        raise DataError(
            f"Рядок {row_num}: поле '{field_name}' має бути невід'ємним скінченним числом."
        )
    return number


def build_reading(row: dict[str, str], row_num: int) -> Reading:
    """Створює відповідний підтип Reading за назвою лічильника з CSV-рядка."""
    meter = str(row["meter"]).strip()
    if not meter:
        raise DataError(f"Рядок {row_num}: назва лічильника не може бути порожньою.")

    raw_date = str(row["date"]).strip()
    try:
        parsed_date = date.fromisoformat(raw_date)
    except ValueError as exc:
        raise DataError(
            f"Рядок {row_num}: некоректна дата '{raw_date}' (очікується YYYY-MM-DD)."
        ) from exc

    value = _parse_non_negative_float(row["value"], "value", row_num)
    tariff = _parse_non_negative_float(row["tariff"], "tariff", row_num)

    try:
        if "вода" in meter.lower():
            return WaterReading(meter=meter, reading_date=parsed_date, value=value, tariff=tariff)
        return ElectricityReading(
            meter=meter,
            reading_date=parsed_date,
            value=value,
            tariff=tariff,
        )
    except ValueError as exc:
        raise DataError(f"Рядок {row_num}: {exc}") from exc


def load_readings(csv_path: str | Path) -> list[Reading]:
    """Зчитує CSV у UTF-8 та повертає список типізованих об'єктів Reading."""
    path = Path(csv_path)
    if not path.exists() or not path.is_file():
        raise DataError(f"Вхідний файл не знайдено: {path}")

    readings: list[Reading] = []
    try:
        with path.open(mode="r", encoding="utf-8", newline="") as handle:
            reader = csv.DictReader(handle)
            if reader.fieldnames is None:
                return []

            normalized_headers = tuple(name.strip() for name in reader.fieldnames)
            if normalized_headers != EXPECTED_FIELDS:
                raise DataError(
                    f"Некоректний заголовок CSV: очікувалося {EXPECTED_FIELDS}, "
                    f"отримано {normalized_headers}."
                )

            for row_num, row in enumerate(reader, start=2):
                if None in row or any(row[field] is None for field in EXPECTED_FIELDS):
                    raise DataError(f"Рядок {row_num}: невідповідна кількість стовпців.")
                readings.append(build_reading(row, row_num))
    except UnicodeDecodeError as exc:
        raise DataError(f"Помилка кодування UTF-8 у файлі {path}: {exc}") from exc

    return readings
