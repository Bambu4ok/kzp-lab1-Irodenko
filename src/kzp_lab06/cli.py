"""Інтерфейс командного рядка (CLI) для генерації звіту з CSV."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from .analysis import DEFAULT_THRESHOLD, make_summary
from .errors import DataError
from .loader import load_records
from .reporting import format_report, write_report


def _configure_stdio_utf8() -> None:
    """Гарантує коректне виведення кирилиці у stdout/stderr на Windows."""
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8")  # type: ignore[union-attr]
        except (AttributeError, ValueError):
            pass


def build_parser() -> argparse.ArgumentParser:
    """Створює парсер аргументів командного рядка."""
    parser = argparse.ArgumentParser(
        prog="kzp-lab06",
        description="Аналіз комунальних показників із CSV та формування текстового звіту.",
    )
    parser.add_argument("input_csv", type=Path, help="Шлях до вхідного CSV-файлу")
    parser.add_argument("output_report", type=Path, help="Шлях до вихідного текстового звіту")
    parser.add_argument(
        "--threshold",
        type=float,
        default=DEFAULT_THRESHOLD,
        help=f"Поріг споживання для відбору value > threshold (за замовчуванням {DEFAULT_THRESHOLD})",
    )
    return parser


def main(argv: list[str] | None = None) -> int:
    """Точка входу CLI: повертає 0 при успіху та 2 при очікуваній помилці даних."""
    _configure_stdio_utf8()
    parser = build_parser()
    args = parser.parse_args(argv)

    try:
        records = load_records(args.input_csv)
        summary = make_summary(records, threshold=args.threshold)
        report_text = format_report(summary)
        write_report(args.output_report, report_text)
    except DataError as error:
        print(f"Помилка даних: {error}", file=sys.stderr)
        return 2

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
