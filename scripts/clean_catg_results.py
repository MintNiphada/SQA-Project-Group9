"""Remove CATG result rows that are not part of the selected Defects4J manifest."""
from __future__ import annotations

import argparse
import csv
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, default=Path("results/defects4j_bugs.csv"))
    parser.add_argument(
        "--results",
        type=Path,
        default=Path("CATG/Result_Round2/benchmark_results.csv"),
    )
    args = parser.parse_args()

    allowed = set()
    with args.manifest.open(newline="", encoding="utf-8-sig") as handle:
        allowed = {(row["project"].strip(), row["bug"].strip()) for row in csv.DictReader(handle)}

    if not args.results.exists():
        print(f"[clean] no result file: {args.results}")
        return 0

    with args.results.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        fieldnames = reader.fieldnames or []
        rows = list(reader)

    kept = [
        row for row in rows
        if row.get("tool") == "CATG"
        and (row.get("project", "").strip(), row.get("bug", "").strip()) in allowed
    ]
    removed = len(rows) - len(kept)
    with args.results.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(kept)
    print(f"[clean] kept={len(kept)} removed={removed} file={args.results}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())