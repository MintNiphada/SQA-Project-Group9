from __future__ import annotations

import argparse
import csv
import hashlib
import multiprocessing as mp
import os
import re
import shutil
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from queue import Empty

SCRIPT_DIR = Path(__file__).resolve().parent
ROOT = SCRIPT_DIR.parent.parent
SCRIPTS_DIR = ROOT / "scripts"
RESULTS_DIR = ROOT / "results"
BASELINE_RESULTS_CSV = ROOT / "CATG" / "Result_Round2" / "benchmark_results.csv"
DEFAULT_CATG_RESULTS_DIR = ROOT / "CATG" / "Result_Round2" / "enhanced"
CATG_RESULTS_DIR = Path(os.environ.get(
    "CATG_RESULT_DIR",
    str(DEFAULT_CATG_RESULTS_DIR),
))
CATG_RESULTS_CSV = CATG_RESULTS_DIR / "benchmark_results.csv"
if CATG_RESULTS_CSV.resolve() == BASELINE_RESULTS_CSV.resolve():
    raise RuntimeError(
        "CATG_RESULT_DIR resolves to the read-only Baseline; "
        "use CATG/Result_Round2/enhanced instead"
    )
os.environ["CATG_RESULT_DIR"] = str(CATG_RESULTS_DIR)
os.environ["SQA_RESULTS_CSV"] = str(CATG_RESULTS_CSV)
D4J_PROJECTS = Path(r"D:\Lab_SQA\defects4j\framework\projects")
CATG_ROOT = Path(os.environ.get("CATG_HOME", r"D:\Lab_SQA\CATG"))
CATG_JAR = CATG_ROOT / "build" / "libs" / "CATG-0.2.jar"
JAVA8_HOME = Path(r"C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot")
JAVA8_BIN = JAVA8_HOME / "bin"
RESULTS_CSV = CATG_RESULTS_CSV
MANIFEST = RESULTS_DIR / "defects4j_bugs.csv"
BUG_TIMEOUT_SEC = max(60, int(os.environ.get("CATG_BUG_TIMEOUT_SEC", "3600")))


def _sync_catg_results() -> None:
    """Ensure the CATG result directory exists for result writers."""
    CATG_RESULTS_DIR.mkdir(parents=True, exist_ok=True)

if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

from d4j_helpers import (  # noqa: E402
    append_result,
    checkout,
    cleanup_generated_sources,
    compile_version,
    defects4j,
    export_classpath,
    has_result,
    run_cmd,
)
from run_experiment import run_one  # noqa: E402

PRIMITIVE_TYPES = {"boolean", "byte", "short", "int", "long", "char"}
SUPPORTED_TYPES = PRIMITIVE_TYPES | {"java.lang.String"}
CONTROL_NAMES = {
    "if", "for", "while", "switch", "catch", "synchronized", "try", "else",
    "do", "return", "new", "assert", "throw", "case",
}
METHOD_RE = re.compile(
    r"(?m)^[ \t]*(?P<mods>(?:(?:public|protected|private|static|final|abstract|synchronized|native|default|strictfp)\s+)*)"
    r"(?P<ret>(?:[A-Za-z_$][\w$.]*(?:\s*<[^;{}()]*>)?(?:\[\])*\s+)+?)"
    r"(?P<name>[A-Za-z_$][\w$]*)\s*\((?P<params>[^(){}]*)\)"
    r"\s*(?:throws\s+[^{}]+)?\{"
)


@dataclass
class PatchFile:
    path: str
    changed_lines: set[int]
    hunk_lines: set[int]


@dataclass
class SourceMethod:
    name: str
    raw_params: tuple[str, ...]
    params: tuple[str, ...] | None
    static: bool
    start_line: int
    end_line: int
    owner_binary: str
    unsupported_type: str = ""


@dataclass
class TargetSpec:
    project: str
    bug: int
    target_class: str
    method_name: str
    parameter_types: tuple[str, ...]
    static_method: bool
    source_path: Path
    changed_line: int
    unsupported_reason: str = ""


class UnsupportedTarget(Exception):
    pass


def _read_modified_classes(project: str, bug: int) -> list[str]:
    path = D4J_PROJECTS / project / "modified_classes" / f"{bug}.src"
    if not path.exists():
        return []
    classes: list[str] = []
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        value = line.strip()
        if value and not value.startswith("#") and re.match(r"^[A-Za-z_$][\w$]*(\.[A-Za-z_$][\w$]*)+$", value):
            classes.append(value)
    return classes


def _patch_path(project: str, bug: int) -> Path:
    return D4J_PROJECTS / project / "patches" / f"{bug}.src.patch"


def _parse_patch(path: Path) -> list[PatchFile]:
    if not path.exists():
        return []
    files: dict[str, PatchFile] = {}
    current: str | None = None
    old_line = 0
    new_line = 0
    in_hunk = False
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        if line.startswith("diff --git "):
            current = None
            in_hunk = False
            continue
        if line.startswith("+++ b/"):
            current = line[6:].strip()
            files.setdefault(current, PatchFile(current, set(), set()))
            in_hunk = False
            continue
        if line.startswith("@@"):
            match = re.match(r"@@\s+-(\d+)(?:,(\d+))?\s+\+(\d+)(?:,(\d+))?\s+@@", line)
            if not match:
                in_hunk = False
                continue
            old_line = int(match.group(1))
            new_line = int(match.group(3))
            old_count = int(match.group(2) or "1")
            new_count = int(match.group(4) or "1")
            if current:
                entry = files[current]
                entry.hunk_lines.update(range(old_line, old_line + max(old_count, 1)))
                entry.hunk_lines.update(range(new_line, new_line + max(new_count, 1)))
            in_hunk = bool(current)
            continue
        if not in_hunk or not current:
            continue
        if line.startswith("\\"):
            continue
        if line.startswith("-"):
            files[current].changed_lines.add(old_line)
            old_line += 1
            new_line += 1
        elif line.startswith("+"):
            files[current].changed_lines.add(new_line)
            new_line += 1
        elif line.startswith(" "):
            old_line += 1
            new_line += 1
    return list(files.values())


def _split_top_level(value: str) -> list[str]:
    parts: list[str] = []
    start = 0
    depth = 0
    for index, char in enumerate(value):
        if char in "<([{":
            depth += 1
        elif char in ">)]}":
            depth = max(0, depth - 1)
        elif char == "," and depth == 0:
            parts.append(value[start:index].strip())
            start = index + 1
    tail = value[start:].strip()
    if tail:
        parts.append(tail)
    return parts


def _mask_java(source: str) -> str:
    result = list(source)
    index = 0
    state = "code"
    while index < len(source):
        char = source[index]
        nxt = source[index + 1] if index + 1 < len(source) else ""
        if state == "code":
            if char == "/" and nxt == "/":
                result[index] = result[index + 1] = " "
                state = "line"
                index += 2
                continue
            if char == "/" and nxt == "*":
                result[index] = result[index + 1] = " "
                state = "block"
                index += 2
                continue
            if char == '"':
                result[index] = " "
                state = "string"
                index += 1
                continue
            if char == "'":
                result[index] = " "
                state = "char"
                index += 1
                continue
            index += 1
            continue
        if state == "line":
            if char == "\n":
                state = "code"
            else:
                result[index] = " "
            index += 1
            continue
        if state == "block":
            if char == "*" and nxt == "/":
                result[index] = result[index + 1] = " "
                state = "code"
                index += 2
            else:
                if char != "\n":
                    result[index] = " "
                index += 1
            continue
        if state in {"string", "char"}:
            quote = '"' if state == "string" else "'"
            if char == "\\":
                result[index] = " "
                if index + 1 < len(source) and source[index + 1] != "\n":
                    result[index + 1] = " "
                index += 2
                continue
            if char == quote:
                result[index] = " "
                state = "code"
            elif char != "\n":
                result[index] = " "
            index += 1
    return "".join(result)


def _normalise_type(value: str) -> str | None:
    text = re.sub(r"@[A-Za-z_$][\w.$]*(?:\([^)]*\))?", "", value)
    text = re.sub(r"\bfinal\b", "", text)
    text = re.sub(r"\s+", " ", text).strip()
    if not text or text.endswith("...") or text.endswith("[]"):
        return None
    tokens = text.split(" ")
    if len(tokens) > 1 and re.match(r"^[A-Za-z_$][\w$]*$", tokens[-1]):
        tokens.pop()
    text = "".join(tokens)
    if text in PRIMITIVE_TYPES:
        return text
    if text in {"String", "java.lang.String"}:
        return "java.lang.String"
    return None


def _source_declares_class(source: str, target_class: str) -> bool:
    names = {
        target_class.rsplit(".", 1)[-1].split("$", 1)[-1],
        target_class.rsplit(".", 1)[-1].split("$", 1)[0],
    }
    return any(
        re.search(rf"\b(?:class|interface|enum|record)\s+{re.escape(name)}\b", source)
        for name in names
    )


def _find_source_file(workdir: Path, relative: str, target_class: str) -> Path | None:
    relative_path = Path(relative.replace("\\", "/"))
    if relative_path.parts and relative_path.parts[0] in {"a", "b"}:
        relative_path = Path(*relative_path.parts[1:])
    outer_class = target_class.rsplit(".", 1)[-1].split("$", 1)[0]
    target_path = Path(*target_class.split(".")).with_suffix(".java")
    outer_path = Path(*outer_class.split(".")).with_suffix(".java")
    source_roots = (
        workdir,
        workdir / "source",
        workdir / "src" / "java",
        workdir / "src" / "main" / "java",
        workdir / "src",
        workdir / "main" / "java",
    )
    candidates: list[Path] = []
    candidates.append(workdir / relative_path)
    for root in source_roots[1:]:
        candidates.append(root / target_path)
        candidates.append(root / outer_path)
    seen: set[Path] = set()
    for candidate in candidates:
        if candidate in seen or not candidate.is_file():
            continue
        seen.add(candidate)
        source = candidate.read_text(encoding="utf-8", errors="replace")
        if _source_declares_class(source, target_class):
            return candidate
    for root in source_roots[1:]:
        if not root.is_dir():
            continue
        matches = [p for p in root.rglob(outer_path.name) if p.is_file()]
        for candidate in sorted(matches, key=lambda p: (len(p.parts), str(p))):
            if candidate in seen:
                continue
            seen.add(candidate)
            if _source_declares_class(candidate.read_text(encoding="utf-8", errors="replace"), target_class):
                return candidate
    return None


def _brace_end(masked: str, brace: int) -> int:
    depth = 0
    for index in range(brace, len(masked)):
        if masked[index] == "{":
            depth += 1
        elif masked[index] == "}":
            depth -= 1
            if depth == 0:
                return index
    return len(masked) - 1


def _class_ranges(masked: str) -> list[tuple[str, int, int, str]]:
    ranges: list[tuple[str, int, int, str]] = []
    pattern = re.compile(r"\b(?:class|interface|enum|record)\s+([A-Za-z_$][\w$]*)\b[^{};]*\{")
    for match in pattern.finditer(masked):
        brace = masked.find("{", match.start(), match.end())
        if brace < 0:
            continue
        end = _brace_end(masked, brace)
        parents = [
            item for item in ranges
            if item[1] <= match.start() and match.start() <= item[2]
        ]
        parent = min(parents, key=lambda item: item[2] - item[1], default=None)
        owner = f"{parent[3]}${match.group(1)}" if parent else match.group(1)
        ranges.append((match.group(1), match.start(), end, owner))
    return ranges


def _find_methods(source: str) -> list[SourceMethod]:
    masked = _mask_java(source)
    class_names = {
        name for name, _, _, _ in _class_ranges(masked)
    }
    class_ranges = _class_ranges(masked)
    methods: list[SourceMethod] = []
    for match in METHOD_RE.finditer(masked):
        name = match.group("name")
        if name in CONTROL_NAMES or name in class_names:
            continue
        brace = masked.find("{", match.end() - 1)
        if brace < 0:
            continue
        end = _brace_end(masked, brace)
        owners = [
            item for item in class_ranges
            if item[1] <= match.start() <= item[2]
        ]
        owner = min(owners, key=lambda item: item[2] - item[1], default=None)
        raw_params = tuple(_split_top_level(match.group("params")))
        normalized = tuple(_normalise_type(param) or "" for param in raw_params)
        unsupported_type = next(
            (raw.strip() for raw, item in zip(raw_params, normalized) if not item),
            "",
        )
        params = None if unsupported_type else normalized
        methods.append(SourceMethod(
            name=name,
            raw_params=raw_params,
            params=params,
            static=bool(re.search(r"\bstatic\b", match.group("mods"))),
            start_line=masked.count("\n", 0, match.start()) + 1,
            end_line=masked.count("\n", 0, end) + 1,
            owner_binary=owner[3] if owner else "",
            unsupported_type=unsupported_type,
        ))
    return methods


def _method_score(method: SourceMethod, patch: PatchFile) -> tuple[int, int]:
    overlap = len(patch.changed_lines.intersection(range(method.start_line, method.end_line + 1)))
    hunk_overlap = len(patch.hunk_lines.intersection(range(method.start_line, method.end_line + 1)))
    nearest = min(
        (abs(line - method.start_line) for line in patch.hunk_lines),
        default=100000,
    )
    return overlap * 100000 + hunk_overlap * 1000 - nearest, overlap


def discover_target(project: str, bug: int, workdir: Path) -> TargetSpec:
    classes = _read_modified_classes(project, bug)
    if not classes:
        raise UnsupportedTarget("dataset_metadata:modified_classes_missing")
    patches = _parse_patch(_patch_path(project, bug))
    if not patches:
        raise UnsupportedTarget("dataset_metadata:source_patch_missing")
    candidates: list[tuple[int, TargetSpec]] = []
    for target_class in classes:
        class_simple = target_class.rsplit(".", 1)[-1].split("$", 1)[0]
        expected_name = f"{class_simple}.java"
        class_patches = [
            patch for patch in patches
            if Path(patch.path.replace("\\", "/")).name == expected_name
        ]
        for patch in class_patches:
            source_path = _find_source_file(workdir, patch.path, target_class)
            if source_path is None or not source_path.is_file():
                continue
            source = source_path.read_text(encoding="utf-8", errors="replace")
            for method in _find_methods(source):
                score, overlap = _method_score(method, patch)
                if patch.changed_lines:
                    if overlap == 0:
                        continue
                elif not patch.hunk_lines:
                    continue
                elif min(
                    (abs(line - method.start_line) for line in patch.hunk_lines),
                    default=100000,
                ) > 40:
                    continue
                if method.owner_binary and method.owner_binary.split("$", 1)[0] != class_simple:
                    continue
                package = target_class.rsplit(".", 1)[0] if "." in target_class else ""
                reflection_class = (
                    f"{package}.{method.owner_binary}"
                    if package and method.owner_binary
                    else method.owner_binary or target_class
                )
                params = method.params or ()
                outer_name = class_simple
                abstract_class = bool(re.search(
                    rf"\babstract\s+(?:class|interface)\s+{re.escape(outer_name)}\b",
                    source,
                ))
                unsupported = ""
                if method.params is None:
                    unsupported = f"harness_limitation:unsupported_type:{method.unsupported_type}"
                elif abstract_class and not method.static:
                    unsupported = "harness_limitation:abstract_class"
                candidates.append((score, TargetSpec(
                    project=project,
                    bug=bug,
                    target_class=reflection_class,
                    method_name=method.name,
                    parameter_types=params,
                    static_method=method.static,
                    source_path=source_path,
                    changed_line=(patch.changed_lines and min(patch.changed_lines))
                    or (patch.hunk_lines and min(patch.hunk_lines))
                    or method.start_line,
                    unsupported_reason=unsupported,
                )))
    if not candidates:
        raise UnsupportedTarget("target_discovery:no_source_method")
    supported = [item for item in candidates if not item[1].unsupported_reason]
    if supported:
        return max(supported, key=lambda item: item[0])[1]
    spec = max(candidates, key=lambda item: item[0])[1]
    raise UnsupportedTarget(f"{spec.unsupported_reason}:{spec.method_name}")


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
        Path(r"C:\Program Files (x86)\Git\bin\bash.exe"),
        Path(r"C:\Program Files\Git\usr\bin\bash.exe"),
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


def _git_path(path: Path) -> str:
    value = os.fspath(path).replace("\\", "/")
    if len(value) >= 3 and value[0] == "/" and value[1].isalpha() and value[2] == "/":
        return value
    if os.name == "nt" and len(value) >= 2 and value[1] == ":":
        return f"/{value[0].lower()}/{value[3:]}"
    return str(path.resolve()).replace("\\", "/")


def _find_project_classes(workdir: Path) -> Path:
    candidates = [
        workdir / "target" / "classes",
        workdir / "build" / "classes" / "java" / "main",
        workdir / "build" / "classes",
        workdir / "build",
        workdir / "classes",
        workdir / "bin",
    ]
    for candidate in candidates:
        if candidate.is_dir() and any(candidate.rglob("*.class")):
            return candidate
    raise RuntimeError(f"compiled classes not found under {workdir}")


def _java_string(value: str) -> str:
    result: list[str] = []
    for char in value:
        code = ord(char)
        if char == "\\":
            result.append("\\\\")
        elif char == '"':
            result.append('\\"')
        elif char == "\n":
            result.append("\\n")
        elif char == "\r":
            result.append("\\r")
        elif char == "\t":
            result.append("\\t")
        elif code < 32 or code > 126:
            if code <= 0xFFFF:
                result.append(f"\\u{code:04x}")
            else:
                adjusted = code - 0x10000
                high = 0xD800 + (adjusted >> 10)
                low = 0xDC00 + (adjusted & 0x3FF)
                result.append(f"\\u{high:04x}\\u{low:04x}")
        else:
            result.append(char)
    return "".join(result)


def _java_type_expression(value: str) -> str:
    return f"{value}.class"


def _new_instance_source() -> str:
    return '''    private static Object newInstance(Class<?> type) throws Exception {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception error) {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);
            return unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
        }
    }
'''


def _stable_suffix(project: str, bug: int) -> str:
    raw = f"{project}-{bug}".encode("utf-8")
    return hashlib.sha1(raw).hexdigest()[:10]


def _default_values(params: tuple[str, ...]) -> list[str]:
    values: list[str] = []
    for param in params:
        values.append({
            "boolean": "0",
            "byte": "0",
            "short": "0",
            "int": "0",
            "long": "0",
            "char": "0",
            "java.lang.String": "0",
        }[param])
    return values


def _harness_source(spec: TargetSpec, class_name: str) -> str:
    types = ", ".join(_java_type_expression(item) for item in spec.parameter_types)
    reads = []
    defaults = _default_values(spec.parameter_types)
    for index, (param, default) in enumerate(zip(spec.parameter_types, defaults)):
        if param == "int":
            reads.append(f"values[{index}] = CATG.readInt({default});")
        elif param == "long":
            reads.append(f"values[{index}] = CATG.readLong({default}L);")
        elif param == "char":
            reads.append(f"values[{index}] = CATG.readChar('{default}');")
        elif param == "byte":
            reads.append(f"values[{index}] = CATG.readByte((byte){default});")
        elif param == "short":
            reads.append(f"values[{index}] = CATG.readShort((short){default});")
        elif param == "boolean":
            reads.append(f"values[{index}] = CATG.readBool(false);")
        else:
            reads.append(f"values[{index}] = CATG.readString(\"{_java_string(default)}\");")
    types_decl = f"Class<?>[] types = {{{types}}};" if types else "Class<?>[] types = new Class<?>[0];"
    receiver = "Modifier.isStatic(method.getModifiers()) ? null : newInstance(target)"
    return f'''package catg.generated;

import catg.CATG;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class {class_name} {{
    public static void main(String[] args) {{
        try {{
            Class<?> target = Class.forName("{_java_string(spec.target_class)}");
            {types_decl}
            Method method = target.getDeclaredMethod("{_java_string(spec.method_name)}", types);
            method.setAccessible(true);
            Object receiver = {receiver};
            Object[] values = new Object[types.length];
{chr(10).join(reads)}
            try {{
                method.invoke(receiver, values);
            }} catch (InvocationTargetException error) {{
                Throwable cause = error.getCause();
                if (cause != null) {{
                    System.err.println(cause);
                }}
            }}
        }} catch (Throwable error) {{
            error.printStackTrace();
            System.exit(1);
        }}
    }}

{_new_instance_source()}}}
'''


def _runner_source(spec: TargetSpec, class_name: str) -> str:
    types = ", ".join(_java_type_expression(item) for item in spec.parameter_types)
    types_decl = f"private static final Class<?>[] TYPES = {{{types}}};" if types else "private static final Class<?>[] TYPES = new Class<?>[0];"
    return f'''package catg.generated;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class {class_name} {{
    {types_decl}
    public static void main(String[] args) throws Exception {{
        String outcome;
        try {{
            List<String> lines = Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8);
            while (lines.size() < TYPES.length) {{
                lines.add("");
            }}
            outcome = invoke(lines);
        }} catch (Throwable error) {{
            outcome = "ERROR:" + error.getClass().getName() + ":" + clean(error.getMessage());
        }}
        System.out.println("CATG_OUTCOME:" + clean(outcome));
    }}

    private static String invoke(List<String> lines) throws Exception {{
        Class<?> target = Class.forName("{_java_string(spec.target_class)}");
        Method method = target.getDeclaredMethod("{_java_string(spec.method_name)}", TYPES);
        method.setAccessible(true);
        Object receiver = null;
        if (!Modifier.isStatic(method.getModifiers())) {{
            receiver = newInstance(target);
        }}
        Object[] values = new Object[TYPES.length];
        for (int index = 0; index < TYPES.length; index++) {{
            values[index] = convert(lines.get(index), TYPES[index]);
        }}
        try {{
            return outcome(method.invoke(receiver, values));
        }} catch (InvocationTargetException error) {{
            Throwable cause = error.getCause();
            return "EX:" + cause.getClass().getName() + ":" + clean(cause.getMessage());
        }}
    }}

    private static Object convert(String value, Class<?> type) {{
        if (type == int.class) return Integer.parseInt(value);
        if (type == long.class) return Long.parseLong(value);
        if (type == char.class) return value.isEmpty() ? '\\u0000' : value.charAt(0);
        if (type == byte.class) return Byte.parseByte(value);
        if (type == short.class) return Short.parseShort(value);
        if (type == boolean.class) return Integer.parseInt(value) != 0;
        if (type == String.class) return value;
        throw new IllegalArgumentException(type.getName());
    }}

    private static String outcome(Object value) {{
        if (value == null) return "null";
        if (value.getClass().isArray()) return Arrays.deepToString(new Object[]{{value}});
        return String.valueOf(value);
    }}

    private static String clean(String value) {{
        if (value == null) return "";
        return value.replace("\\r", "\\\\r").replace("\\n", "\\\\n");
    }}

{_new_instance_source()}}}
'''


def _junit_source(spec: TargetSpec, project: str, bug: int, cases: list[list[str]], outcomes: list[str]) -> str:
    package = spec.target_class.rsplit(".", 1)[0] if "." in spec.target_class else ""
    class_name = f"CATG_{re.sub(r'[^A-Za-z0-9_]', '_', project)}_{bug}Test"
    types = ", ".join(_java_type_expression(item) for item in spec.parameter_types)
    types_decl = f"Class<?>[] types = {{{types}}};" if types else "Class<?>[] types = new Class<?>[0];"
    lines: list[str] = []
    if package:
        lines.extend([f"package {package};", ""])
    lines.extend([
        "import java.lang.reflect.Array;",
        "import java.lang.reflect.Constructor;",
        "import java.lang.reflect.InvocationTargetException;",
        "import java.lang.reflect.Method;",
        "import java.lang.reflect.Modifier;",
        "import java.util.Arrays;",
        "import org.junit.Test;",
        "import static org.junit.Assert.assertEquals;",
        "",
        f"public class {class_name} {{",
    ])
    for index, (values, expected) in enumerate(zip(cases, outcomes), 1):
        literal_values = ", ".join('"' + _java_string(value) + '"' for value in values)
        lines.extend([
            "",
            "    @Test(timeout = 120000)",
            f"    public void testCatg{index}() throws Exception {{",
            f"        assertEquals(\"{_java_string(expected)}\", invoke(new String[] {{{literal_values}}}));",
            "    }",
        ])
    lines.extend([
        "",
        "    private static String invoke(String[] values) throws Exception {",
        f"        Class<?> target = Class.forName(\"{_java_string(spec.target_class)}\");",
        f"        {types_decl}",
        f"        Method method = target.getDeclaredMethod(\"{_java_string(spec.method_name)}\", types);",
        "        method.setAccessible(true);",
        "        Object receiver = null;",
        "        if (!Modifier.isStatic(method.getModifiers())) {",
        "            receiver = newInstance(target);",
        "        }",
        "        Object[] arguments = new Object[types.length];",
        "        for (int index = 0; index < types.length; index++) {",
        "            arguments[index] = convert(values[index], types[index]);",
        "        }",
        "        try {",
        "            return outcome(method.invoke(receiver, arguments));",
        "        } catch (InvocationTargetException error) {",
        "            Throwable cause = error.getCause();",
        "            return \"EX:\" + cause.getClass().getName() + \":\" + clean(cause.getMessage());",
        "        }",
        "    }",
        "",
        "    private static Object convert(String value, Class<?> type) {",
        "        if (type == int.class) return Integer.parseInt(value);",
        "        if (type == long.class) return Long.parseLong(value);",
        "        if (type == char.class) return value.isEmpty() ? '\\u0000' : value.charAt(0);",
        "        if (type == byte.class) return Byte.parseByte(value);",
        "        if (type == short.class) return Short.parseShort(value);",
        "        if (type == boolean.class) return Integer.parseInt(value) != 0;",
        "        if (type == String.class) return value;",
        "        throw new IllegalArgumentException(type.getName());",
        "    }",
        "",
        "    private static String outcome(Object value) {",
        "        if (value == null) return \"null\";",
        "        if (value.getClass().isArray()) return Arrays.deepToString(new Object[] { value });",
        "        return String.valueOf(value);",
        "    }",
        "",
        "    private static String clean(String value) {",
        "        if (value == null) return \"\";",
        "        return value.replace(\"\\r\", \"\\\\r\").replace(\"\\n\", \"\\\\n\");",
        "    }",
        _new_instance_source().rstrip("\n"),
        "}",
        "",
    ])
    return "\n".join(lines)


def _compile_java(source: Path, output: Path, classpath: str) -> None:
    output.mkdir(parents=True, exist_ok=True)
    javac = JAVA8_BIN / "javac.exe"
    if not javac.is_file():
        raise FileNotFoundError(f"Java 8 compiler not found: {javac}")
    result = run_cmd(
        [
            str(javac), "-source", "8", "-target", "8", "-cp", classpath,
            "-d", str(output), str(source),
        ],
        cwd=source.parent,
        extra_path=[JAVA8_BIN],
        timeout=1800,
    )
    if result.returncode != 0:
        detail = (result.stdout + result.stderr).strip()
        raise RuntimeError(f"generated Java compilation failed:\n{detail[-6000:]}")


def _read_snapshot(path: Path, count: int, defaults: list[str]) -> list[str]:
    text = path.read_text(encoding="utf-8", errors="replace")
    text = text.replace("\r\n", "\n").replace("\r", "\n").replace("\x00", "")
    values = text.split("\n")
    if values and values[-1] == "":
        values.pop()
    if len(values) < count:
        values.extend(defaults[len(values):])
    return values[:count]


def _collect_cases(snapshot_dir: Path, params: tuple[str, ...]) -> list[list[str]]:
    defaults = _default_values(params)
    snapshots = sorted(
        snapshot_dir.glob("input-*.txt"),
        key=lambda item: int(item.stem.split("-")[-1]),
    )
    if not snapshots:
        raise UnsupportedTarget("no_snapshots")
    return [_read_snapshot(path, len(params), defaults) for path in snapshots]


def _write_case_file(path: Path, values: list[str]) -> None:
    path.write_text("\n".join(values) + "\n", encoding="utf-8")


def _remove_generated_tests(workdir: Path) -> None:
    cleanup_generated_sources(workdir)


def _run_runner(runner_class: str, runner_classes: Path, classpath: str, case_file: Path) -> str:
    java = JAVA8_BIN / "java.exe"
    result = run_cmd(
        [str(java), "-cp", f"{runner_classes};{classpath}", runner_class, str(case_file)],
        cwd=runner_classes,
        extra_path=[JAVA8_BIN],
        env_overrides={"JAVA_TOOL_OPTIONS": "", "ANT_OPTS": ""},
        timeout=120,
    )
    stdout = result.stdout.strip()
    marker = "CATG_OUTCOME:"
    if result.returncode != 0:
        return "ERROR:" + (stdout + "\n" + result.stderr).strip()[-2000:]
    if marker not in stdout:
        return "ERROR:missing_outcome"
    marker_line = [line for line in stdout.splitlines() if line.startswith(marker)]
    if not marker_line:
        return "ERROR:missing_outcome"
    return marker_line[-1][len(marker):].strip()


def _run_catg(spec: TargetSpec, workdir: Path, case_dir: Path, harness_classes: Path, budget: str) -> Path:
    launcher = ROOT / "CATG" / "Code" / "dconcolic"
    seed = case_dir / "seed.txt"
    snapshots = case_dir / "snapshots"
    state_dir = case_dir / "catg-state"
    classpath_file = case_dir / "classpath.txt"
    classpath = export_classpath(workdir)
    classpath_file.write_text(classpath, encoding="utf-8")
    project_classes = _find_project_classes(workdir)
    _write_case_file(seed, _default_values(spec.parameter_types))
    shutil.rmtree(state_dir, ignore_errors=True)
    state_dir.mkdir(parents=True, exist_ok=True)
    command = [
        str(_find_git_bash()),
        _git_path(launcher),
        "--budget", budget,
        "--workdir", _git_path(workdir),
        "--state-dir", _git_path(state_dir),
        "--project", spec.project,
        "--main-class", f"catg.generated.CatgHarness_{_stable_suffix(spec.project, spec.bug)}",
        "--harness-classes", _git_path(harness_classes),
        "--classes-dir", _git_path(project_classes),
        "--classpath-file", _git_path(classpath_file),
        "--input-file", _git_path(seed),
        "--snapshot-dir", _git_path(snapshots),
    ]
    if not spec.parameter_types:
        command.append("--allow-empty-input")
    log_path = case_dir / "catg.log"
    env = os.environ.copy()
    env.update({
        "CATG_HOME": str(CATG_ROOT),
        "CATG_JAVA_HOME": str(JAVA8_HOME),
        "JAVA_TOOL_OPTIONS": "",
        "ANT_OPTS": "",
    })
    try:
        result = subprocess.run(
            command,
            cwd=str(ROOT),
            env=env,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=1800,
        )
        log_path.write_text(result.stdout + result.stderr, encoding="utf-8")
        if result.returncode != 0:
            detail = (result.stdout + result.stderr).strip()
            if "ConcolicInterpreter" in detail:
                for runtime_error in ("ClassCastException", "ArrayIndexOutOfBoundsException"):
                    if runtime_error in detail:
                        raise UnsupportedTarget(f"catg_runtime_interpreter:{runtime_error}")
            raise RuntimeError(f"CATG failed with exit {result.returncode}:\n{detail[-6000:]}")
        return snapshots
    finally:
        shutil.rmtree(state_dir, ignore_errors=True)


def _result_failure(project: str, bug: int, budget: str,
                    generation_elapsed: float, reason: str) -> None:
    append_result({
        "project": project,
        "bug": bug,
        "tool": "CATG",
        "budget": budget,
        "repetition": 1,
        "test_count": 0,
        "compile_ok": False,
        "status": "error" if reason.startswith("ERROR:") else "unsupported",
        "failing_on_buggy": 0,
        "failing_on_fixed": 0,
        "fault_detected": False,
        "lines_total": 0,
        "lines_covered": 0,
        "line_cov_pct": 0,
        "branches_total": 0,
        "branches_covered": 0,
        "branch_cov_pct": 0,
        "generation_time_sec": round(generation_elapsed, 2),
        "notes": f"STATUS:{reason}",
    })
    _sync_catg_results()


def _run_one_bug_worker(row: dict, budget: str, force: bool, result_queue) -> None:
    result_queue.put(_run_one_bug(row, budget, force))


def _run_one_bug_with_timeout(row: dict, budget: str, force: bool) -> str:
    """Run one bug in a killable child process with a wall-clock deadline."""
    context = mp.get_context("spawn")
    result_queue = context.Queue()
    process = context.Process(
        target=_run_one_bug_worker,
        args=(row, budget, force, result_queue),
    )
    process.start()
    process.join(BUG_TIMEOUT_SEC)
    if process.is_alive():
        process.terminate()
        process.join(30)
        project = str(row.get("project", "")).strip()
        bug = int(str(row.get("bug", "0")).strip())
        _result_failure(
            project,
            bug,
            budget,
            0.0,
            f"ERROR:timeout_after_{BUG_TIMEOUT_SEC}s",
        )
        print(f"[error] {project}-{bug}: timeout after {BUG_TIMEOUT_SEC}s", file=sys.stderr)
        return "error"
    try:
        return result_queue.get(timeout=5)
    except Empty:
        project = str(row.get("project", "")).strip()
        bug = int(str(row.get("bug", "0")).strip())
        _result_failure(project, bug, budget, 0.0, "ERROR:worker_exit_without_result")
        return "error"


def _run_one_bug(row: dict, budget: str, force: bool) -> str:
    project = str(row["project"]).strip()
    bug = int(str(row["bug"]).strip())
    label = f"{project}-{bug}"
    if not force and has_result(project, bug, "CATG", budget, 1):
        print(f"[skip] {label} already in results")
        return "skipped"
    generation_elapsed = 0.0
    try:
        workdir = checkout(project, bug, "b")
        _remove_generated_tests(workdir)
        compile_version(workdir)
        spec = discover_target(project, bug, workdir)
        case_dir = CATG_RESULTS_DIR / f"{project}_{bug}"
        if case_dir.exists():
            shutil.rmtree(case_dir)
        harness_classes = case_dir / "harness-classes"
        runner_classes = case_dir / "runner-classes"
        harness_class = f"CatgHarness_{_stable_suffix(project, bug)}"
        runner_class = f"catg.generated.CatgOutcome_{_stable_suffix(project, bug)}"
        harness_source = case_dir / f"{harness_class}.java"
        runner_source = case_dir / f"CatgOutcome_{_stable_suffix(project, bug)}.java"
        case_dir.mkdir(parents=True, exist_ok=True)
        harness_source.write_text(_harness_source(spec, harness_class), encoding="utf-8")
        runner_source.write_text(_runner_source(spec, runner_class.rsplit(".", 1)[-1]), encoding="utf-8")
        project_cp = export_classpath(workdir)
        _compile_java(harness_source, harness_classes, f"{CATG_JAR};{project_cp}")
        _compile_java(runner_source, runner_classes, project_cp)
        generation_started = time.perf_counter()
        try:
            snapshots = _run_catg(spec, workdir, case_dir, harness_classes, budget)
        finally:
            generation_elapsed = time.perf_counter() - generation_started
        cases = _collect_cases(snapshots, spec.parameter_types)
        if not cases:
            raise RuntimeError("CATG generated zero cases")
        fixed = checkout(project, bug, "f")
        _remove_generated_tests(fixed)
        compile_version(fixed)
        fixed_cp = export_classpath(fixed)
        outcomes: list[str] = []
        for index, values in enumerate(cases, 1):
            case_file = case_dir / f"case-{index}.txt"
            _write_case_file(case_file, values)
            outcome = _run_runner(runner_class, runner_classes, fixed_cp, case_file)
            if outcome.startswith("ERROR:"):
                raise UnsupportedTarget(f"concrete_runner:{outcome[:300]}")
            outcomes.append(outcome)
        suite_dir = case_dir / "test-suite"
        if suite_dir.exists():
            shutil.rmtree(suite_dir)
        suite_dir.mkdir(parents=True, exist_ok=True)
        junit_file = suite_dir / f"CATG_{project}_{bug}Test.java"
        junit_file.write_text(_junit_source(spec, project, bug, cases, outcomes), encoding="utf-8")
        run_one(
            project,
            bug,
            "CATG",
            budget,
            1,
            suite_dir,
            force=force,
            generation_time_sec=generation_elapsed,
            extra_notes=f"target={spec.target_class}::{spec.method_name};params={','.join(spec.parameter_types)};catg_cases={len(cases)}",
        )
        _sync_catg_results()
        print(f"[ok]   {label}: {generation_elapsed:.1f}s cases={len(cases)} target={spec.target_class}::{spec.method_name}")
        return "ok"
    except UnsupportedTarget as error:
        _result_failure(project, bug, budget, generation_elapsed, str(error))
        print(f"[unsupported] {label}: {error}")
        return "unsupported"
    except Exception as error:
        _result_failure(project, bug, budget, generation_elapsed, f"ERROR:{error}")
        print(f"[error] {label}: {error}", file=sys.stderr)
        return "error"


def _load_rows(manifest: Path, projects: str | None) -> list[dict]:
    if projects:
        wanted = {item.strip() for item in projects.split(",") if item.strip()}
        rows = []
        with manifest.open(newline="", encoding="utf-8-sig") as handle:
            for row in csv.DictReader(handle):
                if row.get("project") in wanted:
                    rows.append(row)
        return rows
    with manifest.open(newline="", encoding="utf-8-sig") as handle:
        return list(csv.DictReader(handle))


def run_batch(manifest: Path, projects: str | None = None, budget: str = "default",
              limit: int | None = None, force: bool = False, dry_run: bool = False) -> int:
    rows = _load_rows(manifest, projects)
    if limit is not None:
        if limit < 0:
            raise ValueError("limit must be non-negative")
        rows = rows[:limit]
    if dry_run:
        for row in rows:
            project = str(row.get("project", "")).strip()
            bug = str(row.get("bug", "")).strip()
            print(f"[plan] {project}-{bug}: auto-discover modified class/method; budget={budget}")
        return 0
    errors = 0
    unsupported = 0
    skipped = 0
    completed = 0
    started = time.time()
    for row in rows:
        project = str(row.get("project", "")).strip()
        bug_text = str(row.get("bug", "")).strip()
        try:
            bug = int(bug_text)
        except ValueError:
            errors += 1
            print(f"[error] invalid bug id: {bug_text!r}", file=sys.stderr)
            continue
        result = _run_one_bug_with_timeout(row, budget, force)
        if result == "error":
            errors += 1
        elif result == "unsupported":
            unsupported += 1
        elif result == "ok":
            completed += 1
        else:
            skipped += 1
    print(f"[done] completed={completed} unsupported={unsupported} skipped={skipped} errors={errors} elapsed={time.time()-started:.1f}s")
    return 1 if errors else 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, default=MANIFEST)
    parser.add_argument("--projects")
    parser.add_argument("--budget", default="default")
    parser.add_argument("--limit", type=int)
    parser.add_argument("--force", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    return run_batch(args.manifest, args.projects, args.budget, args.limit, args.force, args.dry_run)


if __name__ == "__main__":
    raise SystemExit(main())
