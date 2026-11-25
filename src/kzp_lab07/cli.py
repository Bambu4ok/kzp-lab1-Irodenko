"""Інтерфейс командного рядка (CLI) для об'єктної обробки комунальних показників."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from kzp_lab07.analysis import DEFAULT_THRESHOLD, make_summary
from kzp_lab07.errors import DataError
from kzp_lab07.loader import load_readings
from kzp_lab07.reporting import write_report


def build_parser() -> argparse.ArgumentParser:
    """Створює парсер аргументів командного рядка."""
    parser = argparse.ArgumentParser(
        prog="kzp-lab07",
        description="Об'єктно-орієнтований аналіз комунальних показників із CSV.",
    )
    parser.add_argument("input_csv", type=Path, help="Шлях до вхідного CSV-файлу")
    parser.add_argument("output_report", type=Path, help="Шлях до вихідного текстового звіту")
    parser.add_argument(
        "--threshold",
        type=float,
        default=DEFAULT_THRESHOLD,
        help=f"Поріг value > threshold (типово {DEFAULT_THRESHOLD})",
    )
    return parser


def main(argv: list[str] | None = None) -> int:
    """Точка входу CLI: повертає 0 при успіху та 2 при очікуваній помилці даних."""
    parser = build_parser()
    args = parser.parse_args(argv)

    try:
        readings = load_readings(args.input_csv)
        summary = make_summary(readings, threshold=args.threshold)
        write_report(args.output_report, summary)
    except DataError as exc:
        print(f"Помилка вхідних даних: {exc}", file=sys.stderr)
        return 2

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
