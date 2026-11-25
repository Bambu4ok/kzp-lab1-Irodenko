"""Формування та запис текстового звіту для об'єктної моделі."""

from __future__ import annotations

from pathlib import Path

from kzp_lab07.models import UtilitySummary


def format_report(summary: UtilitySummary) -> str:
    """Повертає детермінований текст звіту за незмінним об'єктом UtilitySummary."""
    meters_text = ", ".join(summary.unique_meters) if summary.unique_meters else "-"
    lines: list[str] = [
        "=== ЗВІТ ПО КОМУНАЛЬНИХ ПОКАЗНИКАХ (ВАРІАНТ 9) ===",
        f"Усього записів у вхідному файлі: {summary.total_count}",
        f"Поріг відбору (value > threshold): {summary.threshold:.2f}",
        f"Відібрано записів: {summary.selected_count}",
        f"Унікальні лічильники: {meters_text}",
        "Сума споживання за лічильниками:",
    ]

    if summary.sum_by_meter:
        for meter, total in summary.sum_by_meter:
            lines.append(f"  - {meter}: {total:.2f}")
    else:
        lines.append("  - немає записів")

    lines.append(f"Загальна вартість споживання: {summary.total_cost:.2f} грн")
    if summary.selected_count > 0:
        lines.append(f"Найбільший показник: {summary.max_value:.2f} ({summary.max_meter})")
    else:
        lines.append("Найбільший показник: 0.00 (-)")

    return "\n".join(lines) + "\n"


def write_report(output_path: str | Path, summary: UtilitySummary) -> None:
    """Записує текстовий звіт у файл у кодуванні UTF-8."""
    path = Path(output_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open(mode="w", encoding="utf-8", newline="\n") as handle:
        handle.write(format_report(summary))
