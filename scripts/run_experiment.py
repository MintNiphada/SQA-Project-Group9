"""
Universal benchmark runner for Group 9.

Reads a directory of generated JUnit test sources (*.java), packs them as a
Defects4J external test suite, runs them on both the buggy and the fixed
version of a bug, measures coverage + fault detection, and appends one row to
results/benchmark_results.csv.

Usage examples:

  # Single bug, one tool, one budget, first repetition
  python run_experiment.py --project Lang --bug 1 --tool ChatGPT \
      --test-dir ../ChatGPT/TestCode/Lang_1 --budget default --rep 1

  # Run a batch from a manifest (project,bug,tool,budget,test-dir)
  python run_experiment.py --manifest my_manifest.csv --rep 1

  # Dry run (just pack the archive and print paths)
  python run_experiment.py --project Lang --bug 1 --tool ChatGPT \
      --test-dir ../ChatGPT/TestCode/Lang_1 --dry-run

Resume: if a (project,bug,tool,budget,rep) row already exists in
results/benchmark_results.csv it is skipped unless --force is given.
"""
from __future__ import annotations

import argparse
import csv
import sys
import time
from pathlib import Path

from d4j_helpers import (
    build_test_archive,
    checkout,
    cleanup_generated_sources,
    compile_version,
    count_tests,
    has_result,
    append_result,
    read_bug_info,
    run_external_suite,
    workdir,
)


def discover_java(test_dir: Path) -> list[Path]:
    if not test_dir.exists():
        raise FileNotFoundError(f"test dir not found: {test_dir}")
    files = sorted(test_dir.rglob("*.java"))
    if not files:
        raise FileNotFoundError(f"no .java files under: {test_dir}")
    return files


def run_one(project: str, bug: int, tool: str, budget: str, rep: int,
            test_dir: Path, dry_run: bool = False, force: bool = False,
            generation_time_sec: float | None = None,
            extra_notes: str = "") -> dict:
    if has_result(project, bug, tool, budget, rep) and not force:
        print(f"[skip] {project}-{bug} {tool} {budget} rep{rep} (already in results)")
        return {}

    java_files = discover_java(test_dir)
    n_tests = count_tests(java_files)

    # Archive name follows Defects4J external-suite convention.
    archive = test_dir / f"{project}-{bug}b-{tool}.{rep}.tar.bz2"
    build_test_archive(java_files, archive, project, bug, "b")
    print(f"[pack] {archive}  ({len(java_files)} files, ~{n_tests} @Test)")

    if dry_run:
        return {"archive": str(archive), "test_count": n_tests}

    # Make sure both versions are checked out and compiled once.
    w_buggy = checkout(project, bug, "b")
    w_fixed = checkout(project, bug, "f")
    cleanup_generated_sources(w_buggy)
    cleanup_generated_sources(w_fixed)
    compile_version(w_buggy)
    compile_version(w_fixed)

    info = read_bug_info(project, bug, "b")

    t0 = time.time()
    res_b = run_external_suite(project, bug, "b", archive)
    res_f = run_external_suite(project, bug, "f", archive)
    elapsed = round(time.time() - t0, 2)

    # Coverage is measured on the buggy version (where the defect lives).
    line_pct = res_b.line_cov_pct
    branch_pct = res_b.branch_cov_pct

    # Fault detection: suite must FAIL on buggy (res_b) AND PASS on fixed (res_f).
    # run_external_suite stores the count on the matching side, so the count of
    # failures observed while running the FIXED version lives in res_f.failing_on_fixed.
    failing_buggy = res_b.failing_on_buggy
    failing_fixed = res_f.failing_on_fixed
    fault_detected = failing_buggy > 0 and failing_fixed == 0

    notes = f"modified={';'.join(info.modified_classes)}"
    if extra_notes:
        notes = f"{notes};{extra_notes}"
    row = {
        "project": project,
        "bug": bug,
        "tool": tool,
        "budget": budget,
        "repetition": rep,
        "test_count": n_tests,
        "compile_ok": True,
        "status": "completed",
        "failing_on_buggy": failing_buggy,
        "failing_on_fixed": failing_fixed,
        "fault_detected": fault_detected,
        "lines_total": res_b.lines_total,
        "lines_covered": res_b.lines_covered,
        "line_cov_pct": round(line_pct, 2),
        "branches_total": res_b.branches_total,
        "branches_covered": res_b.branches_covered,
        "branch_cov_pct": round(branch_pct, 2),
        "generation_time_sec": elapsed if generation_time_sec is None else round(generation_time_sec, 2),
        "notes": notes,
    }
    append_result(row)
    print(f"[done] {project}-{bug} {tool} {budget} rep{rep} -> "
          f"line={line_pct:.1f}% branch={branch_pct:.1f}% "
          f"fault={fault_detected} tests={n_tests} time={elapsed}s")
    return row


def run_manifest(manifest: Path, rep: int, dry_run: bool, force: bool) -> None:
    with manifest.open(newline="", encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for i, r in enumerate(reader, 1):
            project = r["project"].strip()
            bug = int(r["bug"])
            tool = r["tool"].strip()
            budget = r.get("budget", "default").strip() or "default"
            test_dir = Path(r["test_dir"]).resolve()
            print(f"--- [{i}] {project}-{bug} {tool} {budget} rep{rep} ---")
            try:
                run_one(project, bug, tool, budget, rep, test_dir, dry_run, force)
            except Exception as e:
                print(f"[error] {project}-{bug} {tool}: {e}", file=sys.stderr)
                append_result({
                    "project": project, "bug": bug, "tool": tool,
                    "budget": budget, "repetition": rep,
                    "test_count": 0, "compile_ok": False,
                    "status": "error",
                    "failing_on_buggy": 0, "failing_on_fixed": 0,
                    "fault_detected": False,
                    "lines_total": 0, "lines_covered": 0, "line_cov_pct": 0,
                    "branches_total": 0, "branches_covered": 0, "branch_cov_pct": 0,
                    "generation_time_sec": 0, "notes": f"ERROR: {e}",
                })


def main() -> None:
    p = argparse.ArgumentParser(description="Group 9 Defects4J benchmark runner")
    p.add_argument("--project")
    p.add_argument("--bug", type=int)
    p.add_argument("--tool")
    p.add_argument("--budget", default="default")
    p.add_argument("--rep", type=int, default=1)
    p.add_argument("--test-dir", type=Path)
    p.add_argument("--manifest", type=Path, help="CSV with project,bug,tool,budget,test_dir")
    p.add_argument("--dry-run", action="store_true")
    p.add_argument("--force", action="store_true", help="re-run even if result exists")
    args = p.parse_args()

    if args.manifest:
        run_manifest(args.manifest, args.rep, args.dry_run, args.force)
        return

    if not (args.project and args.bug and args.tool and args.test_dir):
        p.error("need --project --bug --tool --test-dir (or --manifest)")

    run_one(args.project, args.bug, args.tool, args.budget, args.rep,
            args.test_dir, args.dry_run, args.force)


if __name__ == "__main__":
    main()
