"""Тести модуля loader.py: коректні дані, порожній CSV, лапки, помилки формату."""

from __future__ import annotations

import io
from pathlib import Path

import pytest

from kzp_lab06.errors import DataError
from kzp_lab06.loader import load_records, parse_records


def test_parse_valid_records_with_comma_in_quotes() -> None:
    """Перевіряє парсинг коректних записів та екранованого текстового поля з комою."""
    csv_text = (
        "meter,date,value,tariff\n"
        '"Вода, холодна",2026-10-02,12.5,30.50\n'
        "Газ,2026-10-03,100.0,7.96\n"
    )
    records = parse_records(io.StringIO(csv_text, newline=""))
    assert len(records) == 2
    assert records[0]["meter"] == "Вода, холодна"
    assert records[0]["value"] == 12.5
    assert records[0]["tariff"] == 30.50
    assert records[0]["cost"] == pytest.approx(381.25)


def test_parse_empty_file_returns_empty_list() -> None:
    """Повністю порожній CSV повертає порожній список."""
    assert parse_records(io.StringIO("", newline="")) == []


def test_parse_header_only_returns_empty_list() -> None:
    """CSV лише із заголовком повертає порожній список."""
    csv_text = "meter,date,value,tariff\n"
    assert parse_records(io.StringIO(csv_text, newline="")) == []


def test_missing_required_column_raises_data_error() -> None:
    """Відсутній обов'язковий стовпець викликає DataError."""
    csv_text = "meter,date,value\nГаз,2026-10-03,100.0\n"
    with pytest.raises(DataError, match="Некоректний заголовок CSV"):
        parse_records(io.StringIO(csv_text, newline=""))


def test_missing_value_in_row_includes_row_number() -> None:
    """Порожнє значення в рядку викликає DataError із номером рядка."""
    csv_text = "meter,date,value,tariff\nГаз,2026-10-03,,7.96\n"
    with pytest.raises(DataError, match="Рядок 2: поле 'value' не може бути порожнім"):
        parse_records(io.StringIO(csv_text, newline=""))


def test_invalid_number_raises_data_error_with_cause() -> None:
    """Нечислове значення викликає DataError із номером рядка та першопричиною ValueError."""
    csv_text = "meter,date,value,tariff\nГаз,2026-10-03,abc,7.96\n"
    with pytest.raises(DataError, match="Рядок 2: поле 'value' має бути числом") as exc_info:
        parse_records(io.StringIO(csv_text, newline=""))
    assert isinstance(exc_info.value.__cause__, ValueError)


def test_negative_number_raises_data_error() -> None:
    """Від'ємний показник відхиляється з помилкою DataError."""
    csv_text = "meter,date,value,tariff\nГаз,2026-10-03,-5.0,7.96\n"
    with pytest.raises(DataError, match="Рядок 2: поле 'value' не може бути від'ємним"):
        parse_records(io.StringIO(csv_text, newline=""))


def test_load_missing_file_raises_data_error(tmp_path: Path) -> None:
    """Відсутній файл на диску обробляється як DataError."""
    with pytest.raises(DataError, match="Не вдалося відкрити файл"):
        load_records(tmp_path / "missing.csv")
