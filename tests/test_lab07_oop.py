"""Поведінкові тести моделей, властивостей, dataclass та підтипів (ЛР № 7)."""

from __future__ import annotations

import dataclasses
from datetime import date

import pytest

from kzp_lab07.analysis import make_summary, total_cost
from kzp_lab07.cli import main
from kzp_lab07.errors import DataError
from kzp_lab07.loader import load_readings
from kzp_lab07.models import ElectricityReading, UtilitySummary, WaterReading


def test_property_setter_validation() -> None:
    """Перевіряє коректні, нульові та помилкові значення у setter."""
    item = ElectricityReading("Електроенергія", date(2026, 10, 1), 15.0, 4.32)
    item.value = 0.0
    assert item.value == 0.0

    with pytest.raises(ValueError):
        item.value = -0.1

    with pytest.raises(ValueError):
        item.tariff = -5.0

    with pytest.raises(ValueError):
        item.meter = "   "


def test_subtypes_cost_and_duck_typing() -> None:
    """Перевіряє різні формули cost() для підтипів і спільну функцію без перевірки типу."""
    el = ElectricityReading("Електроенергія", date(2026, 10, 1), 100.0, 4.0)
    wt = WaterReading("Вода, холодна", date(2026, 10, 1), 100.0, 4.0)

    assert el.cost() == pytest.approx(400.0)
    assert wt.cost() == pytest.approx(440.0)
    assert el.cost() != wt.cost()
    assert total_cost([el, wt]) == pytest.approx(840.0)


def test_dunder_methods_and_equality() -> None:
    """Перевіряє __str__, __repr__ та __eq__."""
    a = ElectricityReading("Газ", date(2026, 10, 2), 20.0, 7.96)
    b = ElectricityReading("Газ", date(2026, 10, 2), 20.0, 7.96)
    c = WaterReading("Газ", date(2026, 10, 2), 20.0, 7.96)

    assert a == b
    assert a != c
    assert a != "Газ"
    assert "Газ" in str(a)
    assert "ElectricityReading" in repr(a)


def test_summary_dataclass_is_frozen() -> None:
    """Перевіряє незмінність підсумкового @dataclass(frozen=True)."""
    summary = UtilitySummary(
        total_count=1,
        selected_count=1,
        threshold=10.0,
        unique_meters=("Газ",),
        sum_by_meter=(("Газ", 20.0),),
        total_cost=159.2,
        max_value=20.0,
        max_meter="Газ",
    )
    with pytest.raises(dataclasses.FrozenInstanceError):
        summary.total_count = 5  # type: ignore[misc]


def test_loader_empty_and_invalid_rows(tmp_path) -> None:
    """Перевіряє порожній вхід та помилку з номером рядка у loader."""
    empty_csv = tmp_path / "empty.csv"
    empty_csv.write_text("meter,date,value,tariff\n", encoding="utf-8")
    readings = load_readings(empty_csv)
    assert readings == []
    summary = make_summary(readings)
    assert summary.total_cost == 0.0

    bad_csv = tmp_path / "bad.csv"
    bad_csv.write_text(
        "meter,date,value,tariff\nГаз,2026-10-01,-10.0,7.96\n",
        encoding="utf-8",
    )
    with pytest.raises(DataError, match="Рядок 2"):
        load_readings(bad_csv)


def test_cli_success_and_error(tmp_path, capsys) -> None:
    """Перевіряє CLI на успішний (exit 0) та помилковий (exit 2) сценарії."""
    csv_file = tmp_path / "readings.csv"
    csv_file.write_text(
        "meter,date,value,tariff\n"
        "Електроенергія,2026-10-01,50.0,4.0\n"
        '"Вода, холодна",2026-10-01,20.0,10.0\n'
        "Газ,2026-10-02,10.0,8.0\n",
        encoding="utf-8",
    )
    out_file = tmp_path / "report.txt"

    code_ok = main([str(csv_file), str(out_file)])
    assert code_ok == 0
    content = out_file.read_text(encoding="utf-8")
    assert "Відібрано записів: 2" in content
    assert "420.00 грн" in content

    code_err = main([str(tmp_path / "missing.csv"), str(out_file)])
    assert code_err == 2
    assert "Помилка вхідних даних" in capsys.readouterr().err
