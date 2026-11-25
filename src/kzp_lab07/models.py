"""Об'єктна модель комунальних показників (Варіант 9)."""

from __future__ import annotations

import math
from dataclasses import dataclass
from datetime import date
from typing import Protocol


class BillableReading(Protocol):
    """Протокол об'єкта комунального показника з методом cost()."""

    @property
    def meter(self) -> str:
        """Назва лічильника."""

    @property
    def value(self) -> float:
        """Обсяг споживання."""

    def cost(self) -> float:
        """Обчислює вартість споживання."""


class Reading:
    """Базовий клас показання комунального лічильника."""

    def __init__(self, meter: str, reading_date: date, value: float, tariff: float) -> None:
        """Створює показник лічильника та перевіряє інваріанти полів."""
        clean_meter = meter.strip()
        if not clean_meter:
            raise ValueError("Назва лічильника не може бути порожньою.")
        self.meter = clean_meter
        self.reading_date = reading_date
        self.value = value
        self.tariff = tariff

    @property
    def meter(self) -> str:
        """Повертає назву лічильника."""
        return self._meter

    @meter.setter
    def meter(self, raw: str) -> None:
        """Встановлює непорожню назву лічильника."""
        clean = raw.strip()
        if not clean:
            raise ValueError("Назва лічильника не може бути порожньою.")
        self._meter = clean

    @property
    def value(self) -> float:
        """Повертає обсяг споживання."""
        return self._value

    @value.setter
    def value(self, amount: float) -> None:
        """Встановлює невід'ємний скінченний обсяг споживання."""
        if not math.isfinite(amount) or amount < 0.0:
            raise ValueError("Показник споживання не може бути від'ємним.")
        self._value = float(amount)

    @property
    def tariff(self) -> float:
        """Повертає тариф за одиницю споживання."""
        return self._tariff

    @tariff.setter
    def tariff(self, rate: float) -> None:
        """Встановлює невід'ємний скінченний тариф."""
        if not math.isfinite(rate) or rate < 0.0:
            raise ValueError("Тариф не може бути від'ємним.")
        self._tariff = float(rate)

    def cost(self) -> float:
        """Оголошує поліморфний розрахунок вартості для підтипів."""
        raise NotImplementedError

    def __str__(self) -> str:
        """Повертає коротке подання для користувача."""
        return f"{self.meter} ({self.reading_date.isoformat()}): {self.value:.2f}"

    def __repr__(self) -> str:
        """Повертає діагностичне подання об'єкта."""
        return (
            f"{type(self).__name__}(meter={self.meter!r}, "
            f"reading_date={self.reading_date!r}, value={self.value!r}, tariff={self.tariff!r})"
        )

    def __eq__(self, other: object) -> bool:
        """Порівнює об'єкти одного підтипу за повним станом."""
        if not isinstance(other, Reading):
            return NotImplemented
        return type(self) is type(other) and (
            self.meter,
            self.reading_date,
            self.value,
            self.tariff,
        ) == (
            other.meter,
            other.reading_date,
            other.value,
            other.tariff,
        )


class ElectricityReading(Reading):
    """Показання лічильника електроенергії."""

    def cost(self) -> float:
        """Обчислює вартість електроенергії за базовим тарифом."""
        return self.value * self.tariff


class WaterReading(Reading):
    """Показання лічильника води (з урахуванням коефіцієнта водовідведення 1.1)."""

    DRAINAGE_FACTOR: float = 1.1

    def cost(self) -> float:
        """Обчислює вартість водопостачання та водовідведення."""
        return self.value * self.tariff * self.DRAINAGE_FACTOR


@dataclass(frozen=True)
class UtilitySummary:
    """Незмінний підсумок аналізу комунальних показників."""

    total_count: int
    selected_count: int
    threshold: float
    unique_meters: tuple[str, ...]
    sum_by_meter: tuple[tuple[str, float], ...]
    total_cost: float
    max_value: float
    max_meter: str
