from __future__ import annotations

"""
SER-97 Stage A/B validation profiles.

FOUNDATION — mandatory for Stage A CI. Structural SER-96 grammar/graph integrity.
FULL — Stage B after SER-102; activates coverage cardinalities that need UseCase/SOURCE_OWNER mappings.

This is an implementation-detail of the validator, not a graph node status.
"""

from enum import Enum


class ValidationProfile(str, Enum):
    FOUNDATION = "foundation"
    FULL = "full"


# Rules that require SER-102 (or later) authoritative mappings. Enforced only in FULL.
FULL_ONLY_RULES = frozenset(
    {
        "REQUIREMENT_IMPLEMENTATION_OWNER",
        "REQUIREMENT_USE_CASE_LINK",
        "USE_CASE_IMPLEMENTS_REQUIREMENT",
        "USE_CASE_SOURCE_OWNER",
        "SOURCE_PATH_OWNERSHIP",
        "TRANSPORT_EXPOSES",
        "EVENT_PRODUCER_CONSUMER",
        "CRITICAL_VERIFICATION_COVERAGE",
    }
)


def parse_profile(value: str | None) -> ValidationProfile:
    if value is None or value == "" or value == ValidationProfile.FOUNDATION.value:
        return ValidationProfile.FOUNDATION
    if value == ValidationProfile.FULL.value:
        return ValidationProfile.FULL
    raise ValueError(f"unknown validation profile: {value}")
