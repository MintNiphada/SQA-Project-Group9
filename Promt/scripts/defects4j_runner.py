import os
import re
import time
import csv
import shutil
import threading
import subprocess
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
import httpx
import openai
from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()

# =====================================================================
#  SETTINGS (ปรับผ่าน .env ได้)
# =====================================================================
WORKERS = int(os.getenv("WORKERS", 4))
MAX_BUGS_PER_PROJECT = int(os.getenv("MAX_BUGS_PER_PROJECT", 0))
MAX_RETRIES = int(os.getenv("MAX_RETRIES", 0))
MAX_TOKENS = int(os.getenv("MAX_TOKENS",4096))  # เพิ่มขีดจำกัดเป็น 8192 ป้องกันโดนตัดจบ

# --- Subprocess timeouts (seconds) ---
TIMEOUT_CHECKOUT = int(os.getenv("TIMEOUT_CHECKOUT", 300))
TIMEOUT_COMPILE = int(os.getenv("TIMEOUT_COMPILE", 600))
TIMEOUT_TEST = int(os.getenv("TIMEOUT_TEST", 300))
TIMEOUT_COVERAGE = int(os.getenv("TIMEOUT_COVERAGE", 400))

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
#  1. KEY MANAGER (thread-safe, per-key cooldown, round-robin)
# =====================================================================
class KKUKeyManager:
    def __init__(self, keys_string, base_url):
        if not keys_string:
            raise ValueError("[ERROR] KKU_API_KEYS not found in .env file. Please check configuration.")
        self.keys = [k.strip() for k in keys_string.split(",") if k.strip()]
        self.base_url = base_url
        self.current_index = 0
        self.cooldown_until = [0.0] * len(self.keys)
        self.lock = threading.Lock()

        self.clients = []
        for key in self.keys:
            http_client = httpx.Client(
                timeout=httpx.Timeout(
                    connect=30.0,
                    read=240.0,
                    write=30.0,
                    pool=60.0
                ),
                limits=httpx.Limits(max_keepalive_connections=20, max_connections=40),
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
                
                # ฟอร์แมต User Prompt ให้สอดคล้องกับโครงสร้าง Package และ Class Name
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
                    max_tokens=MAX_TOKENS,  # ปรับเพิ่มเป็น 8192 ป้องกันการโดนตัดจบ
                )
                gen_time = round(time.time() - start_time, 2)

                if not response or getattr(response, 'choices', None) is None or len(response.choices) == 0:
                    raise ValueError("API returned invalid response: choices is None or empty")

                choice = response.choices[0]
                finish_reason = getattr(choice, 'finish_reason', None)
                
                # ตรวจจับ output ที่โดนตัดจบกลางคันเนื่องจากติด token limit
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
#  2. DEFECTS4J HELPERS
# =====================================================================
def run_cmd(cmd, cwd=None, timeout=None):
    try:
        res = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        return -1, "", f"TIMEOUT after {timeout}s running: {' '.join(cmd)}"
    return res.returncode, res.stdout.strip(), res.stderr.strip()


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


# =====================================================================
#  3. HELPERS
# =====================================================================
def clean_java_code(raw_code):
    text = raw_code.strip()
    
    # 1. ดึงข้อความภายใน Markdown Code Block (ถ้ามี)
    match = re.search(r'```(?:java)?\s*(.*?)\s*```', text, flags=re.DOTALL | re.IGNORECASE)
    if match:
        text = match.group(1).strip()
    
    # 2. ตัดคำอธิบายเกริ่นนำ โดยเริ่มสกัดตั้งแต่ keyword หลักของไฟล์ Java
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


def run_defects4j_test(project_dir, full_test_class):
    code_c, out_c, err_c = run_cmd(["defects4j", "compile"], cwd=project_dir, timeout=TIMEOUT_COMPILE)
    if code_c != 0:
        return False, f"COMPILE ERROR:\n{err_c or out_c}"

    code_t, out_t, err_t = run_cmd(["defects4j", "test", "-t", full_test_class],
                                   cwd=project_dir, timeout=TIMEOUT_TEST)
    if "Failing tests: 0" in out_t or code_t == 0:
        return True, f"PASS:\n{out_t}"
    return False, f"TEST FAILURE:\n{out_t or err_t}"


def get_defects4j_coverage(work_dir, full_class_name, full_test_class):
    inst_file = os.path.abspath(os.path.join(work_dir, "instrument_classes.txt"))
    try:
        with open(inst_file, "w", encoding="utf-8") as f:
            f.write(full_class_name + "\n")

        code, out, err = run_cmd(
            ["defects4j", "coverage", "-w", work_dir, "-t", full_test_class, "-i", inst_file],
            timeout=TIMEOUT_COVERAGE,
        )
        if code != 0:
            log(f"  [WARN] Coverage command failed/timeout: {(err or out)[:200]}")
            return 0.0, 0.0

        coverage_xml_path = os.path.join(work_dir, "coverage.xml")
        if os.path.exists(coverage_xml_path):
            root = ET.parse(coverage_xml_path).getroot()
            line_rate = float(root.attrib.get('line-rate', 0.0)) * 100
            branch_rate = float(root.attrib.get('branch-rate', 0.0)) * 100
            return round(line_rate, 2), round(branch_rate, 2)
    except Exception as e:
        log(f"  [WARN] Coverage error: {e}")
    return 0.0, 0.0


def save_metrics(model, project_id, bug_id, class_name, gen_time, p_tokens, c_tokens, status, retries,
                 line_cov=0.0, branch_cov=0.0):
    with CSV_LOCK:
        os.makedirs("results", exist_ok=True)
        file_exists = os.path.isfile("results/benchmark_data.csv")
        with open("results/benchmark_data.csv", "a", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            if not file_exists:
                writer.writerow(["Model", "Project", "BugID", "TargetClass", "GenTime(s)", "PromptTokens",
                                 "CompletionTokens", "Status", "Retries", "LineCoverage(%)", "BranchCoverage(%)"])
            writer.writerow([model, project_id, bug_id, class_name, gen_time, p_tokens, c_tokens,
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

        modified_classes = export_d4j_property("classes.modified", work_dir).splitlines()
        test_src_dir = export_d4j_property("dir.src.tests", work_dir)
        main_src_dir = export_d4j_property("dir.src.classes", work_dir)

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

                    status, out_log = run_defects4j_test(work_dir, full_test_class)

                    with open(log_file_path, "w", encoding="utf-8") as f:
                        f.write(out_log)

                    if status:
                        log(f"  [SUCCESS] [{tag}][{model_label}] PASS on attempt {retries + 1}")
                        line_cov, branch_cov = get_defects4j_coverage(work_dir, full_class_name, full_test_class)
                        log(f"  [COVERAGE] [{tag}][{model_label}] Line: {line_cov}% | Branch: {branch_cov}%")
                        save_metrics(model_label, project_id, bug_id, class_name, gen_time, p_tokens, c_tokens,
                                     "PASS", retries, line_cov, branch_cov)
                        break
                    else:
                        log(f"  [FAIL] [{tag}][{model_label}] Failed attempt {retries + 1}.")
                        retries += 1
                        if retries > MAX_RETRIES:
                            log(f"  [ERROR] [{tag}][{model_label}] Max retries reached for {class_name}.")
                            save_metrics(model_label, project_id, bug_id, class_name, gen_time, p_tokens,
                                         c_tokens, "FAIL", retries, 0.0, 0.0)
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
    keys_str = os.getenv("KKU_API_KEYS")
    key_manager = KKUKeyManager(keys_str, base_url)

    prompt_path = "prompts/system_prompt.txt"
    if not os.path.exists(prompt_path):
        raise FileNotFoundError(f"[ERROR] System prompt file not found at: {prompt_path}")
    with open(prompt_path, "r", encoding="utf-8") as f:
        system_prompt = f.read()

    # ตรวจสอบชื่อรุ่นโมเดลให้ตรงกับระบบของ KKU API
    models = {
        #"Gemini": "gemini-3.7-flash",
        "DeepSeek": "deepseek-v4-pro",
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

    checkout_base_dir = "defects4j_checkout"
    os.makedirs(checkout_base_dir, exist_ok=True)

    done = 0
    stop_error = None
    with ThreadPoolExecutor(max_workers=WORKERS) as ex:
        futures = [ex.submit(process_bug, p, b, models, key_manager, system_prompt, checkout_base_dir)
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