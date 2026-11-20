#!/usr/bin/env python3
"""Assert SER-93 §8 problem-code registry matches accepted pack faf775736687."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCHEMA = ROOT / "contracts/schemas/ser-93/problem-codes.v1.schema.json"

# Authority: Linear SER-93 pack §8 "Minimum stable v1 codes" (ACCEPTED_FOR_P0_FREEZE).
AUTHORITATIVE_CODES: tuple[str, ...] = (
    "CONNECTIVITY_PROVIDER_NOT_FOUND",
    "CONNECTIVITY_PROVIDER_ACCOUNT_NOT_FOUND",
    "CONNECTIVITY_AUTHENTICATION_REQUIRED",
    "CONNECTIVITY_REVALIDATION_REQUIRED",
    "CONNECTIVITY_CONNECTOR_UNAVAILABLE",
    "CONNECTIVITY_UNSUPPORTED_CAPABILITY",
    "CONNECTIVITY_RATE_LIMITED",
    "CONNECTIVITY_LICENSE_REVIEW_REQUIRED",
    "CONNECTIVITY_BINDING_REVISION_CONFLICT",
    "CONNECTIVITY_SYNC_ALREADY_RUNNING",
    "CONNECTIVITY_SYNC_NOT_FOUND",
    "CONNECTIVITY_QUARANTINE_NOT_RETRYABLE",
    "CONNECTIVITY_IDEMPOTENCY_CONFLICT",
    "INSTRUMENT_NOT_FOUND",
    "INSTRUMENT_RESOLUTION_AMBIGUOUS",
    "INSTRUMENT_RESOLUTION_CONFLICT",
    "INSTRUMENT_COMPOSITION_UNAVAILABLE",
    "INSTRUMENT_COMPOSITION_INVALID",
    "INSTRUMENT_CLASSIFICATION_UNAVAILABLE",
    "MARKET_QUOTE_UNAVAILABLE",
    "MARKET_QUOTE_STALE",
    "MARKET_SOURCE_AMBIGUOUS",
    "MARKET_FX_UNAVAILABLE",
    "MARKET_FX_CONVENTION_MISMATCH",
    "MARKET_BENCHMARK_UNAVAILABLE",
    "MACRO_SERIES_NOT_FOUND",
    "MACRO_VINTAGE_UNAVAILABLE",
    "RESEARCH_SOURCE_UNAVAILABLE",
    "RESEARCH_CONTENT_NOT_LICENSED",
    "RESEARCH_CASE_NOT_FOUND",
    "RESEARCH_CASE_REVISION_CONFLICT",
    "SOURCE_POLICY_MISMATCH",
    "SOURCE_DATA_VALIDATION_REJECTED",
)


def main() -> int:
    if len(AUTHORITATIVE_CODES) != 33:
        print("SER93_PROBLEM_CODES_FAIL expected_count=33", file=sys.stderr)
        return 2

    if not SCHEMA.is_file():
        print(f"SER93_PROBLEM_CODES_FAIL missing_schema={SCHEMA}", file=sys.stderr)
        return 2

    doc = json.loads(SCHEMA.read_text())
    if doc.get("$id") != "urn:sergiotr:schema:ser-93:problem-codes:v1":
        print("SER93_PROBLEM_CODES_FAIL invalid_schema_id", file=sys.stderr)
        return 2

    enum = doc.get("enum")
    if not isinstance(enum, list):
        print("SER93_PROBLEM_CODES_FAIL schema_missing_enum", file=sys.stderr)
        return 2

    if enum != list(AUTHORITATIVE_CODES):
        missing = set(AUTHORITATIVE_CODES) - set(enum)
        extra = set(enum) - set(AUTHORITATIVE_CODES)
        if missing or extra or len(enum) != len(AUTHORITATIVE_CODES):
            print(
                f"SER93_PROBLEM_CODES_FAIL mismatch missing={sorted(missing)} extra={sorted(extra)}",
                file=sys.stderr,
            )
            return 2
        print("SER93_PROBLEM_CODES_FAIL enum_order", file=sys.stderr)
        return 2

    print("SER93_PROBLEM_CODES_PASS count=33")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())