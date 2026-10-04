import os
import re
import time
import csv
import shutil
import threading
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
import httpx
import openai
from dotenv import load_dotenv
from openai import OpenAI
CSV_PATH = "results/benchmark_data.csv"
OUT_ROOT = "."
CSV_FIELDS = [
    "Model", "Project", "BugID", "TargetClass", "Status", "Retries",
    "GenTime(s)", "PromptTokens", "CompletionTokens", "NumTests",
    "NumAssertions", "FailingTests", "TestRunTime(s)", "LineCoverage(%)",
    "BranchCoverage(%)", "LinesTotal", "LinesCovered", "BranchesTotal",
    "BranchesCovered", "Timestamp"
]

load_dotenv()

# =====================================================================
#  SETTINGS (ปรับผ่าน .env ได้)
# =====================================================================
WORKERS = int(os.getenv("WORKERS", 8))
MAX_BUGS_PER_PROJECT = int(os.getenv("MAX_BUGS_PER_PROJECT", 0))
MAX_RETRIES = int(os.getenv("MAX_RETRIES", 0))
MAX_TOKENS = int(os.getenv("MAX_TOKENS", 4096))

# --- Feature Toggles for Speed ---
ENABLE_COVERAGE = os.getenv("ENABLE_COVERAGE", "false").lower() in ("true", "1", "yes")

# --- Directory setup ---
CHECKOUT_BASE_DIR = os.getenv("CHECKOUT_DIR", "/tmp/defects4j_checkout" if os.name != "nt" else "defects4j_checkout")

# --- Subprocess timeouts (seconds) ---
TIMEOUT_CHECKOUT = int(os.getenv("TIMEOUT_CHECKOUT", 180))
TIMEOUT_COMPILE = int(os.getenv("TIMEOUT_COMPILE", 300))
TIMEOUT_TEST = int(os.getenv("TIMEOUT_TEST", 180))
TIMEOUT_COVERAGE = int(os.getenv("TIMEOUT_COVERAGE", 300))

# --- Key rotation / cooldown ---
KEY_QUOTA_COOLDOWN = int(os.getenv("KEY_QUOTA_COOLDOWN", 3600))
KEY_RATE_COOLDOWN = int(os.getenv("KEY_RATE_COOLDOWN", 60))
KEY_TRANSIENT_COOLDOWN = int(os.getenv("KEY_TRANSIENT_COOLDOWN", 15))
MAX_TOTAL_WAIT = int(os.getenv("MAX_TOTAL_WAIT", 4 * 3600))
MAX_SINGLE_SLEEP = 300

STOP = threading.Event()
CSV_LOCK = threading.Lock()
PRINT_LOCK = threading.Lock()


def log(msg):
    with PRINT_LOCK:
        print(msg, flush=True)


class AllKeysExhaustedError(Exception):
    """ทุกคีย์ใช้ไม่ได้ และรอครบ MAX_TOTAL_WAIT แล้วก็ยังไม่กลับมาใช้ได้"""
    pass


# =====================================================================
#  1. KEY MANAGER
# =====================================================================
class KKUKeyManager:
    def __init__(self, base_url):
        # อ่านคีย์จาก environment variables ที่ขึ้นต้นด้วย KKU_API_KEY_
        self.keys = []
        for env_key, env_val in os.environ.items():
            if env_key.startswith("KKU_API_KEY_") and env_val.strip():
                self.keys.append(env_val.strip())
        
        # กรณีสำรองเผื่อมี KKU_API_KEYS (แบบคอมมา) อยู่
        if not self.keys and os.getenv("KKU_API_KEYS"):
            self.keys = [k.strip() for k in os.getenv("KKU_API_KEYS").split(",") if k.strip()]

        if not self.keys:
            raise ValueError("[ERROR] No KKU_API_KEY_* found in .env file. Please check configuration.")

        self.base_url = base_url
        self.current_index = 0
        self.cooldown_until = [0.0] * len(self.keys)
        self.lock = threading.Lock()

        self.clients = []
        for key in self.keys:
            http_client = httpx.Client(
                timeout=httpx.Timeout(connect=15.0, read=180.0, write=20.0, pool=30.0),
                limits=httpx.Limits(max_keepalive_connections=50, max_connections=100),
            )
            self.clients.append(OpenAI(api_key=key, base_url=base_url, http_client=http_client, max_retries=0))
        log(f"[INFO] Loaded {len(self.keys)} KKU API Keys (persistent connections ready).")
        
    def _next_available_index(self):
        with self.lock:
            now = time.time()
            n = len(self.keys)
            for i in range(n):
                idx = (self.current_index + i) % n
                if self.cooldown_until[idx] <= now:
                    self.current_index = (idx + 1) % n
                    return idx
            return None

    def _mark_key(self, idx, cooldown):
        with self.lock:
            self.cooldown_until[idx] = max(self.cooldown_until[idx], time.time() + cooldown)

    def _earliest_ready_in(self):
        with self.lock:
            return min(self.cooldown_until) - time.time()

    def _status_line(self):
        now = time.time()
        with self.lock:
            return " ".join(
                f"#{i + 1}:" + ("ready" if t <= now else f"wait {int(t - now)}s")
                for i, t in enumerate(self.cooldown_until)
            )

    @staticmethod
    def _retry_after_seconds(e):
        try:
            val = e.response.headers.get("retry-after")
            if val:
                return min(max(float(val), 1.0), 600.0)
        except Exception:
            pass
        return None

    def _classify_error(self, e):
        msg = str(e).lower()
        status = getattr(e, "status_code", None)
        quota_words = ("quota", "insufficient", "exhausted", "billing", "credit", "balance", "token limit")

        if isinstance(e, (openai.AuthenticationError, openai.PermissionDeniedError)) or status in (401, 402, 403):
            return "quota", KEY_QUOTA_COOLDOWN

        if isinstance(e, openai.RateLimitError) or status == 429:
            if any(w in msg for w in quota_words):
                return "quota", KEY_QUOTA_COOLDOWN
            return "rate", self._retry_after_seconds(e) or KEY_RATE_COOLDOWN

        if isinstance(e, (openai.APITimeoutError, openai.APIConnectionError, openai.InternalServerError,
                          httpx.TimeoutException, httpx.TransportError, ValueError)) \
                or (isinstance(status, int) and status >= 500):
            return "transient", KEY_TRANSIENT_COOLDOWN

        if isinstance(e, openai.BadRequestError) or status in (400, 404, 413, 422):
            return "fatal", 0

        if any(w in msg for w in ["429", "401", "quota", "limit", "exhausted", "unauthorized",
                                  "rate", "timeout", "timed out", "connect"]):
            return "rate", KEY_RATE_COOLDOWN

        return "fatal", 0

    def generate_code(self, model_name, system_prompt, target_code, pkg_name="", class_name=""):
        total_waited = 0.0
        while True:
            if STOP.is_set():
                raise AllKeysExhaustedError("Stopped: another worker reported all keys exhausted.")

            idx = self._next_available_index()

            if idx is None:
                wait = min(max(self._earliest_ready_in(), 1.0), MAX_SINGLE_SLEEP)
                if total_waited + wait > MAX_TOTAL_WAIT:
                    raise AllKeysExhaustedError(
                        f"All {len(self.keys)} keys unavailable; waited {int(total_waited)}s. [{self._status_line()}]"
                    )
                log(f"[INFO] All keys cooling down [{self._status_line()}]. Sleeping {int(wait)}s...")
                time.sleep(wait)
                total_waited += wait
                continue

            client = self.clients[idx]
            try:
                start_time = time.time()
                user_content = (
                    f"Package: {pkg_name}\n"
                    f"Target Class: {class_name}\n\n"
                    f"Target Java Source Code:\n```java\n{target_code}\n```"
                )

                response = client.chat.completions.create(
                    model=model_name,
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": user_content}
                    ],
                    temperature=0.2,
                    max_tokens=MAX_TOKENS,
                )
                gen_time = round(time.time() - start_time, 2)

                if not response or getattr(response, 'choices', None) is None or len(response.choices) == 0:
                    raise ValueError("API returned invalid response: choices is None or empty")

                choice = response.choices[0]
                finish_reason = getattr(choice, 'finish_reason', None)
                
                if str(finish_reason).lower() in ["length", "max_tokens", "max_token"]:
                    raise ValueError(f"Output truncated by token limit (finish_reason={finish_reason})")

                output_text = choice.message.content
                if not output_text:
                    raise ValueError("API returned empty content")

                usage = getattr(response, 'usage', None)
                prompt_tokens = getattr(usage, 'prompt_tokens', 0)
                completion_tokens = getattr(usage, 'completion_tokens', 0)
                return clean_java_code(output_text), gen_time, prompt_tokens, completion_tokens

            except Exception as e:
                kind, cooldown = self._classify_error(e)
                if kind == "fatal":
                    raise
                log(f"[WARNING] Key {idx + 1}/{len(self.keys)} [{kind}] {type(e).__name__}: "
                    f"{str(e)[:120]} -> rotating (cooldown {int(cooldown)}s)")
                self._mark_key(idx, cooldown)


# =====================================================================
#  2. DEFECTS4J HELPERS & TEST METHOD EXTRACTION (สวมส่วนเพื่อน)
# =====================================================================
def run_cmd(cmd, cwd=None, timeout=None):
    try:
        res = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        return -1, "", f"TIMEOUT after {timeout}s running: {' '.join(cmd)}"
    return res.returncode, res.stdout.strip(), res.stderr.strip()


def extract_test_methods(java_code):
    """
    ดึงชื่อเมธอดที่มี Annotation @Test ออกจากโค้ด Java (นำมาจากสคริปต์เพื่อน)
    """
    test_methods = []
    lines = java_code.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        if line.startswith("@Test") or "@Test" in line:
            j = i + 1
            while j < len(lines):
                next_line = lines[j].strip()
                if next_line.startswith("@"):
                    j += 1
                    continue
                m = re.search(r'public\s+void\s+(\w+)\s*\(', next_line)
                if m:
                    test_methods.append(m.group(1))
                    break
                j += 1
            i = j
        else:
            i += 1
    return list(dict.fromkeys(test_methods))


def get_all_projects():
    code, out, _ = run_cmd(["defects4j", "pids"])
    if code == 0 and out:
        return [p.strip() for p in out.splitlines() if p.strip()]
    return ["Chart", "Cli", "Closure", "Codec", "Collections", "Compress", "Csv", "Gson", "JacksonCore",
            "JacksonDatabind", "JacksonXml", "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"]


def get_bug_ids(project_id):
    code, out, _ = run_cmd(["defects4j", "bids", "-p", project_id])
    if code == 0 and out:
        return [b.strip() for b in out.splitlines() if b.strip()]
    return []


def export_d4j_property(prop_name, work_dir):
    code, out, _ = run_cmd(["defects4j", "export", "-p", prop_name], cwd=work_dir)
    return out if code == 0 else ""


def get_d4j_properties_fast(work_dir):
    props = {}
    prop_file = os.path.join(work_dir, "defects4j.build.properties")
    if os.path.exists(prop_file):
        with open(prop_file, "r", encoding="utf-8", errors="ignore") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    props[k.strip()] = v.strip()

    modified_str = props.get("d4j.classes.modified")
    test_src_dir = props.get("d4j.dir.src.tests")
    main_src_dir = props.get("d4j.dir.src.classes")

    if not modified_str:
        modified_classes = export_d4j_property("classes.modified", work_dir).splitlines()
    else:
        modified_classes = [c.strip() for c in modified_str.split(",") if c.strip()]
        
    if not test_src_dir:
        test_src_dir = export_d4j_property("dir.src.tests", work_dir)
    if not main_src_dir:
        main_src_dir = export_d4j_property("dir.src.classes", work_dir)

    return modified_classes, test_src_dir, main_src_dir


# =====================================================================
#  3. HELPERS
# =====================================================================
def clean_java_code(raw_code):
    text = raw_code.strip()
    match = re.search(r'```(?:java)?\s*(.*?)\s*```', text, flags=re.DOTALL | re.IGNORECASE)
    if match:
        text = match.group(1).strip()
    
    class_start = re.search(r'^(package|import|public\s+class|class)', text, flags=re.MULTILINE)
    if class_start:
        text = text[class_start.start():]
        
    return text.strip() + "\n"


def extract_class_and_package(java_code):
    pkg_match = re.search(r'package\s+([\w\.]+);', java_code)
    package_name = pkg_match.group(1) if pkg_match else ""
    class_match = re.search(r'public\s+(?:abstract\s+)?class\s+(\w+)', java_code)
    class_name = class_match.group(1) if class_match else "TargetClass"
    return package_name, class_name


def run_defects4j_test_method(project_dir, full_test_class, test_method=None):
    """
    รองรับการสั่งรันทั้งระดับ Class หรือ เจาะจงเฉพาะ Method (Class#method)
    """
    code_c, out_c, err_c = run_cmd(["defects4j", "compile"], cwd=project_dir, timeout=TIMEOUT_COMPILE)
    if code_c != 0:
        return False, f"COMPILE ERROR:\n{err_c or out_c}"

    test_target = f"{full_test_class}::{test_method}" if test_method else full_test_class
    code_t, out_t, err_t = run_cmd(["defects4j", "test", "-t", test_target],
                                   cwd=project_dir, timeout=TIMEOUT_TEST)
    if "Failing tests: 0" in out_t or code_t == 0:
        return True, f"PASS:\n{out_t}"
    return False, f"TEST FAILURE:\n{out_t or err_t}"

def get_defects4j_coverage(work_dir, full_class_name, full_test_class, *args, **kwargs):
    default_res = {
        "line": 0.0, "branch": 0.0,
        "line_coverage": 0.0, "branch_coverage": 0.0,
        "line_cov": 0.0, "branch_cov": 0.0,
        "lines_covered": 0, "lines_total": 0,
        "branches_covered": 0, "branches_total": 0
    }
    
    if not ENABLE_COVERAGE:
        return default_res

    # --- แปลงข้อมูล test_target ---
    test_str = str(full_test_class).strip()
    
    if "/" in test_str or "\\" in test_str or test_str.endswith(".java"):
        base_filename = os.path.basename(test_str)
        if base_filename.endswith(".java"):
            base_filename = base_filename[:-5]
        
        if "." in full_class_name:
            pkg_prefix = full_class_name.rsplit(".", 1)[0]
            test_target = f"{pkg_prefix}.{base_filename}"
        else:
            test_target = base_filename
    else:
        test_target = test_str

    inst_file = os.path.abspath(os.path.join(work_dir, "instrument_classes.txt"))
    try:
        with open(inst_file, "w", encoding="utf-8") as f:
            f.write(full_class_name + "\n")

        # ตรวจสอบรูปแบบคำสั่ง:
        # Defects4J จะรับค่า -t เฉพาะเมื่อมีรูปแบบ Class::method เท่านั้น
        # หากเป็นการรันระดับคลาส (ไม่มี ::) จะไม่ใส่แฟล็ก -t
        cmd = ["defects4j", "coverage", "-w", work_dir, "-i", inst_file]
        if "::" in test_target:
            cmd.extend(["-t", test_target])

        code, out, err = run_cmd(cmd, timeout=TIMEOUT_COVERAGE)
        if code != 0:
            log(f"  [WARN] Coverage command failed/timeout: {(err or out)[:200]}")
            return default_res

        coverage_xml_path = os.path.join(work_dir, "coverage.xml")
        if os.path.exists(coverage_xml_path):
            root = ET.parse(coverage_xml_path).getroot()
            
            lines_covered = int(root.attrib.get('lines-covered', 0))
            lines_total = int(root.attrib.get('lines-valid', 0))
            branches_covered = int(root.attrib.get('branches-covered', 0))
            branches_total = int(root.attrib.get('branches-valid', 0))
            
            line_rate = float(root.attrib.get('line-rate', 0.0)) * 100
            branch_rate = float(root.attrib.get('branch-rate', 0.0)) * 100
            
            l_cov = round(line_rate, 2)
            b_cov = round(branch_rate, 2)
            
            return {
                "line": l_cov, "branch": b_cov,
                "line_coverage": l_cov, "branch_coverage": b_cov,
                "line_cov": l_cov, "branch_cov": b_cov,
                "lines_covered": lines_covered, "lines_total": lines_total,
                "branches_covered": branches_covered, "branches_total": branches_total
            }

    except Exception as e:
        log(f"  [WARN] Coverage error: {e}")

    return default_res

def save_metrics(model, project_id, bug_id, class_name, test_method, gen_time, p_tokens, c_tokens, status, retries,
                 line_cov=0.0, branch_cov=0.0):
    with CSV_LOCK:
        os.makedirs("results", exist_ok=True)
        file_exists = os.path.isfile("results/benchmark_data.csv")
        with open("results/benchmark_data.csv", "a", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            if not file_exists:
                writer.writerow(["Model", "Project", "BugID", "TargetClass", "TestMethod", "GenTime(s)", "PromptTokens",
                                 "CompletionTokens", "Status", "Retries", "LineCoverage(%)", "BranchCoverage(%)"])
            writer.writerow([model, project_id, bug_id, class_name, test_method, gen_time, p_tokens, c_tokens,
                             status, retries, line_cov, branch_cov])


# =====================================================================
#  4. PROCESS ONE BUG
# =====================================================================
def process_bug(project_id, bug_id, models, key_manager, system_prompt, checkout_base_dir):
    if STOP.is_set():
        return

    tag = f"{project_id}_{bug_id}"

    all_done = True
    for model_label in models.keys():
        result_dir = os.path.join(model_label, "Result", project_id, str(bug_id))
        if not os.path.exists(result_dir) or not os.listdir(result_dir):
            all_done = False
            break
    if all_done:
        log(f"[SKIP] Bug {tag} already completed for all models.")
        return

    work_dir = os.path.join(checkout_base_dir, tag)
    if os.path.exists(work_dir):
        shutil.rmtree(work_dir, ignore_errors=True)

    try:
        log(f"[INFO] Checking out {tag}...")
        code_co, _, err_co = run_cmd(
            ["defects4j", "checkout", "-p", project_id, "-v", f"{bug_id}b", "-w", work_dir],
            timeout=TIMEOUT_CHECKOUT)
        if code_co != 0:
            log(f"[ERROR] Checkout failed for {tag}: {err_co}")
            return

        modified_classes, test_src_dir, main_src_dir = get_d4j_properties_fast(work_dir)

        if not modified_classes:
            log(f"[WARNING] No modified classes found for {tag}. Skipping.")
            return

        for full_class_name in modified_classes:
            if STOP.is_set():
                return
            full_class_name = full_class_name.strip()
            if not full_class_name:
                continue

            rel_path = full_class_name.replace(".", "/") + ".java"
            target_file_path = os.path.join(work_dir, main_src_dir, rel_path)
            if not os.path.exists(target_file_path):
                log(f"[WARNING] File not found: {target_file_path}. Skipping.")
                continue

            with open(target_file_path, "r", encoding="utf-8", errors="ignore") as f:
                target_code = f.read()

            pkg_name, class_name = extract_class_and_package(target_code)
            test_class_name = f"{class_name}Test"
            full_test_class = f"{pkg_name}.{test_class_name}" if pkg_name else test_class_name

            pkg_path = pkg_name.replace(".", "/") if pkg_name else ""
            target_test_dir_in_d4j = os.path.join(work_dir, test_src_dir, pkg_path)
            os.makedirs(target_test_dir_in_d4j, exist_ok=True)
            d4j_test_file_path = os.path.join(target_test_dir_in_d4j, f"{test_class_name}.java")

            for model_label, model_id in models.items():
                result_dir = os.path.join(model_label, "Result", project_id, str(bug_id))
                log_file_path = os.path.join(result_dir, f"{test_class_name}_log.txt")
                if os.path.exists(log_file_path):
                    log(f"[SKIP] [{model_label}] {tag} {test_class_name} already completed.")
                    continue

                log(f"[INFO] [{tag}] Model: [{model_label}] | Target: {full_class_name}")

                retries = 0
                current_target = target_code
                api_gave_up = False

                while retries <= MAX_RETRIES:
                    try:
                        test_code, gen_time, p_tokens, c_tokens = key_manager.generate_code(
                            model_id, system_prompt, current_target, pkg_name=pkg_name, class_name=class_name)
                    except AllKeysExhaustedError:
                        raise
                    except Exception as e:
                        log(f"  [SKIP-API] [{model_label}] {tag} {test_class_name}: "
                            f"{type(e).__name__}: {str(e)[:200]}")
                        api_gave_up = True
                        break

                    timestamp = time.strftime("%Y%m%d_%H%M%S")
                    testcode_dir = os.path.join(model_label, "TestCode", project_id, str(bug_id))
                    prompt_dir = os.path.join(model_label, "Prompt", project_id, str(bug_id))
                    os.makedirs(testcode_dir, exist_ok=True)
                    os.makedirs(prompt_dir, exist_ok=True)
                    os.makedirs(result_dir, exist_ok=True)

                    with open(os.path.join(testcode_dir, f"{test_class_name}.java"), "w", encoding="utf-8") as f:
                        f.write(test_code)
                    with open(os.path.join(prompt_dir, f"{class_name}_{timestamp}.txt"), "w", encoding="utf-8") as f:
                        f.write(f"--- SYSTEM PROMPT ---\n{system_prompt}\n\n--- TARGET CODE ---\n{current_target}")
                    with open(d4j_test_file_path, "w", encoding="utf-8") as f:
                        f.write(test_code)

                    # --- ส่วนสวมกลไกขยายผลรายเมธอดของเพื่อน ---
                    extracted_methods = extract_test_methods(test_code)
                    
                    if extracted_methods:
                        log(f"  [EXEC] Found {len(extracted_methods)} @Test methods in {test_class_name}. Testing each method...")
                        overall_pass = True
                        combined_logs = []

                        for m_name in extracted_methods:
                            m_status, m_log = run_defects4j_test_method(work_dir, full_test_class, m_name)
                            combined_logs.append(f"=== METHOD: {m_name} ===\n{m_log}\n")
                            
                            line_cov, branch_cov = (0.0, 0.0)
                            if m_status and ENABLE_COVERAGE:
                                cov_res = get_defects4j_coverage(work_dir, full_class_name, full_test_class)
                                line_cov = cov_res.get("line_coverage", 0.0) if isinstance(cov_res, dict) else cov_res[0]
                                branch_cov = cov_res.get("branch_coverage", 0.0) if isinstance(cov_res, dict) else cov_res[1]

                            save_metrics(model_label, project_id, bug_id, class_name, m_name, gen_time, p_tokens,
                                         c_tokens, "PASS" if m_status else "FAIL", retries, line_cov, branch_cov)

                            if not m_status:
                                overall_pass = False

                        out_log = "\n".join(combined_logs)
                        status = overall_pass
                    else:
                        # Fallback กรณีสแกนไม่พบเมธอด @Test แยกเป็นรายอัน
                        status, out_log = run_defects4j_test_method(work_dir, full_test_class)
                        line_cov, branch_cov = (0.0, 0.0)
                        if status and ENABLE_COVERAGE:
                           cov_res = get_defects4j_coverage(work_dir, full_class_name, full_test_class)
                        line_cov = cov_res.get("line_coverage", 0.0) if isinstance(cov_res, dict) else cov_res[0]
                        branch_cov = cov_res.get("branch_coverage", 0.0) if isinstance(cov_res, dict) else cov_res[1]
                        
                        save_metrics(model_label, project_id, bug_id, class_name, "ALL", gen_time, p_tokens,
                                     c_tokens, "PASS" if status else "FAIL", retries, line_cov, branch_cov)

                    with open(log_file_path, "w", encoding="utf-8") as f:
                        f.write(out_log)

                    if status:
                        log(f"  [SUCCESS] [{tag}][{model_label}] PASS on attempt {retries + 1}")
                        break
                    else:
                        log(f"  [FAIL] [{tag}][{model_label}] Failed attempt {retries + 1}.")
                        retries += 1
                        if retries > MAX_RETRIES:
                            log(f"  [ERROR] [{tag}][{model_label}] Max retries reached for {class_name}.")
                            break
                        current_target = (f"Original Target Code:\n{target_code}\n\n"
                                          f"Previous Generated Code Failure Log:\n{out_log}\n\n"
                                          f"Please fix the generated test code.")

                if api_gave_up:
                    continue

    except AllKeysExhaustedError:
        STOP.set()
        raise
    except Exception as e:
        log(f"[ERROR] Unexpected failure while processing {tag}: {e}. Skipping to next bug.")
    finally:
        shutil.rmtree(work_dir, ignore_errors=True)


# =====================================================================
#  5. MAIN
# =====================================================================
def run_full_benchmark():
    base_url = os.getenv("KKU_API_BASE_URL")
    key_manager = KKUKeyManager(base_url)

    prompt_path = "prompts/system_prompt.txt"
    if not os.path.exists(prompt_path):
        raise FileNotFoundError(f"[ERROR] System prompt file not found at: {prompt_path}")
    with open(prompt_path, "r", encoding="utf-8") as f:
        system_prompt = f.read()

    models = {
        "DeepSeek": "deepseek-v4-pro",
        "Gemini": "gemini-3.7-flash",
    }

    projects = get_all_projects()
    log(f"[INFO] Found {len(projects)} Projects in Defects4J: {projects}")

    per_project = []
    for project_id in projects:
        bug_ids = get_bug_ids(project_id)
        if MAX_BUGS_PER_PROJECT > 0:
            bug_ids = bug_ids[:MAX_BUGS_PER_PROJECT]
        per_project.append([(project_id, b) for b in bug_ids])

    tasks = []
    for i in range(max((len(p) for p in per_project), default=0)):
        for p in per_project:
            if i < len(p):
                tasks.append(p[i])

    total = len(tasks)
    log(f"[INFO] Total bugs to process: {total} | Workers: {WORKERS} | Max retries: {MAX_RETRIES}")

    os.makedirs(CHECKOUT_BASE_DIR, exist_ok=True)

    done = 0
    stop_error = None
    with ThreadPoolExecutor(max_workers=WORKERS) as ex:
        futures = [ex.submit(process_bug, p, b, models, key_manager, system_prompt, CHECKOUT_BASE_DIR)
                   for p, b in tasks]
        for fut in as_completed(futures):
            try:
                fut.result()
            except AllKeysExhaustedError as e:
                STOP.set()
                stop_error = stop_error or e
            except Exception as e:
                log(f"[ERROR] Worker crashed: {e}")
            done += 1
            log(f"[PROGRESS] {done}/{total} bugs finished")

    if stop_error:
        raise stop_error


if __name__ == "__main__":
    try:
        run_full_benchmark()
    except AllKeysExhaustedError as e:
        print(f"\n[STOP] {e}")
        print("[STOP] ทุกคีย์ใช้ไม่ได้นานเกินกำหนด หยุดอย่างปลอดภัย — เติมโควตา/รอ แล้วรันสคริปต์ซ้ำเพื่อทำต่อจากจุดเดิม")