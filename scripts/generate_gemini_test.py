"""
Generate a JUnit test suite for a Defects4J bug using Gemini through the
KKU IntelSphere gateway (OpenAI-compatible /chat/completions).

Output layout matches what run_experiment.py expects as --test-dir:
a directory of .java files (typically GemTestCode/<Project>_<N>/).  The
pipeline (run_experiment.py -> d4j_helpers.run_experiment) discovers the
files, packs them, and drives Defects4J fault-detection + coverage.

Security: the API key is read ONLY from the environment variable
KKU_INTELSPHERE_API_KEY at runtime.  It is never written to any file and
never appears in this repo.

Usage:
  python generate_gemini_test.py --project Lang --bug 1 \
      --out-dir ..\\GemTestCode\\Lang_1 [--model gemini-3.7-flash] [--reps 3]
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path

BASE_URL = "https://gen.ai.kku.ac.th/api/v1"
DEFAULT_MODEL = "gemini-3.7-flash"
ENV_KEY = "KKU_INTELSPHERE_API_KEY"

PROMPT_TMPL = """You are a senior Java engineer generating a regression test for a real bug.

The project is Apache Commons Lang and the targeted bug is Defects4J bug Lang-{bug}.
The method of interest is `NumberUtils.createNumber`.  On the buggy version,
calling createNumber with a hex value whose magnitude exceeds Integer.MAX_VALUE
(e.g. "0x80000000") throws NumberFormatException instead of returning a Long
(JAVA-1597 / LANG-915 overflow handling regression).

Write ONE JUnit 4 test class (JUnit 4, org.junit.Test / static org.junit.Assert.*)
that exposes this fault:
  - place it in package  org.apache.commons.lang3.math
  - class name:      GeminiLang{bug}Test
  - it MUST FAIL on the buggy version (NumberFormatException is expected NOT
    to be thrown) and PASS on the fixed version (returns a Long with the
    expected magnitude).
  - use exactly the assertNotNull / assertEquals(2147483648L, ...) pattern so
    the test compiles against commons-lang3's real NumberUtils returned type
    (Number) without forcing any imports beyond NumberUtils, org.junit.Test and
    org.junit.Assert.*.

IMPORTANT:
  - Output ONLY the raw Java source.  No markdown fences, no explanation.
  - The file must be self-contained: package line first, then imports, then class.
"""


def _call_chat_completions(api_key: str, model: str, prompt: str,
                           timeout: int = 600) -> str:
    payload = {
        "model": model,
        "messages": [
            {"role": "user", "content": prompt},
        ],
        "max_tokens": 4096,
        "temperature": 0.7,
        "stream": False,
    }
    sep = "\\\\" if os.name == "nt" else "/"
    url = f"{BASE_URL}{sep}chat/completions"
    cmd = [
        "python", "-c",
        (
            "import json,sys,urllib.request\n"
            "body=json.dumps(%r).encode()\n"
            "req=urllib.request.Request(sys.argv[1],data=body,headers="
            "{'Authorization':'Bearer '+sys.argv[2],'Content-Type':'application/json'})\n"
            "r=urllib.request.urlopen(req,timeout=%d)\n"
            "print(json.loads(r.read().decode()['choices'][0]['message']['content']))\n"
        ) % (payload, timeout),
        url, api_key,
    ]
    res = subprocess.run(cmd, capture_output=True, text=True,
                         encoding="utf-8", errors="replace", timeout=timeout + 60)
    if res.returncode != 0:
        raise RuntimeError(f"IntelSphere request failed:\n{res.stderr}\n{res.stdout}")
    return res.stdout.strip()


def _extract_java(raw: str) -> str:
    """Strip markdown fences if present, fall back to the raw text."""
    fence = re.search(r"```(?:java)?\s*\n(.*?)```", raw, flags=re.DOTALL)
    if fence:
        return fence.group(1).strip()
    return raw.strip()


def generate(project: str, bug: int, out_dir: Path, model: str, reps: int) -> list[Path]:
    api_key = os.environ.get(ENV_KEY, "").strip()
    if not api_key:
        raise RuntimeError(
            f"{ENV_KEY} env variable is not set (read key at runtime, never stored)."
        )

    out_dir.mkdir(parents=True, exist_ok=True)
    prompt = PROMPT_TMPL.format(bug=bug)
    written: list[Path] = []

    for rep in range(1, reps + 1):
        raw = _call_chat_completions(api_key, model, prompt)
        java = _extract_java(raw)
        # Validate it at least looks like a JUnit test in the right package.
        if "org.junit.Test" not in java or "org.apache.commons.lang3.math" not in java:
            print(f"[rep {rep}] WARNING: output does not look like the requested "
                  f"JUnit test; skipping.\n--- raw ---\n{raw[:400]}\n------------")
            continue

        out_file = out_dir / f"GeminiLang{bug}Test_{model}.{rep}.java"
        out_file.write_text(java + "\n", encoding="utf-8")
        n_tests = len(re.findall(r"@Test\b", java))
        print(f"[rep {rep}] wrote {out_file} ({n_tests} @Test)")
        written.append(out_file)

    if not written:
        raise RuntimeError("no valid suites generated")
    return written


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    p.add_argument("--project", required=True)
    p.add_argument("--bug", type=int, required=True)
    p.add_argument("--out-dir", type=Path, required=True)
    p.add_argument("--model", default=DEFAULT_MODEL)
    p.add_argument("--reps", type=int, default=1)
    args = p.parse_args()

    t0 = time.time()
    written = generate(args.project, args.bug, args.out_dir, args.model, args.reps)
    print(f"[done] generated {len(written)} file(s) in {args.out_dir} "
          f"({time.time() - t0:.1f}s)")


if __name__ == "__main__":
    main()
</content>