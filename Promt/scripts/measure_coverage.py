#!/usr/bin/env python3
"""
Phase 2: measure coverage for rows produced with SKIP_COVERAGE=1.

Reads results/benchmark_data.csv, and for every PASS / TEST_FAIL row whose coverage is still NA:
  checkout buggy version -> take saved test from <Model>/TestCode/... -> defects4j coverage -> update the row.

Resume-safe: rows that already have LineCoverage(%) are skipped. Run AFTER the main runner has finished
(this script rewrites the CSV, so do not run it at the same time as the runner).

Usage:   python scripts/measure_coverage.py
         WORKERS=4 python scripts/measure_coverage.py
         QUICK=1 python scripts/measure_coverage.py      # operate on quick_check/
Place it next to defects4j_runner.py (same folder).
"""
import os
import sys
import csv
import shutil
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import defects4j_runner as R  # noqa: E402

KEY = ("Model", "Project", "BugID", "TargetClass")
COV_COLS = {"line": "LineCoverage(%)", "branch": "BranchCoverage(%)",
            "lines_total": "LinesTotal", "lines_covered": "LinesCovered",
            "branches_total": "BranchesTotal", "branches_covered": "BranchesCovered"}
LOCK = threading.Lock()


def load_rows():
    with open(R.CSV_PATH, encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))


def save_rows(rows):
    tmp = R.CSV_PATH + ".tmp"
    with open(tmp, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=R.CSV_FIELDS)
        w.writeheader()
        w.writerows(rows)
    os.replace(tmp, R.CSV_PATH)


def needs_coverage(r):
    return r["Status"] in ("PASS", "TEST_FAIL") and r.get("LineCoverage(%)", "NA") == "NA" \
        and r.get("LinesTotal", "NA") == "NA"


def process_bug(project, bug, rows_of_bug, all_rows, base_dir):
    tag = f"{project}_{bug}"
    work_dir = os.path.join(base_dir, tag)
    shutil.rmtree(work_dir, ignore_errors=True)
    try:
        for _ in range(2):
            code, _, err = R.run_cmd(["defects4j", "checkout", "-p", project, "-v", f"{bug}b", "-w", work_dir],
                                     timeout=R.TIMEOUT_CHECKOUT)
            if code == 0:
                break
            shutil.rmtree(work_dir, ignore_errors=True)
        if code != 0:
            R.log(f"[ERROR] Checkout failed {tag}: {err[:150]}")
            return 0

        classes = [c.strip() for c in R.export_d4j_property("classes.modified", work_dir).splitlines() if c.strip()]
        test_src_dir = R.export_d4j_property("dir.src.tests", work_dir)
        by_simple = {c.rpartition(".")[2]: c for c in classes}

        # compile the buggy sources once so 'defects4j coverage' starts from a clean, compiled tree
        R.run_cmd(["defects4j", "compile"], cwd=work_dir, timeout=R.TIMEOUT_COMPILE)

        n = 0
        for r in rows_of_bug:
            full_class = by_simple.get(r["TargetClass"])
            if not full_class:
                R.log(f"[WARN] {tag}: class {r['TargetClass']} not in classes.modified")
                continue
            pkg, _, cls = full_class.rpartition(".")
            test_cls = f"{cls}Test"
            full_test = f"{pkg}.{test_cls}" if pkg else test_cls
            saved = os.path.join(R.OUT_ROOT, r["Model"], "TestCode", project, str(bug), f"{test_cls}.java")
            if not os.path.isfile(saved):
                R.log(f"[WARN] {tag}: saved test missing: {saved}")
                continue

            # put the test into the checkout (same location the runner used)
            dest_dir = os.path.join(work_dir, test_src_dir, pkg.replace(".", "/") if pkg else "")
            os.makedirs(dest_dir, exist_ok=True)
            dest = os.path.join(dest_dir, f"{test_cls}.java")
            shutil.copyfile(saved, dest)

            cov = R.get_defects4j_coverage(work_dir, full_class, full_test, dest)
            with LOCK:
                for k, col in COV_COLS.items():
                    r[col] = cov[k]
                save_rows(all_rows)
            R.log(f"  [COV] [{tag}][{r['Model']}] Line: {cov['line']}% | Branch: {cov['branch']}%")
            n += 1
        return n
    finally:
        shutil.rmtree(work_dir, ignore_errors=True)


def main():
    R.ensure_java11()
    if not os.path.isfile(R.CSV_PATH):
        sys.exit(f"CSV not found: {R.CSV_PATH}")
    rows = load_rows()
    todo = [r for r in rows if needs_coverage(r)]
    R.log(f"[INFO] Rows total={len(rows)} | need coverage={len(todo)}")

    groups = {}
    for r in todo:
        groups.setdefault((r["Project"], r["BugID"]), []).append(r)

    base_dir = os.path.join(R.OUT_ROOT, "defects4j_checkout_cov")
    os.makedirs(base_dir, exist_ok=True)

    done = 0
    ex = ThreadPoolExecutor(max_workers=R.WORKERS)
    try:
        futs = [ex.submit(process_bug, p, b, rs, rows, base_dir) for (p, b), rs in groups.items()]
        for fut in as_completed(futs):
            try:
                fut.result()
            except Exception as e:
                R.log(f"[ERROR] Worker crashed: {type(e).__name__}: {e}")
            done += 1
            R.log(f"[PROGRESS] {done}/{len(futs)} bugs")
    except KeyboardInterrupt:
        R.log("[STOP] Ctrl-C: cancelling queued bugs...")
        ex.shutdown(wait=True, cancel_futures=True)
    else:
        ex.shutdown(wait=True)

    left = sum(1 for r in load_rows() if needs_coverage(r))
    R.log(f"[DONE] Rows still without coverage: {left} (re-run to retry them)")


if __name__ == "__main__":
    main()