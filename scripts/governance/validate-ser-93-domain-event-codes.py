#!/usr/bin/env python3
"""Assert SER-93 §9 domain-event code registry matches accepted pack faf775736687."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCHEMA = ROOT / "contracts/schemas/ser-93/domain-event-codes.v1.schema.json"

# Authority: Linear SER-93 pack §9 "Initial safe event codes" (ACCEPTED_FOR_P0_FREEZE).
AUTHORITATIVE_CODES: tuple[str, ...] = (
    "connectivity.provider-account.created:v1",
    "connectivity.provider-account.updated:v1",
    "connectivity.provider-account.disconnected:v1",
    "connectivity.sync.requested:v1",
    "connectivity.sync.started:v1",
    "connectivity.sync.completed:v1",
    "connectivity.sync.quarantined:v1",
    "instrument.canonical.changed:v1",
    "instrument.composition.accepted:v1",
    "instrument.classification.accepted:v1",
    "instrument.corporate-action.accepted:v1",
    "market.observation.accepted:v1",
    "macro.vintage.accepted:v1",
    "research.case.created:v1",
    "research.case.evidence-added:v1",
    "research.case.closed:v1",
    "research.source-data.accepted:v1",
)


def main() -> int:
    if len(AUTHORITATIVE_CODES) != 17:
        print("SER93_DOMAIN_EVENT_CODES_FAIL expected_count=17", file=sys.stderr)
        return 2

    if not SCHEMA.is_file():
        print(f"SER93_DOMAIN_EVENT_CODES_FAIL missing_schema={SCHEMA}", file=sys.stderr)
        return 2

    doc = json.loads(SCHEMA.read_text())
    if doc.get("$id") != "urn:sergiotr:schema:ser-93:domain-event-codes:v1":
        print("SER93_DOMAIN_EVENT_CODES_FAIL invalid_schema_id", file=sys.stderr)
        return 2

    enum = doc.get("enum")
    if not isinstance(enum, list):
        print("SER93_DOMAIN_EVENT_CODES_FAIL schema_missing_enum", file=sys.stderr)
        return 2

    if enum != list(AUTHORITATIVE_CODES):
        missing = set(AUTHORITATIVE_CODES) - set(enum)
        extra = set(enum) - set(AUTHORITATIVE_CODES)
        if missing or extra or len(enum) != len(AUTHORITATIVE_CODES):
            print(
                f"SER93_DOMAIN_EVENT_CODES_FAIL mismatch missing={sorted(missing)} extra={sorted(extra)}",
                file=sys.stderr,
            )
            return 2
        print("SER93_DOMAIN_EVENT_CODES_FAIL enum_order", file=sys.stderr)
        return 2

    print("SER93_DOMAIN_EVENT_CODES_PASS count=17")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())