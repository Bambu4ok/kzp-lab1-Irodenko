"""Аналіз та агрегація об'єктів комунальних показників через протокол."""

from __future__ import annotations

from collections.abc import Iterable, Sequence

from kzp_lab07.models import BillableReading, UtilitySummary

DEFAULT_THRESHOLD: float = 10.0


def filter_above_threshold(
    readings: Iterable[BillableReading],
    threshold: float = DEFAULT_THRESHOLD,
) -> list[BillableReading]:
    """Відбирає показники, де обсяг перевищує поріг (value > threshold)."""
    return [item for item in readings if item.value > threshold]


def unique_meters(readings: Iterable[BillableReading]) -> set[str]:
    """Повертає множину унікальних назв лічильників."""
    return {item.meter for item in readings}


def sum_by_meter(readings: Iterable[BillableReading]) -> dict[str, float]:
    """Обчислює сумарне споживання за кожним лічильником."""
    totals: dict[str, float] = {}
    for item in readings:
        totals[item.meter] = totals.get(item.meter, 0.0) + item.value
    return totals


def total_cost(readings: Iterable[BillableReading]) -> float:
    """Обчислює сумарну вартість споживання поліморфним викликом item.cost()."""
    return sum(item.cost() for item in readings)


def make_summary(
    readings: Sequence[BillableReading],
    threshold: float = DEFAULT_THRESHOLD,
) -> UtilitySummary:
    """Формує незмінний об'єкт підсумку UtilitySummary."""
    selected = filter_above_threshold(readings, threshold=threshold)
    meters = tuple(sorted(unique_meters(selected)))
    sums_dict = sum_by_meter(selected)
    ordered_sums = tuple((meter, sums_dict[meter]) for meter in sorted(sums_dict))
    max_item = max(selected, key=lambda item: item.value, default=None)

    return UtilitySummary(
        total_count=len(readings),
        selected_count=len(selected),
        threshold=float(threshold),
        unique_meters=meters,
        sum_by_meter=ordered_sums,
        total_cost=round(total_cost(selected), 2),
        max_value=max_item.value if max_item is not None else 0.0,
        max_meter=max_item.meter if max_item is not None else "-",
    )
