"""Формування детермінованого текстового звіту та запис у файл UTF-8."""

from __future__ import annotations

from pathlib import Path


def format_report(summary: dict[str, object]) -> str:
    """Перетворює підсумковий словник на детермінований текстовий звіт."""
    meters: list[str] = list(summary["unique_meters"])  # type: ignore[arg-type]
    totals: dict[str, float] = dict(summary["sum_by_meter"])  # type: ignore[arg-type]

    meters_line = ", ".join(meters) if meters else "немає"
    lines = [
        "=== ЗВІТ ПО КОМУНАЛЬНИХ ПОКАЗНИКАХ (ВАРІАНТ 9) ===",
        f"Усього записів у вхідному файлі: {summary['total_records']}",
        f"Поріг відбору (value > threshold): {float(summary['threshold']):.2f}",
        f"Відібрано записів: {summary['selected_count']}",
        f"Унікальні лічильники: {meters_line}",
        "Сума споживання за лічильниками:",
    ]

    if totals:
        for meter, total_val in sorted(totals.items()):
            lines.append(f"  - {meter}: {total_val:.2f}")
    else:
        lines.append("  - немає даних за умовою відбору")

    lines.extend(
        [
            f"Загальна вартість споживання: {float(summary['total_cost']):.2f} грн",
            f"Найбільший показник: {float(summary['max_value']):.2f} ({summary['max_meter']})",
        ]
    )
    return "\n".join(lines) + "\n"


def write_report(path: Path, text: str) -> None:
    """Створює батьківський каталог за потреби та записує звіт у UTF-8."""
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as target:
        target.write(text)
