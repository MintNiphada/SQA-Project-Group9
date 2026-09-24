"""Group 9 - build results/defects4j_bugs.csv (all Defects4J bugs via d4j helper)."""
from __future__ import annotations

import csv
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from d4j_helpers import defects4j

ROOT = Path(__file__).resolve().parents[1]

PROJECTS = ["Chart", "Cli", "Closure", "Codec", "Collections", "Compress",
            "Csv", "Gson", "JacksonCore", "JacksonDatabind", "JacksonXml",
            "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"]

csv_out = ROOT / "results" / "defects4j_bugs.csv"
rows: list[tuple[str, str]] = []
ok = fail = 0
for p in PROJECTS:
    r = defects4j(["bids", "-p", p])
    if r.returncode == 0:
        bugs = [ln.strip() for ln in (r.stdout or "").splitlines() if ln.strip().isdigit()]
        if bugs:
            rows += [(p, b) for b in sorted(set(bugs), key=int)]
            ok += 1
        else:
            fail += 1
            print(f"[ERR] {p}: no bug ids (rc=0)")
    else:
        fail += 1
        tail = ((r.stderr or "").strip().splitlines() or ["(no stderr)"])[-1]
        print(f"[ERR] {p}: rc={r.returncode} {tail}")

rows.sort(key=lambda x: (x[0], int(x[1])))
csv_out.parent.mkdir(parents=True, exist_ok=True)
with csv_out.open("w", newline="", encoding="utf-8") as fh:
    w = csv.writer(fh)
    w.writerow(["project", "bug"])
    w.writerows(rows)
print(f"[OK] {ok}/17 projects errored={fail} | total bugs={len(rows)} | {csv_out}")