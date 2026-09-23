"""
Shared Defects4J helpers for Group 9 benchmark.

Environment contract (see BENCHMARK_PROTOCOL.md):
  - Java 11            -> Defects4J requires it
  - Strawberry perl    -> defects4j CLI is a perl script; MUST NOT be MSYS2/Git perl
  - Git\\usr\\bin       -> wc, tar, bzip2, head, awk, ... (GNU tools D4J shells out to)
  - Git\\bin            -> bash.exe (Git Bash, NOT WSL bash)
Order matters (first match wins in PATH).  WSL bash (C:\\WINDOWS\\system32\\bash.exe)
must never win the lookup for `bash` because Defects4J's ant.cmd calls
`bash -c` with /d/... paths that only Git Bash understands.  Likewise system32
tar.exe has no bzip2 so it can't extract .tar.bz2.
"""
from __future__ import annotations

import csv
import os
import re
import shutil
import subprocess
import tarfile
import tempfile
from dataclasses import dataclass
from pathlib import Path

# --- Paths (adjust to your machine) ---
JAVA11 = Path(r"C:\Program Files\Eclipse Adoptium\jdk-11.0.32.101-hotspot")
STRAWBERRY_PERL_BIN = Path(r"C:\Strawberry\perl\bin")
GIT_BIN = Path(r"C:\Program Files\Git\bin")
GIT_USR_BIN = Path(r"C:\Program Files\Git\usr\bin")
D4J_HOME = Path(r"D:\Lab_SQA\defects4j")
D4J_DEFECTS4J = D4J_HOME / "framework" / "bin" / "defects4j"
PROJECTS_DIR = Path(r"D:\Lab_SQA\Defects4J_Projects")

RESULTS_CSV = Path(__file__).resolve().parent.parent / "results" / "benchmark_results.csv"
RESULTS_CSV.parent.mkdir(parents=True, exist_ok=True)


def build_env(path_prepend: list[Path] | None = None) -> dict:
    """PATH with Java 11, Strawberry perl, Git usr/bin, Git bin first; D4J vars set."""
    env = os.environ.copy()
    extra = path_prepend or []
    env["PATH"] = ";".join(
        [str(p) for p in extra]
        + [
            str(JAVA11 / "bin"),
            str(STRAWBERRY_PERL_BIN),
            str(GIT_USR_BIN),
            str(GIT_BIN),
            env.get("PATH", ""),
        ]
    )
    env["JAVA_HOME"] = str(JAVA11)
    env["D4J_HOME"] = str(D4J_HOME)
    env["TZ"] = "America/Los_Angeles"
    return env


def run_cmd(cmd: list[str], cwd: Path | None = None, timeout: int = 1800,
            extra_path: list[Path] | None = None) -> subprocess.CompletedProcess:
    return subprocess.run(
        cmd,
        cwd=str(cwd) if cwd else None,
        env=build_env(extra_path),
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        timeout=timeout,
    )


def defects4j(args: list[str], cwd: Path | None = None, timeout: int = 1800) -> subprocess.CompletedProcess:
    """Invoke the defects4j CLI (perl) with the standard env."""
    return run_cmd(["perl", str(D4J_DEFECTS4J)] + args, cwd=cwd, timeout=timeout)


def workdir(project: str, bug: int, version: str) -> Path:
    """e.g. Lang_1_b (buggy) / Lang_1_f (fixed)."""
    return PROJECTS_DIR / f"{project}_{bug}_{version}"


def checkout(project: str, bug: int, version: str) -> Path:
    """
    Checkout (or reuse) a Defects4J version.  A directory is only considered
    complete when both `.defects4j.config` and `defects4j.build.properties`
    exist (the latter is written last by Defects4J); anything left over from an
    interrupted checkout is deleted and re-created.
    """
    w = workdir(project, bug, version)
    cfg = w / ".defects4j.config"
    props = w / "defects4j.build.properties"
    if cfg.exists() and props.exists():
        return w

    if w.exists():
        shutil.rmtree(w, ignore_errors=True)
    w.parent.mkdir(parents=True, exist_ok=True)
    res = defects4j(["checkout", "-p", project, "-v", f"{bug}{version}", "-w", str(w)])
    if res.returncode != 0 or not cfg.exists():
        raise RuntimeError(
            f"checkout {project}-{bug}{version} failed:\n{res.stdout}\n{res.stderr}"
        )
    return w


def compile_version(w: Path) -> None:
    res = defects4j(["compile", "-w", str(w)])
    if res.returncode != 0:
        raise RuntimeError(f"compile {w} failed:\n{res.stdout}\n{res.stderr}")


@dataclass
class BugInfo:
    project: str
    bug: int
    modified_classes: list[str]
    relevant_classes: list[str]
    trigger_tests: list[str]


def read_bug_info(project: str, bug: int, version: str = "b") -> BugInfo:
    w = checkout(project, bug, version)
    props = w / "defects4j.build.properties"
    data: dict[str, str] = {}
    if props.exists():
        for line in props.read_text(encoding="utf-8", errors="replace").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            data[k.strip()] = v.strip()
    modified = [c for c in data.get("d4j.classes.modified", "").split(",") if c]
    relevant = [c for c in data.get("d4j.classes.relevant", "").split(",") if c]
    trigger = data.get("d4j.tests.trigger", "")
    triggers = [t for t in trigger.split(",") if t]
    return BugInfo(project, bug, modified, relevant, triggers)


def build_test_archive(test_sources: list[Path], archive_path: Path,
                       project: str, bug: int, version: str) -> Path:
    """
    Pack .java test sources into a Defects4J external-test-suite archive.

    Layout: each file is placed so its `package` declaration matches the
    directory path inside the archive (e.g. org/apache/.../FooTest.java).
    """
    archive_path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        tmp_root = Path(tmp)
        for src in test_sources:
            pkg = _java_package(src)
            rel_dir = tmp_root / Path(*pkg.split(".")) if pkg else tmp_root
            rel_dir.mkdir(parents=True, exist_ok=True)
            target = rel_dir / src.name
            target.write_bytes(src.read_bytes())
        with tarfile.open(archive_path, "w:bz2") as tar:
            for p in tmp_root.rglob("*.java"):
                tar.add(p, arcname=str(p.relative_to(tmp_root)).replace("\\", "/"))
    return archive_path


def _java_package(java_file: Path) -> str:
    for line in java_file.read_text(encoding="utf-8", errors="replace").splitlines():
        s = line.strip()
        if s.startswith("package"):
            body = s[len("package"):].rstrip(";").strip()
            return body
    return ""


def count_tests(java_files: list[Path]) -> int:
    """Rough count of @Test methods across the given .java files."""
    total = 0
    pat = re.compile(r"^\s*@Test", re.MULTILINE)
    for f in java_files:
        total += len(pat.findall(f.read_text(encoding="utf-8", errors="replace")))
    return total


@dataclass
class TestRunResult:
    failing_on_buggy: int
    failing_on_fixed: int
    lines_total: int
    lines_covered: int
    branches_total: int
    branches_covered: int

    @property
    def line_cov_pct(self) -> float:
        return (self.lines_covered / self.lines_total * 100) if self.lines_total else 0.0

    @property
    def branch_cov_pct(self) -> float:
        return (self.branches_covered / self.branches_total * 100) if self.branches_total else 0.0

    @property
    def fault_detected(self) -> bool:
        return self.failing_on_buggy > 0 and self.failing_on_fixed == 0


def _parse_failing_tests(w: Path) -> int:
    """
    Count failing test entries in <work>/failing_tests (0 if file missing/empty).

    Defects4J's JUnit formatter writes one block per failing test, each block
    starting with a marker line:
        --- org.apache.commons.lang3.math.NumberUtilsTest::TestLang747
        java.lang.NumberFormatException: ...
            at ...
    So the reliable per-test delimiter is a line beginning with "--- ".
    """
    ft = w / "failing_tests"
    if not ft.exists():
        return 0
    text = ft.read_text(encoding="utf-8", errors="replace")
    if not text.strip():
        return 0
    markers = re.findall(r"^\s*---\s+[\w.$]+::\w+", text, flags=re.MULTILINE)
    if markers:
        return len(markers)
    tokens = re.findall(r"([\w.$]+::\w+)", text)
    if tokens:
        return len(set(tokens))
    return 1


def _test_source_root(w: Path) -> Path:
    """Test source dir of a checked-out version (default src/test/java)."""
    props = w / "defects4j.build.properties"
    if props.exists():
        for line in props.read_text(encoding="utf-8", errors="replace").splitlines():
            s = line.strip()
            if s.startswith("d4j.dir.src.tests="):
                rel = s.split("=", 1)[1].strip()
                return w / rel
    return w / "src" / "test" / "java"


def _test_ids_from_archive_native(w: Path, archive: Path) -> list[tuple[str, str]]:
    """
    Extract an external-suite .tar.bz2 into the workdir's test source dir.

    This REPLACES `defects4j coverage/test -s <archive>` whose
    `Utils::extract_test_suite` fails on Windows (it shells out to
    `mkdir -p ... && rm -rf ... && tar -xjf ...` which the Windows shell
    mangles: "mkdir: unknown option -- r").  Python's native tarfile handles
    the package layout without any shell, so the suite actually lands in
    src/test/java/<pkg>, gets compiled + run by Defects4J.

    Returns [(fully_qualified_class, method), ...] for every @Test found.
    """
    test_root = _test_source_root(w)
    test_root.mkdir(parents=True, exist_ok=True)

    test_ids: list[tuple[str, str]] = []
    with tarfile.open(archive, "r:bz2") as tar:
        for member in tar.getmembers():
            if not member.isfile() or not member.name.endswith(".java"):
                continue
            data = tar.extractfile(member).read()

            # Place preserving the package layout already baked into the archive.
            dest = test_root / Path(member.name.replace("\\", "/"))
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)

            text = data.decode("utf-8", errors="replace")
            pkg = ""
            m_pkg = re.search(r"^\s*package\s+([\w.]+)\s*;", text, flags=re.MULTILINE)
            if m_pkg:
                pkg = m_pkg.group(1)
            cls_name = member.name.replace("\\", "/").rstrip("/").split("/")[-1].replace(".java", "")
            fqcn = f"{pkg}.{cls_name}" if pkg else cls_name
            for m_method in re.finditer(r"@Test\b[^\{]*?\bvoid\s+(\w+)\s*\(", text, flags=re.DOTALL):
                test_ids.append((fqcn, m_method.group(1)))
    return test_ids


def run_external_suite(project: str, bug: int, version: str, archive: Path) -> TestRunResult:
    """
    Run a generated external suite on the given version (b/f).

    Bypass (used here): `defects4j coverage/test -s` is unreliable on Windows
    (the suite never extracts/compiles/runs -> bogus 0% / no fault detection),
    so we extract the archive with python tarfile into the workdir's test source
    dir and then drive Defects4J per test method (`defects4j test/coverage -t
    <Class>::<method>`), aggregating failing tests and line/branch coverage.
    """
    w = checkout(project, bug, version)
    compile_version(w)

    test_ids = _test_ids_from_archive_native(w, archive)
    if not test_ids:
        raise RuntimeError(f"no @Test methods extracted from {archive}")

    failing = 0
    lines_total = lines_cov = branches_total = branches_cov = 0
    ran_coverage = False

    for fqcn, method in test_ids:
        tid = f"{fqcn}::{method}"

        # --- Fault detection: run the single test ---
        res = defects4j(["test", "-w", str(w), "-t", tid])
        failing += _parse_failing_tests(w)

        # --- Coverage: run coverage for the same single test ---
        res_cov = defects4j(["coverage", "-w", str(w), "-t", tid])
        ran_coverage = True

        summary = w / "summary.csv"
        if summary.exists():
            with summary.open(newline="", encoding="utf-8") as fh:
                reader = csv.DictReader(fh)
                for row in reader:
                    lines_total = max(lines_total, int(row.get("LinesTotal", 0) or 0))
                    lines_cov = max(lines_cov, int(row.get("LinesCovered", 0) or 0))
                    branches_total = max(branches_total, int(row.get("ConditionsTotal", 0) or 0))
                    branches_cov = max(branches_cov, int(row.get("ConditionsCovered", 0) or 0))
                    break
        else:
            import re as _re
            out = res_cov.stdout + res_cov.stderr
            m = _re.search(r"Lines total:\s*(\d+)", out)
            if m:
                lines_total = max(lines_total, int(m.group(1)))
            m = _re.search(r"Lines covered:\s*(\d+)", out)
            if m:
                lines_cov = max(lines_cov, int(m.group(1)))
            m = _re.search(r"Conditions total:\s*(\d+)", out)
            if m:
                branches_total = max(branches_total, int(m.group(1)))
            m = _re.search(r"Conditions covered:\s*(\d+)", out)
            if m:
                branches_cov = max(branches_cov, int(m.group(1)))

    if not ran_coverage:
        raise RuntimeError("coverage never ran")

    return TestRunResult(
        failing_on_buggy=failing if version == "b" else 0,
        failing_on_fixed=failing if version == "f" else 0,
        lines_total=lines_total,
        lines_covered=lines_cov,
        branches_total=branches_total,
        branches_covered=branches_cov,
    )


RESULT_FIELDS = [
    "project", "bug", "tool", "budget", "repetition",
    "test_count", "compile_ok",
    "failing_on_buggy", "failing_on_fixed", "fault_detected",
    "lines_total", "lines_covered", "line_cov_pct",
    "branches_total", "branches_covered", "branch_cov_pct",
    "generation_time_sec", "notes",
]


def append_result(row: dict) -> None:
    write_header = not RESULTS_CSV.exists()
    with RESULTS_CSV.open("a", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=RESULT_FIELDS, extrasaction="ignore")
        if write_header:
            writer.writeheader()
        writer.writerow(row)


def has_result(project: str, bug: int, tool: str, budget: str, rep: int) -> bool:
    """Resume support: skip work that already has a row in the results CSV."""
    if not RESULTS_CSV.exists():
        return False
    with RESULTS_CSV.open(newline="", encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            if (
                row.get("project") == str(project)
                and row.get("bug") == str(bug)
                and row.get("tool") == tool
                and row.get("budget") == budget
                and row.get("repetition") == str(rep)
            ):
                return True
    return False
