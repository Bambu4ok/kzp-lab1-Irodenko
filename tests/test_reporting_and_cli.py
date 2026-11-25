"""Тести формування звіту, запису у файл та роботи CLI."""

from __future__ import annotations

from pathlib import Path

import pytest

from kzp_lab06.analysis import make_summary
from kzp_lab06.cli import main
from kzp_lab06.reporting import format_report, write_report


def test_write_and_format_report_utf8(tmp_path: Path) -> None:
    """Перевіряє детерміноване форматування та запис звіту у UTF-8."""
    records = [
        {"meter": "Газ", "date": "2026-10-03", "value": 85.0, "tariff": 7.96, "cost": 676.60},
        {"meter": "Вода", "date": "2026-10-02", "value": 12.0, "tariff": 30.50, "cost": 366.00},
    ]
    summary = make_summary(records, threshold=10.0)
    report_path = tmp_path / "nested" / "report.txt"
    write_report(report_path, format_report(summary))

    content = report_path.read_text(encoding="utf-8")
    assert "Унікальні лічильники: Вода, Газ" in content
    assert "  - Вода: 12.00" in content
    assert "  - Газ: 85.00" in content
    assert "Загальна вартість споживання: 1042.60 грн" in content
    assert "Найбільший показник: 85.00 (Газ)" in content


def test_cli_success_and_error_exit_codes(
    tmp_path: Path, capsys: pytest.CaptureFixture[str]
) -> None:
    """CLI повертає 0 для валідного CSV і 2 для пошкодженого файлу з повідомленням у stderr."""
    valid_csv = tmp_path / "valid.csv"
    valid_csv.write_text(
        "meter,date,value,tariff\nЕлектроенергія,2026-10-01,145.5,4.32\n",
        encoding="utf-8",
    )
    out_report = tmp_path / "summary.txt"

    exit_ok = main([str(valid_csv), str(out_report)])
    assert exit_ok == 0
    assert out_report.exists()
    assert "628.56 грн" in out_report.read_text(encoding="utf-8")

    broken_csv = tmp_path / "broken.csv"
    broken_csv.write_text(
        "meter,date,value,tariff\nЕлектроенергія,2026-10-01,not_a_number,4.32\n",
        encoding="utf-8",
    )
    exit_err = main([str(broken_csv), str(tmp_path / "fail.txt")])
    captured = capsys.readouterr()
    assert exit_err == 2
    assert "Рядок 2: поле 'value' має бути числом" in captured.err
