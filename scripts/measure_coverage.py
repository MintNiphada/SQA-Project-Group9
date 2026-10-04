#!/usr/bin/env python3
"""
Phase 2: measure coverage for rows in results/benchmark_data.csv.

For every PASS / TEST_FAIL row whose "Line Coverage" is empty or NA:
  checkout buggy version -> take saved test from <Model>/TestCode/<Project>/<Bug>/ -> defects4j coverage
  -> write "Line Coverage" and "Branch Coverage" back to the row.

Usage:
  python3 scripts/measure_coverage.py                                   # all rows that need coverage
  python3 scripts/measure_coverage.py --model DeepSeek --project Chart --bug 10   # one at a time
  python3 scripts/measure_coverage.py --model DeepSeek --project Chart            # one project
  WORKERS=4 python3 scripts/measure_coverage.py

Place next to defects4j_runner.py. Do not run at the same time as the runner.
"""
import os
import sys
import re
import csv
import glob
import shutil
import argparse
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import defects4j_runner as R  # noqa: E402

LINE_COL = "Line Coverage"
BRANCH_COL = "Branch Coverage"
LOCK = threading.Lock()
_FIELDS = []  # real header of the CSV (do not use R.CSV_FIELDS: it is the old schema)


def load_rows():
    global _FIELDS
    with open(R.CSV_PATH, encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        _FIELDS = list(reader.fieldnames or [])
        return list(reader)


def save_rows(rows):
    tmp = R.CSV_PATH + ".tmp"
    with open(tmp, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=_FIELDS)
        w.writeheader()
        w.writerows(rows)
    os.replace(tmp, R.CSV_PATH)


def needs_coverage(r):
    status = str(r.get("Status", "")).strip()
    cov = str(r.get(LINE_COL, "")).strip()
    return status in ("PASS", "TEST_FAIL") and cov in ("", "NA")


def matches_filter(r, args):
    if args.model and r["Model"].lower() != args.model.lower():
        return False
    if args.project and r["Project"].lower() != args.project.lower():
        return False
    if args.bug and str(r["Bug_ID"]).strip() != str(args.bug):
        return False
    return True


def find_saved_tests(model, project, bug, test_name):
    """Return every candidate test file saved for this bug.

    If a file named exactly <test_name>.java exists, return only that one.
    Otherwise return all .java files under <Model>/TestCode/<Project>/<Bug>/.
    """
    d = os.path.join(R.OUT_ROOT, model, "TestCode", project, str(bug))
    exact = os.path.join(d, f"{test_name}.java")
    if os.path.isfile(exact):
        return [exact]
    return sorted(glob.glob(os.path.join(d, "**", "*.java"), recursive=True))


def choose_test(cands, classes):
    """Pick the test file named <ClassUnderTest>Test matching a class in classes.modified.

    Falls back to the only candidate when there is exactly one.
    """
    simple = {c.rpartition(".")[2] for c in classes}
    for f in cands:
        base = os.path.basename(f)[:-5]
        if base.endswith("Test") and base[:-4] in simple:
            return f
    return cands[0] if len(cands) == 1 else None


def read_test_info(path):
    """Return (package, public_class_name) from the saved test file."""
    with open(path, encoding="utf-8", errors="replace") as f:
        src = f.read()
    m_pkg = re.search(r"^\s*package\s+([\w.]+)\s*;", src, re.M)
    m_cls = re.search(r"\bpublic\s+(?:final\s+)?class\s+(\w+)", src)
    return (m_pkg.group(1) if m_pkg else ""), (m_cls.group(1) if m_cls else None)


def pick_target_class(classes, test_cls):
    """Choose the class under test from classes.modified."""
    for c in classes:
        simple = c.rpartition(".")[2]
        if test_cls == f"{simple}Test" or test_cls.startswith(simple):
            return c
    return classes[0] if classes else None


def process_bug(project, bug, rows_of_bug, all_rows, base_dir):
    tag = f"{project}_{bug}"
    work_dir = os.path.join(base_dir, tag)
    shutil.rmtree(work_dir, ignore_errors=True)
    try:
        code, err = 1, ""
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

        R.run_cmd(["defects4j", "compile"], cwd=work_dir, timeout=R.TIMEOUT_COMPILE)

        n = 0
        for r in rows_of_bug:
            cands = find_saved_tests(r["Model"], project, bug, r["Test_Name"])
            saved = choose_test(cands, classes)
            if not saved:
                R.log(f"[WARN] {tag}: saved test not found for {r['Test_Name']} ({r['Model']}) "
                      f"| candidates={len(cands)} | modified={','.join(c.rpartition('.')[2] for c in classes)}")
                continue

            pkg, cls = read_test_info(saved)
            if not cls:
                R.log(f"[WARN] {tag}: cannot find public class in {saved}")
                continue
            full_test = f"{pkg}.{cls}" if pkg else cls

            full_class = pick_target_class(classes, cls)
            if not full_class:
                R.log(f"[WARN] {tag}: classes.modified is empty")
                continue
            if len(classes) > 1:
                R.log(f"[INFO] {tag}: {len(classes)} modified classes, using {full_class}")

            dest_dir = os.path.join(work_dir, test_src_dir, pkg.replace(".", "/") if pkg else "")
            os.makedirs(dest_dir, exist_ok=True)
            dest = os.path.join(dest_dir, f"{cls}.java")
            shutil.copyfile(saved, dest)

            cov = R.get_defects4j_coverage(work_dir, full_class, full_test, dest)
            with LOCK:
                r[LINE_COL] = cov["line"]
                r[BRANCH_COL] = cov["branch"]
                save_rows(all_rows)
            R.log(f"  [COV] [{tag}][{r['Model']}] Line: {cov['line']}% | Branch: {cov['branch']}%")
            n += 1
        return n
    finally:
        shutil.rmtree(work_dir, ignore_errors=True)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--model", help="e.g. DeepSeek")
    ap.add_argument("--project", help="e.g. Chart")
    ap.add_argument("--bug", help="e.g. 10")
    args = ap.parse_args()

    if not os.path.isfile(R.CSV_PATH):
        sys.exit(f"CSV not found: {R.CSV_PATH}")
    rows = load_rows()
    for col in (LINE_COL, BRANCH_COL):
        if col not in _FIELDS:
            sys.exit(f"Column '{col}' not found in {R.CSV_PATH}. Header = {_FIELDS}")

    todo = [r for r in rows if needs_coverage(r) and matches_filter(r, args)]
    R.log(f"[INFO] Rows total={len(rows)} | need coverage={len(todo)}")

    groups = {}
    for r in todo:
        groups.setdefault((r["Project"], r["Bug_ID"]), []).append(r)

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

    left = sum(1 for r in load_rows() if needs_coverage(r) and matches_filter(r, args))
    R.log(f"[DONE] Rows still without coverage: {left} (re-run to retry them)")


if __name__ == "__main__":
    main()