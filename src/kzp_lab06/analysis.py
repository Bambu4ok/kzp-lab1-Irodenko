"""Аналітичні функції для обробки комунальних показників (Варіант 9)."""

from __future__ import annotations

from collections.abc import Iterable

DEFAULT_THRESHOLD: float = 10.0


def filter_above_threshold(
    records: Iterable[dict[str, object]],
    threshold: float = DEFAULT_THRESHOLD,
) -> list[dict[str, object]]:
    """Відбирає записи, де обсяг споживання value строго більший за threshold."""
    return [record for record in records if float(record["value"]) > threshold]


def unique_meters(records: Iterable[dict[str, object]]) -> set[str]:
    """Повертає множину унікальних назв лічильників."""
    return {str(record["meter"]) for record in records}


def sum_by_meter(records: list[dict[str, object]]) -> dict[str, float]:
    """Обчислює сумарне споживання (value) за кожним лічильником у відсортованому порядку."""
    meters = unique_meters(records)
    return {
        meter: round(
            sum(float(record["value"]) for record in records if record["meter"] == meter),
            4,
        )
        for meter in sorted(meters)
    }


def summarize_metrics(
    records: list[dict[str, object]],
    *metrics: str,
    threshold: float = DEFAULT_THRESHOLD,
    **labels: str,
) -> dict[str, object]:
    """Обчислює довільні числові метрики для записів із value > threshold.

    Демонструє використання *args, типового параметра threshold та **kwargs.
    """
    selected = filter_above_threshold(records, threshold=threshold)
    result: dict[str, object] = {
        "selected_count": len(selected),
        "threshold": threshold,
    }
    for metric in metrics:
        if records and any(metric not in record for record in records):
            raise ValueError(f"Невідома метрика у записах: {metric!r}")
        label = labels.get(metric, metric)
        result[label] = round(sum(float(record[metric]) for record in selected), 4)
    return result


def make_summary(
    records: list[dict[str, object]],
    threshold: float = DEFAULT_THRESHOLD,
) -> dict[str, object]:
    """Формує повний словник аналітики за Варіантом 9."""
    selected = filter_above_threshold(records, threshold=threshold)
    meters = unique_meters(selected)
    totals_by_meter = sum_by_meter(selected)

    total_cost = round(sum(float(record["cost"]) for record in selected), 2)
    max_record = max(selected, key=lambda item: float(item["value"]), default=None)
    max_value = float(max_record["value"]) if max_record is not None else 0.0
    max_meter = str(max_record["meter"]) if max_record is not None else "—"

    metrics = summarize_metrics(
        records,
        "value",
        "cost",
        threshold=threshold,
        value="Сумарний обсяг (відібрано)",
        cost="Загальна вартість (відібрано)",
    )

    return {
        "total_records": len(records),
        "selected_count": len(selected),
        "threshold": threshold,
        "unique_meters": sorted(meters),
        "sum_by_meter": totals_by_meter,
        "total_cost": total_cost,
        "max_value": max_value,
        "max_meter": max_meter,
        "metrics": metrics,
    }
