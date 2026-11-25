"""Тести модуля analysis.py: відбір, дублікати, порожній результат, *args/**kwargs."""

from __future__ import annotations

import pytest

from kzp_lab06.analysis import (
    filter_above_threshold,
    make_summary,
    sum_by_meter,
    summarize_metrics,
    unique_meters,
)


def _sample_records() -> list[dict[str, object]]:
    return [
        {"meter": "Газ", "date": "2026-10-03", "value": 85.0, "tariff": 7.96, "cost": 676.60},
        {"meter": "Вода", "date": "2026-10-02", "value": 12.0, "tariff": 30.50, "cost": 366.00},
        {"meter": "Газ", "date": "2026-10-20", "value": 115.0, "tariff": 7.96, "cost": 915.40},
        {"meter": "Вода", "date": "2026-10-16", "value": 10.0, "tariff": 30.50, "cost": 305.00},
    ]


def test_filter_boundary_strictly_greater_than_threshold() -> None:
    """Граничне значення value == threshold (10.0) не включається у вибірку value > 10.0."""
    selected = filter_above_threshold(_sample_records(), threshold=10.0)
    assert len(selected) == 3
    assert all(float(item["value"]) > 10.0 for item in selected)


def test_unique_meters_deduplicates_and_sum_by_meter_is_sorted() -> None:
    """Множина прибирає дублікати лічильників, а словник агрегатів має відсортовані ключі."""
    selected = filter_above_threshold(_sample_records(), threshold=10.0)
    assert unique_meters(selected) == {"Вода", "Газ"}
    totals = sum_by_meter(selected)
    assert list(totals.keys()) == ["Вода", "Газ"]
    assert totals == {"Вода": 12.0, "Газ": 200.0}


def test_make_summary_on_empty_selection() -> None:
    """Якщо жоден запис не пройшов поріг, звіт коректно повертає нулі та порожні колекції."""
    summary = make_summary(_sample_records(), threshold=500.0)
    assert summary["total_records"] == 4
    assert summary["selected_count"] == 0
    assert summary["unique_meters"] == []
    assert summary["sum_by_meter"] == {}
    assert summary["total_cost"] == 0.0
    assert summary["max_value"] == 0.0
    assert summary["max_meter"] == "—"


def test_summarize_metrics_args_defaults_kwargs() -> None:
    """Перевіряє роботу функції з *args, типовим threshold та **kwargs."""
    res = summarize_metrics(
        _sample_records(),
        "value",
        "cost",
        threshold=10.0,
        value="Споживання",
        cost="Вартість",
    )
    assert res["selected_count"] == 3
    assert res["Споживання"] == 212.0
    assert res["Вартість"] == 1958.0


def test_summarize_metrics_unknown_field_raises_value_error() -> None:
    """Невідома назва метрики відхиляється явною помилкою ValueError."""
    with pytest.raises(ValueError, match="Невідома метрика"):
        summarize_metrics(_sample_records(), "unknown_field")
