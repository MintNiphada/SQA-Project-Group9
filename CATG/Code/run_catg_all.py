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
import os
import shutil
import subprocess
import sys
import time
from pathlib import Path, PureWindowsPath

SCRIPT_DIR = Path(__file__).resolve().parent
ROOT = SCRIPT_DIR.parent.parent
SCRIPTS_DIR = ROOT / "scripts"
BASELINE_RESULTS_CSV = ROOT / "CATG" / "Result_Round2" / "benchmark_results.csv"
DEFAULT_CATG_RESULTS_DIR = ROOT / "CATG" / "Result_Round2" / "enhanced"
CATG_RESULTS_DIR = Path(os.environ.get("CATG_RESULT_DIR", str(DEFAULT_CATG_RESULTS_DIR)))
CATG_RESULTS_CSV = CATG_RESULTS_DIR / "benchmark_results.csv"
if CATG_RESULTS_CSV.resolve() == BASELINE_RESULTS_CSV.resolve():
    raise RuntimeError(
        "CATG_RESULT_DIR resolves to the read-only Baseline; "
        "use CATG/Result_Round2/enhanced instead"
    )
os.environ["CATG_RESULT_DIR"] = str(CATG_RESULTS_DIR)
os.environ["SQA_RESULTS_CSV"] = str(CATG_RESULTS_CSV)
RESULTS_CSV = CATG_RESULTS_CSV
DEFAULT_MANIFEST = ROOT / "results" / "defects4j_bugs.csv"
RUN_EXPERIMENT = SCRIPTS_DIR / "run_experiment.py"
MAIN_CLASSES = {"Lang": "tests.test_Lang1"}

if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))
from d4j_helpers import checkout_at, compile_version, result_is_terminal
from catg_auto import run_batch as run_auto_batch


def _find_git_bash() -> Path:
    candidates: list[Path] = []
    configured = os.environ.get("GIT_BASH")
    if configured:
        candidates.append(Path(configured))
        resolved = shutil.which(configured)
        if resolved:
            candidates.append(Path(resolved))
    candidates.extend([
        Path(r"C:\Program Files\Git\bin\bash.exe"),
        Path(r"C:\Program Files\Git\usr\bin\bash.exe"),
        Path(r"C:\Program Files (x86)\Git\bin\bash.exe"),
        Path("/usr/bin/bash"),
    ])
    git = shutil.which("git")
    if git:
        git_root = Path(git).resolve().parent.parent
        candidates.extend([
            git_root / "bin" / "bash.exe",
            git_root / "usr" / "bin" / "bash.exe",
        ])
    seen: set[Path] = set()
    for candidate in candidates:
        candidate = candidate.expanduser()
        if candidate in seen:
            continue
        seen.add(candidate)
        if candidate.is_file() and "system32" not in str(candidate).lower():
            return candidate
    raise FileNotFoundError("Git Bash not found; set GIT_BASH to bash.exe")


def _git_bash_path(path: Path) -> str:
    value = os.fspath(path).replace("\\", "/")
    if len(value) >= 3 and value[0] == "/" and value[1].isalpha() and value[2] == "/":
        return value
    if os.name == "nt":
        windows_path = PureWindowsPath(value)
        if not windows_path.is_absolute():
            windows_path = PureWindowsPath(Path(value).resolve())
        value = windows_path.as_posix()
        if len(value) >= 2 and value[1] == ":":
            return f"/{value[0].lower()}/{value[3:]}"
        if value.startswith("//"):
            return value
    return Path(value).resolve().as_posix().replace("\\", "/")


def _existing_rows(path: Path) -> set[tuple[str, str, str, str, str]]:
    if not path.exists():
        return set()
    rows: set[tuple[str, str, str, str, str]] = set()
    with path.open(newline="", encoding="utf-8-sig") as fh:
        for row in csv.DictReader(fh):
            if not result_is_terminal(row):
                continue
            rows.add((
                row.get("project", ""),
                row.get("bug", ""),
                row.get("tool", ""),
                row.get("budget", ""),
                row.get("repetition", ""),
            ))
    return rows


def _checkout(project: str, bug: int) -> Path:
    workdir = ROOT / "CATG" / "Workdirs" / f"{project}_{bug}_b"
    return checkout_at(project, bug, "b", workdir)


def _run_catg(wd: Path, project: str, bug: int, budget: str = "default") -> Path:
    out_dir = ROOT / "CATG" / "Result_Round2"
    out_dir.mkdir(parents=True, exist_ok=True)
    inputs_file = out_dir / f"{project}-{bug}_generated_inputs.txt"
    inputs_file.unlink(missing_ok=True)

    dconcolic = ROOT / "CATG" / "Code" / "dconcolic"
    if not dconcolic.is_file():
        raise FileNotFoundError(f"missing CATG launcher: {dconcolic}")
    if project not in MAIN_CLASSES:
        raise ValueError(f"no CATG harness configured for project: {project}")

    subprocess.run(
        [
            str(_find_git_bash()),
            _git_bash_path(dconcolic),
            "--budget", budget,
            "--out", _git_bash_path(inputs_file),
            "--workdir", _git_bash_path(wd),
            "--project", project,
            "--main-class", MAIN_CLASSES[project],
        ],
        check=True,
    )
    if not inputs_file.is_file() or inputs_file.stat().st_size == 0:
        raise FileNotFoundError(f"CATG produced no inputs file: {inputs_file}")
    return inputs_file


def _to_junit(project: str, bug: int, inputs_file: Path,
              target_method: str) -> Path:
    """Run catg_to_junit adapter -> JUnit suite dir for the bug."""
    out_dir = CATG_RESULTS_DIR / f"{project}_{bug}" / "test-suite"
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


def _run_experiment(project: str, bug: int, test_dir: Path, budget: str,
                    force: bool) -> None:
    cmd = [
        sys.executable,
        str(RUN_EXPERIMENT),
        "--project", project,
        "--bug", str(bug),
        "--tool", "CATG",
        "--budget", budget,
        "--rep", "1",
        "--test-dir", str(test_dir),
    ]
    if force:
        cmd.append("--force")
    subprocess.run(cmd, check=True)


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    p.add_argument("--manifest", type=Path,
                   help="CSV with project,bug[,budget][,target_method]")
    p.add_argument("--projects", help="comma list, e.g. Lang,Math")
    p.add_argument("--budget", default="default")
    p.add_argument("--dry-run", action="store_true",
                   help="print the plan (project,bug -> suite -> pipeline) without running")
    p.add_argument("--force", action="store_true")
    p.add_argument("--limit", type=int, help="process only first N bugs (pilot)")
    p.add_argument("--legacy", action="store_true", help="use the original Lang-only driver")
    args = p.parse_args()

    if not args.legacy:
        manifest = args.manifest or DEFAULT_MANIFEST
        return run_auto_batch(manifest, args.projects, args.budget, args.limit, args.force, args.dry_run)

    if args.manifest:
        rows = []
        with args.manifest.open(newline="", encoding="utf-8-sig") as fh:
            rows.extend(csv.DictReader(fh))
    elif args.projects:
        rows = []
        for project in args.projects.split(","):
            project = project.strip()
            for bug in _list_defects4j_bugs(project):
                rows.append({"project": project, "bug": str(bug), "budget": args.budget})
    else:
        p.error("need --manifest or --projects")

    if args.limit is not None:
        if args.limit < 0:
            p.error("--limit must be non-negative")
        rows = rows[: args.limit]

    done = _existing_rows(RESULTS_CSV)
    failures: list[str] = []
    processed = 0
    start = time.time()

    for index, row in enumerate(rows, 1):
        project = (row.get("project") or "").strip()
        bug_text = (row.get("bug") or "").strip()
        try:
            bug = int(bug_text)
        except ValueError:
            failures.append(f"row {index}: invalid bug {bug_text!r}")
            print(f"[ERR]  row {index}: invalid bug {bug_text!r}", file=sys.stderr)
            continue

        budget = (row.get("budget") or args.budget).strip()
        target_method = (row.get("target_method") or _default_target(project)).strip()
        result_key = (project, str(bug), "CATG", budget, "1")
        label = f"{project}-{bug}"

        if project not in MAIN_CLASSES:
            message = f"no CATG harness configured for project: {project}"
            failures.append(f"{label}: {message}")
            print(f"[ERR]  {label}: {message}", file=sys.stderr)
            continue
        if args.dry_run:
            print(f"[plan] {label} budget={budget} main={MAIN_CLASSES[project]}")
            continue
        if not args.force and result_key in done:
            print(f"[skip] {label} already in results")
            continue

        started = time.time()
        try:
            workdir = _checkout(project, bug)
            compile_version(workdir)
            inputs_file = _run_catg(workdir, project, bug, budget)
            suite = _to_junit(project, bug, inputs_file, target_method)
            _run_experiment(project, bug, suite, budget, args.force)
            done.add(result_key)
            processed += 1
            print(f"[ok]   {label}: {time.time()-started:.1f}s "
                  f"(inputs={inputs_file.name}, suite={suite.name})")
        except Exception as exc:
            failures.append(f"{label}: {exc}")
            print(f"[ERR]  {label}: {exc}", file=sys.stderr)

    print(f"\n[done] {processed}/{len(rows)} completed, {len(failures)} failed "
          f"in {time.time()-start:.1f}s")
    return 1 if failures else 0


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
    targets = {
        "Lang": "org.apache.commons.lang3.math.NumberUtils::createNumber",
    }
    return targets.get(project, "")


if __name__ == "__main__":
    raise SystemExit(main())
