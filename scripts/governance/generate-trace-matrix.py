#!/usr/bin/env python3
from __future__ import annotations

import argparse
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/governance"))

from traceability import codes  # noqa: E402
from traceability.matrix import matrix_path, render_matrix  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check-clean", action="store_true")
    parser.add_argument(
        "--graph",
        type=Path,
        default=ROOT / "docs/governance/traceability/traceability.yaml",
    )
    args = parser.parse_args()

    graph = yaml.safe_load(args.graph.read_text())
    rendered = render_matrix(graph)
    out = matrix_path(ROOT)
    out.parent.mkdir(parents=True, exist_ok=True)

    if args.check_clean:
        if not out.is_file():
            print(f"{codes.TRACE_GENERATED_REPORT_DIRTY} missing {out}", file=sys.stderr)
            return 2
        existing = out.read_text()
        if existing != rendered:
            print(f"{codes.TRACE_GENERATED_REPORT_DIRTY} {out}", file=sys.stderr)
            return 3
        print("TRACE_MATRIX_CLEAN")
        return 0

    out.write_text(rendered)
    print(f"TRACE_MATRIX_WRITTEN {out.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
