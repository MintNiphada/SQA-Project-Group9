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
import stat
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
PERL_EXE = STRAWBERRY_PERL_BIN / "perl.exe"
PROJECTS_DIR = Path(r"D:\Lab_SQA\Defects4J_Projects")

RESULTS_CSV = Path(os.environ.get(
    "SQA_RESULTS_CSV",
    str(Path(__file__).resolve().parent.parent / "results" / "benchmark_results.csv"),
))
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
    for key in ("JAVA_TOOL_OPTIONS", "ANT_OPTS"):
        value = env.get(key, "")
        if "-Dfile.encoding=UTF-8" not in value:
            env[key] = f"{value} -Dfile.encoding=UTF-8".strip()
    return env


def run_cmd(cmd: list[str], cwd: Path | None = None, timeout: int = 1800,
            extra_path: list[Path] | None = None,
            env_overrides: dict[str, str] | None = None) -> subprocess.CompletedProcess:
    env = build_env(extra_path)
    if env_overrides:
        env.update(env_overrides)
    return subprocess.run(
        cmd,
        cwd=str(cwd) if cwd else None,
        env=env,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        timeout=timeout,
    )


def defects4j(args: list[str], cwd: Path | None = None, timeout: int = 1800,
              env_overrides: dict[str, str] | None = None) -> subprocess.CompletedProcess:
    """Invoke the defects4j CLI (perl) with the standard env."""
    return run_cmd([str(PERL_EXE), str(D4J_DEFECTS4J)] + args, cwd=cwd, timeout=timeout,
                   env_overrides=env_overrides)


def export_classpath(w: Path, property_name: str = "cp.compile") -> str:
    build_file = D4J_HOME / "framework" / "projects" / "defects4j.build.xml"
    ant = D4J_HOME / "major" / "bin" / "ant.cmd"
    output = w / f".catg-export-{property_name.replace('.', '-')}"
    args = [
        str(ant),
        "-f", str(build_file),
        f"-Dd4j.home={D4J_HOME}",
        f"-Dd4j.dir.projects={D4J_HOME / 'framework' / 'projects'}",
        f"-Dbasedir={w}",
        f"-Dfile.export={output}",
        f"export.{property_name}",
    ]
    res = run_cmd(args, cwd=w, extra_path=[ant.parent])
    if res.returncode != 0 or not output.exists():
        detail = (res.stdout + res.stderr).strip()
        raise RuntimeError(
            f"export {property_name} failed for {w}: exit={res.returncode}\n{detail}"
        )
    try:
        value = output.read_text(encoding="utf-8", errors="replace").strip()
    finally:
        output.unlink(missing_ok=True)
    if not value:
        raise RuntimeError(f"export {property_name} returned an empty value for {w}")
    return value


def workdir(project: str, bug: int, version: str) -> Path:
    """e.g. Lang_1_b (buggy) / Lang_1_f (fixed)."""
    return PROJECTS_DIR / f"{project}_{bug}_{version}"


def _remove_checkout_tree(path: Path) -> None:
    def retry(func, target, _exc):
        os.chmod(target, stat.S_IWRITE)
        func(target)

    if path.exists():
        shutil.rmtree(path, onerror=retry)


def _checkout_matches(w: Path, project: str, bug: int, version: str) -> bool:
    cfg = w / ".defects4j.config"
    props = w / "defects4j.build.properties"
    if not cfg.exists() or not props.exists():
        return False
    values: dict[str, str] = {}
    for line in cfg.read_text(encoding="utf-8", errors="replace").splitlines():
        if "=" in line:
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
    return values.get("pid") == project and values.get("vid") == f"{bug}{version}"


def checkout_at(project: str, bug: int, version: str, destination: Path) -> Path:
    w = Path(destination)
    cfg = w / ".defects4j.config"
    props = w / "defects4j.build.properties"
    if _checkout_matches(w, project, bug, version):
        return w

    _remove_checkout_tree(w)
    w.parent.mkdir(parents=True, exist_ok=True)
    res = defects4j(["checkout", "-p", project, "-v", f"{bug}{version}", "-w", str(w)])
    if res.returncode != 0 or not cfg.exists() or not props.exists():
        detail = (res.stdout + res.stderr).strip()
        raise RuntimeError(
            f"checkout {project}-{bug}{version} failed at {w}: "
            f"exit={res.returncode}\n{detail}"
        )
    return w


def checkout(project: str, bug: int, version: str) -> Path:
    return checkout_at(project, bug, version, workdir(project, bug, version))


def cleanup_generated_sources(w: Path) -> None:
    for pattern in ("CATG_*Test.java", "CATG_CoverageSuite_*.java"):
        for path in w.rglob(pattern):
            path.unlink(missing_ok=True)


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


def _test_ids_from_archive_native(
    w: Path,
    archive: Path,
    extracted: list[Path] | None = None,
) -> list[tuple[str, str]]:
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
            member_path = Path(member.name.replace("\\", "/"))
            if member_path.is_absolute() or ".." in member_path.parts:
                raise RuntimeError(f"unsafe test archive member: {member.name}")
            data = tar.extractfile(member).read()

            # Place preserving the package layout already baked into the archive.
            dest = (test_root / member_path).resolve()
            if test_root.resolve() not in dest.parents:
                raise RuntimeError(f"unsafe test archive member: {member.name}")
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)
            if extracted is not None:
                extracted.append(dest)

            text = data.decode("utf-8", errors="replace")
            pkg = ""
            m_pkg = re.search(r"^\s*package\s+([\w.]+)\s*;", text, flags=re.MULTILINE)
            if m_pkg:
                pkg = m_pkg.group(1)
            cls_name = member_path.stem
            fqcn = f"{pkg}.{cls_name}" if pkg else cls_name
            for m_method in re.finditer(r"@Test\b[^\{]*?\bvoid\s+(\w+)\s*\(", text, flags=re.DOTALL):
                test_ids.append((fqcn, m_method.group(1)))
    return test_ids


def _coverage_values(w: Path, result: subprocess.CompletedProcess) -> tuple[int, int, int, int] | None:
    summary = w / "summary.csv"
    if summary.exists():
        with summary.open(newline="", encoding="utf-8") as fh:
            row = next(csv.DictReader(fh), None)
        if row is not None:
            return tuple(int(row.get(key, 0) or 0) for key in (
                "LinesTotal", "LinesCovered", "ConditionsTotal", "ConditionsCovered"
            ))
    output = result.stdout + result.stderr
    values: list[int] = []
    for label in ("Lines total", "Lines covered", "Conditions total", "Conditions covered"):
        match = re.search(rf"{re.escape(label)}:\s*(\d+)", output)
        values.append(int(match.group(1)) if match else 0)
    return tuple(values) if any(values) or "Lines total:" in output else None


def _write_coverage_suite(w: Path, test_ids: list[tuple[str, str]], name: str) -> str:
    test_root = _test_source_root(w)
    suite_dir = test_root / "catg" / "generated"
    suite_dir.mkdir(parents=True, exist_ok=True)
    suite_name = re.sub(r"[^A-Za-z0-9_$]", "_", name)
    class_name = f"catg.generated.{suite_name}"
    classes = list(dict.fromkeys(fqcn for fqcn, _ in test_ids))
    source_lines = [
        "package catg.generated;",
        "import org.junit.Test;",
        "import org.junit.runner.JUnitCore;",
        f"public class {suite_name} {{",
        "    @Test(timeout=120000)",
        "    public void runAll() throws Exception {",
        "        Class<?>[] classes = new Class<?>[] {",
    ]
    for fqcn in classes:
        source_lines.append(f"            {fqcn}.class,")
    source_lines.extend([
        "        };",
        "        JUnitCore.runClasses(classes);",
        "    }",
        "}",
    ])
    (suite_dir / f"{suite_name}.java").write_text("\n".join(source_lines) + "\n", encoding="utf-8")
    return f"{class_name}::runAll"


def run_external_suite(project: str, bug: int, version: str, archive: Path) -> TestRunResult:
    w = checkout(project, bug, version)
    cleanup_generated_sources(w)
    extracted: list[Path] = []
    suite_source: Path | None = None
    try:
        test_ids = _test_ids_from_archive_native(w, archive, extracted)
        if not test_ids:
            raise RuntimeError(f"no @Test methods extracted from {archive}")

        suite_name = f"CATG_CoverageSuite_{project}_{bug}"
        suite_id = _write_coverage_suite(w, test_ids, suite_name)
        suite_source = _test_source_root(w) / "catg" / "generated" / f"{suite_name}.java"
        compile_version(w)

        clean_runner_env = {"JAVA_TOOL_OPTIONS": "", "ANT_OPTS": ""}
        failing = 0
        for fqcn, method in test_ids:
            tid = f"{fqcn}::{method}"
            failing_file = w / "failing_tests"
            failing_file.unlink(missing_ok=True)
            res = defects4j(["test", "-w", str(w), "-t", tid], env_overrides=clean_runner_env)
            failing_count = _parse_failing_tests(w)
            if res.returncode != 0 and failing_count == 0:
                detail = (res.stdout + res.stderr).strip()
                raise RuntimeError(f"test {tid} failed without test results:\n{detail}")
            failing += failing_count

        (w / "failing_tests").unlink(missing_ok=True)
        (w / "summary.csv").unlink(missing_ok=True)
        res_cov = defects4j(["coverage", "-w", str(w), "-t", suite_id], env_overrides=clean_runner_env)
        if res_cov.returncode != 0:
            detail = (res_cov.stdout + res_cov.stderr).strip()
            raise RuntimeError(f"coverage {suite_id} failed:\n{detail}")
        coverage = _coverage_values(w, res_cov)
        if coverage is None:
            raise RuntimeError(f"coverage {suite_id} produced no metrics")
        lines_total, lines_cov, branches_total, branches_cov = coverage
        return TestRunResult(
            failing_on_buggy=failing if version == "b" else 0,
            failing_on_fixed=failing if version == "f" else 0,
            lines_total=lines_total,
            lines_covered=lines_cov,
            branches_total=branches_total,
            branches_covered=branches_cov,
        )
    finally:
        for source in extracted:
            source.unlink(missing_ok=True)
            for compiled in w.rglob(f"{source.stem}.class"):
                compiled.unlink(missing_ok=True)
        if suite_source is not None:
            suite_source.unlink(missing_ok=True)
            for compiled in w.rglob(f"{suite_source.stem}.class"):
                compiled.unlink(missing_ok=True)


RESULT_FIELDS = [
    "project", "bug", "tool", "budget", "repetition",
    "test_count", "compile_ok", "status",
    "failing_on_buggy", "failing_on_fixed", "fault_detected",
    "lines_total", "lines_covered", "line_cov_pct",
    "branches_total", "branches_covered", "branch_cov_pct",
    "generation_time_sec", "notes",
]


def _result_key(row: dict) -> tuple[str, str, str, str, str]:
    return tuple(str(row.get(field, "")).strip() for field in (
        "project", "bug", "tool", "budget", "repetition"
    ))


def result_is_terminal(row: dict) -> bool:
    status = str(row.get("status", "")).strip().lower()
    if status in {"completed", "unsupported"}:
        return True
    if status == "error":
        return False
    compile_ok = str(row.get("compile_ok", "")).strip().lower()
    if compile_ok in {"true", "1", "yes"}:
        return True
    notes = str(row.get("notes", "")).strip()
    return notes.startswith("STATUS:") and not notes.startswith("STATUS:ERROR")


def append_result(row: dict) -> None:
    normalized = {field: row.get(field, "") for field in RESULT_FIELDS}
    if not normalized["status"]:
        normalized["status"] = "completed" if normalized["compile_ok"] else (
            "error" if str(normalized["notes"]).startswith("STATUS:ERROR") else "unsupported"
        )
    key = _result_key(normalized)
    if not RESULTS_CSV.exists():
        with RESULTS_CSV.open("w", newline="", encoding="utf-8") as fh:
            writer = csv.DictWriter(fh, fieldnames=RESULT_FIELDS, extrasaction="ignore")
            writer.writeheader()
            writer.writerow(normalized)
        return

    with RESULTS_CSV.open(newline="", encoding="utf-8-sig") as fh:
        reader = csv.DictReader(fh)
        fieldnames = list(dict.fromkeys([*(reader.fieldnames or []), *RESULT_FIELDS]))
        existing = list(reader)
    for item in existing:
        if not item.get("status"):
            item["status"] = "completed" if str(item.get("compile_ok", "")).lower() == "true" else (
                "error" if str(item.get("notes", "")).startswith("STATUS:ERROR") else "unsupported"
            )
    matching = [item for item in existing if _result_key(item) == key]
    kept: list[dict] = []
    replaced = False
    for item in existing:
        if _result_key(item) != key:
            kept.append(item)
        elif not replaced:
            kept.append(normalized)
            replaced = True
    if not matching:
        kept.append(normalized)

    fd, temp_name = tempfile.mkstemp(prefix=".benchmark-", suffix=".tmp", dir=RESULTS_CSV.parent)
    os.close(fd)
    try:
        with open(temp_name, "w", newline="", encoding="utf-8") as fh:
            writer = csv.DictWriter(fh, fieldnames=fieldnames, extrasaction="ignore")
            writer.writeheader()
            writer.writerows(kept)
        os.replace(temp_name, RESULTS_CSV)
    finally:
        if os.path.exists(temp_name):
            os.unlink(temp_name)


def has_result(project: str, bug: int, tool: str, budget: str, rep: int) -> bool:
    """Return whether a run is complete or a terminal unsupported status exists."""
    if not RESULTS_CSV.exists():
        return False
    with RESULTS_CSV.open(newline="", encoding="utf-8-sig") as fh:
        for row in csv.DictReader(fh):
            if _result_key(row) == (str(project), str(bug), tool, budget, str(rep)):
                return result_is_terminal(row)
    return False
