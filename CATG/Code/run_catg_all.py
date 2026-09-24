"""
CATG scale-out driver (Group 9).

Automates the full CATG pipeline for EVERY Defects4J bug in the dataset:

    for each (project, bug) in manifest:
        checkout buggy
        CATG dconcolic  -> generated symbolic inputs
        catg_to_junit   -> JUnit suite (CATG/TestCode/<Project>_<bug>/)
        run_experiment  -> one truthful row in results/benchmark_results.csv

Resume: a (project,bug) whose row already exists in the CSV is skipped
unless --force is given (passes --force down to run_experiment.py).

Usage:

  python run_catg_all.py --manifest ../manifest_benchmark.csv
  python run_catg_all.py --manifest ../manifest_benchmark.csv --force
  python run_catg_all.py --projects Lang Math --force
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import subprocess
import sys
import time
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent   # ...\CATG\Code
ROOT = SCRIPT_DIR.parent.parent                # ...\SQA_Project_Group9
SCRIPTS_DIR = ROOT / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

D4J_BUGS_CSV = ROOT.parent / "results" / "defects4j_bugs.csv"
D4J_BUGS_CSV = ROOT / "results" / "defects4j_bugs.csv"

BUDGETS = {"default": "default"}
EXISTING_SUITES: dict[tuple[str, int], Path] = {}


def _existing_rows(path: Path) -> set[tuple[str, str]]:
    """(project, bug) pairs already recorded in the CSV (tool-agnostic)."""
    if not path.exists():
        return set()
    with path.open(newline="", encoding="utf-8-sig") as fh:
        for row in csv.DictReader(fh):
            yield (row["project"], row["bug"])


def _checkout(project: str, bug: int) -> str:
    """Checkout buggy checkout dir for a bug (reuse cached one if present)."""
    wd = ROOT / "CATG" / "Workdirs" / f"{project}_{bug}_b"
    wd.mkdir(parents=True, exist_ok=True)
    if (wd / "defects4j.build.properties").exists():
        return str(wd)
    helper = sys.executable
    subprocess.run(
        [
            helper,
            str(SCRIPTS_DIR / "d4j_helpers.py"),
            "--project", project,
            "--bug", str(bug),
            "--workdir", str(wd),
            "--checkout",
        ],
        check=True,
    )
    return str(wd)


def _run_catg(wd: str, project: str, bug: int, budget: str = "default") -> Path:
    """Run CATG dconcolic on a buggy checkout -> generated inputs file path."""
    out_dir = ROOT / "CATG" / "Result_Round2"
    out_dir.mkdir(parents=True, exist_ok=True)
    inputs_file = out_dir / f"{project}-{bug}_generated_inputs.txt"

    dconcolic = ROOT / "CATG" / "Code" / "dconcolic"
    if not dconcolic.exists():
        raise FileNotFoundError(f"missing CATG launcher: {dconcolic}")

    # CATG generates symbolic inputs for the driver it is pointed at.
    subprocess.run(
        [
            "bash",
            str(dconcolic),
            "--budget", budget,
            "--out", str(inputs_file),
            "--workdir", wd,
        ],
        check=False,
    )
    if not inputs_file.exists():
        raise FileNotFoundError(f"CATG produced no inputs file: {inputs_file}")
    return inputs_file


def _to_junit(project: str, bug: int, inputs_file: Path,
              target_method: str) -> Path:
    """Run catg_to_junit adapter -> JUnit suite dir for the bug."""
    out_dir = ROOT / "CATG" / "TestCode" / f"{project}_{bug}"
    out_dir.mkdir(parents=True, exist_ok=True)
    subprocess.run(
        [
            sys.executable,
            str(SCRIPT_DIR / "catg_to_junit.py"),
            "--project", project,
            "--bug", str(bug),
            "--target-method", target_method,
            "--inputs-file", str(inputs_file),
            "--out-dir", str(out_dir),
        ],
        check=True,
    )
    return out_dir


def _run_experiment(project: str, bug: int, test_dir: Path, force: bool) -> None:
    cmd = [
        sys.executable,
        str(SCRIPT_DIR / "run_experiment.py"),
        "--project", project,
        "--bug", str(bug),
        "--tool", "CATG",
        "--rep", "1",
        "--test-dir", str(test_dir),
    ]
    if force:
        cmd.append("--force")
    subprocess.run(cmd, check=False)


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    p.add_argument("--manifest", type=Path,
                   help="CSV with project,bug[,budget][,target_method]")
    p.add_argument("--projects", help="comma list, e.g. Lang,Math")
    p.add_argument("--budget", default="default")
    p.add_argument("--dry-run", action="store_true",
                   help="print the plan (project,bug -> suite -> pipeline) without running")
    p.add_argument("--force", action="store_true")
    p.add_argument("--limit", type=int, help="process only first N bugs (pilot)")
    args = p.parse_args()

    if args.manifest:
        rows = []
        with args.manifest.open(newline="", encoding="utf-8-sig") as fh:
            for r in csv.DictReader(fh):
                rows.append(r)
    elif args.projects:
        rows = []
        for proj in args.projects.split(","):
            for bug in _list_defects4j_bugs(proj):
                rows.append({"project": proj, "bug": str(bug), "budget": args.budget})
    else:
        p.error("need --manifest or --projects")

    done = set(_existing_rows(ROOT / "results" / "benchmark_results.csv"))
    if args.limit:
        rows = rows[: args.limit]

    start = time.time()
    for i, r in enumerate(rows, 1):
        project = r["project"]
        bug = int(r["bug"])
        budget = r.get("budget") or args.budget
        target_method = r.get("target_method", _default_target(project))

        if not args.force and (project, str(bug)) in done:
            print(f"[skip] {project}-{bug} already in CSV")
            continue

        t0 = time.time()
        try:
            wd = _checkout(project, bug)
            inputs_file = _run_catg(wd, project, bug, budget)
            suite = _to_junit(project, bug, inputs_file, target_method)
            _run_experiment(project, bug, suite, args.force)
            print(f"[ok]   {project}-{bug}: {time.time()-t0:.1f}s "
                  f"(inputs={inputs_file.name}, suite={suite.name})")
            done.add((project, str(bug)))
        except Exception as e:  # noqa: BLE001 - one bad bug must not kill the run
            print(f"[ERR]  {project}-{bug}: {e}")

    print(f"\n[done] {i}/{len(rows)} processed in {time.time()-start:.1f}s")


def _list_defects4j_bugs(project: str) -> list[int]:
    """List bug IDs available in the Defects4J dataset for a project."""
    csv_path = ROOT / "results" / "defects4j_bugs.csv"
    if not csv_path.exists():
        raise FileNotFoundError(f"no defects4j_bugs.csv; build it first ({csv_path})")
    out = []
    with csv_path.open(newline="", encoding="utf-8-sig") as fh:
        for r in csv.DictReader(fh):
            if r["project"] == project:
                out.append(int(r["bug"]))
    return sorted(out)


def _default_target(project: str) -> str:
    """Best-guess CATG target method per project (can be overridden via manifest)."""
    table = {
        "Lang": "org.apache.commons.lang3.math.NumberUtils::createNumber",
    }
    return table.get(project, "org.apache.commons.lang3..*")


if __name__ == "__main__":
    main()