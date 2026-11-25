"""Завантаження, парсинг та типізована валідація CSV із комунальними показниками."""

from __future__ import annotations

import csv
from datetime import date
from pathlib import Path
from typing import TextIO

from .errors import DataError

REQUIRED_FIELDS: frozenset[str] = frozenset({"meter", "date", "value", "tariff"})


def _parse_iso_date(raw_date: str, row_number: int) -> str:
    """Перевіряє дату у форматі ISO YYYY-MM-DD і повертає нормалізований рядок."""
    cleaned = raw_date.strip()
    if not cleaned:
        raise DataError(f"Рядок {row_number}: поле 'date' не може бути порожнім")
    try:
        parsed = date.fromisoformat(cleaned)
    except ValueError as error:
        raise DataError(
            f"Рядок {row_number}: поле 'date' має бути у форматі YYYY-MM-DD (отримано {cleaned!r})"
        ) from error
    return parsed.isoformat()


def _parse_non_negative_float(raw_value: str, field_name: str, row_number: int) -> float:
    """Перетворює текстове поле на невід'ємне дійсне число."""
    cleaned = raw_value.strip()
    if not cleaned:
        raise DataError(f"Рядок {row_number}: поле '{field_name}' не може бути порожнім")
    try:
        numeric = float(cleaned)
    except ValueError as error:
        raise DataError(
            f"Рядок {row_number}: поле '{field_name}' має бути числом (отримано {cleaned!r})"
        ) from error
    if numeric < 0:
        raise DataError(
            f"Рядок {row_number}: поле '{field_name}' не може бути від'ємним ({numeric})"
        )
    return numeric


def parse_records(source: TextIO) -> list[dict[str, object]]:
    """Читає CSV із відкритого текстового потоку та повертає список типізованих записів."""
    reader = csv.DictReader(source)
    if reader.fieldnames is None:
        return []

    normalized_headers = [name.strip() for name in reader.fieldnames]
    if set(normalized_headers) != REQUIRED_FIELDS or len(normalized_headers) != len(
        REQUIRED_FIELDS
    ):
        raise DataError(
            f"Некоректний заголовок CSV: очікувалися поля {sorted(REQUIRED_FIELDS)}, "
            f"отримано {normalized_headers}"
        )

    records: list[dict[str, object]] = []
    for row_number, row in enumerate(reader, start=2):
        if None in row or any(value is None for value in row.values()):
            raise DataError(f"Рядок {row_number}: неповна кількість стовпців у рядку")

        meter = row["meter"].strip()
        if not meter:
            raise DataError(f"Рядок {row_number}: поле 'meter' не може бути порожнім")

        iso_date = _parse_iso_date(row["date"], row_number)
        value = _parse_non_negative_float(row["value"], "value", row_number)
        tariff = _parse_non_negative_float(row["tariff"], "tariff", row_number)
        cost = round(value * tariff, 6)

        records.append(
            {
                "meter": meter,
                "date": iso_date,
                "value": value,
                "tariff": tariff,
                "cost": cost,
            }
        )
    return records


def load_records(path: Path) -> list[dict[str, object]]:
    """Відкриває CSV-файл у UTF-8 із newline='' та повертає типізовані записи."""
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            return parse_records(source)
    except OSError as error:
        raise DataError(f"Не вдалося відкрити файл '{path}': {error.strerror or error}") from error
