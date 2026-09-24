"""Build results/defects4j_bugs.csv (project,bug) for all D4J projects.

Uses d4j_helpers.defects4j() so the perl/GIT/Java env is set up the same way
the rest of the pipeline does it.
"""
from __future__ import annotations

import csv
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from d4j_helpers import books_bugs_csv, defects4j, ROOT

PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections", "Compress", "Csv",
    "Gson", "JacksonCore", "JacksonDatabind", "JacksonXml", "Jsoup",
    "JxPath", "Lang", "Math", "Mockito", "Time",
]

csv_out = ROOT / "results" / "defects4j_bugs.csv"
rows: list[tuple[str, str]] = []
ok_projects = 0
for p in PROJECTS:
    r = defects4j(["active-bugs", "-p", p])
    if r.returncode != 0:
        print(f"[ERR] {p}: rc={r.returncode} {(r.stderr or '').splitlines()[-1] if (r.stderr or '').splitlines() else ''}")
        continue
    bugs = [ln.strip() for ln in (r.stdout or "").splitlines() if ln.strip().isdigit()]
    if not bugs:
        print(f"[WARN] {p}: no bug ids")
    rows.extend((p, b) for b in sorted(set(bugs), key=int))
    ok_projects += 1

rows.sort(key=lambda x: (x[0], int(x[1])))
csv_out.parent.mkdir(parents=True, exist_ok=True)
with csv_out.open("w", newline="", encoding="utf-8") as fh:
    w = csv.writer(fh)
    w.writerow(["project", "bug"])
    w.writerows(rows)

print(f"[OK] {ok_projects}/17 projects ok | {(len(rows))} (project,bug) rows -> {csv_out}")
