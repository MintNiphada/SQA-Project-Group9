# Benchmark Protocol – CP353201 Software Quality Assurance (Group 9)

**วิชา:** CP353201 Software Quality Assurance ปีการศึกษา 1/2569  
**อาจารย์:** ผศ.ดร.ชิตสุธา สุ่มเล็ก | หลักสูตรวิทยาการคอมพิวเตอร์ ม.ขอนแก่น

เอกสารนี้เป็นข้อกำหนดกลาง (single source of truth) ของการส่งงานรอบที่ 2 เพื่อให้ Algorithm 2 ตัว และ AI 2 ตัว ใช้ target, environment, budget และเกณฑ์ประเมินเดียวกัน
เพื่อให้ผลลัพธ์เปรียบเทียบกันได้และผู้อื่นสามารถทำซ้ำได้

---

## 1. เครื่องมือที่ใช้

| ประเภท | เครื่องมือ | หลักการ |
|---|---|---|
| Algorithm 1 | **CATG** (Concolic Testing) | Concolic execution + constraint solving (CVC4) |
| Algorithm 2 | **Botsing** (Search-Based) | Search-based crash reproduction (อิง EvoSuite/DynaMOSA) |
| AI Tool 1 | **ChatGPT (GPT-4o)** | Prompt-based unit test generation |
| AI Tool 2 | **Gemini** | Prompt-based unit test generation |

> ทั้ง ChatGPT และ Gemini ใช้โทเคนผ่านแพลตฟอร์ม KKU IntelSphere (ai.kku.ac.th)

---

## 2. ชุดข้อมูล (Dataset)

- **Defects4J** version ที่ติดตั้ง: `v3.0.1` (ตัวอย่างในที่นี้ติดตั้งที่ `D:\Lab_SQA\defects4j`)
- **Projects ทั้งหมด:** Chart, Cli, Closure, Codec, Collections, Compress, Csv, Gson,
  JacksonCore, JacksonDatabind, JacksonXml, Jsoup, JxPath, Lang, Math, Mockito, Time
  (17 projects / 854 active bugs)
- **Active bugs ที่นับ:** ใช้ `defects4j bids -p <project>` เฉพาะ bug ที่ยัง active
- **เป้าหมาย:** สร้างทดสอบบน Target Modified Classes (`classes.modified`)
  ซึ่งเป็นคลาสที่มีข้อบกพร่องจริงตาม ground truth ของ Defects4J

### โหมดการรัน
1. **Sample Benchmark**: เลือก bug ตัวแทน ~5 bug (Lang-1, Math-*, Time-*) สำหรับ pilot
2. **Full Benchmark**: รันทุก active bug ใน project ที่เครื่องมือทำได้
   (document ข้อจำกัดที่รันไม่ได้ เช่น CATG กับ class ที่มี state ซับซ้อน)

---

## 3. Environment (ทุกเครื่องต้องเหมือนกัน)

| Component | ค่าที่ใช้ |
|---|---|
| OS | Windows 11 |
| Java สำหรับ Defects4J | JDK 11 (`C:\Program Files\Eclipse Adoptium\jdk-11.0.32.101-hotspot`) |
| Java สำหรับ CATG | JDK 8 (`C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot`) |
| Solver | CVC4 1.8 (`D:\Lab_SQA\CATG\tools\cvc4\cvc4.exe`) |
| Defects4J HOME | `D:\Lab_SQA\defects4j` |
| Project checkouts | `D:\Lab_SQA\Defects4J_Projects\<Project>_<BugID>_buggy/fixed` |

การสลับ Java ให้ใช้ `JAVA_HOME` + `PATH` ต่อ session เท่านั้น ห้ามแก้ Java ทั้งเครื่อง
(Defects4J ต้องใช้ 11 แต่ CATG ต้องใช้ 8)

### Critical: Git Bash ต้องมาก่อน WSL bash ใน PATH

Defects4J `ant.cmd` เรียก `bash -c` ด้วย path สไตล์ Unix (`/d/...`) ซึ่ง WSL bash
(`C:\WINDOWS\system32\bash.exe`) ตีความไม่ได้ → ถ้าไม่แก้ จะเจอ error
`No such file or directory` และทุก command ที่ต้อง compile จะ FAIL

```powershell
$env:PATH = "C:\Program Files\Git\bin;$env:PATH"   # Git Bash มาก่อน WSL bash
```

ตรวจสอบก่อนรัน Defects4J ทุกครั้ง: `(Get-Command bash).Source` ต้องชี้ไป `C:\Program Files\Git\bin\bash.exe`

---

## 4. Budget (Configuration) — อ้างอิงข้อกำหนด 1.7

เพื่อวัดผลกระทบของ budget ต่อคุณภาพ test suite แต่ละเครื่องมือรันหลาย budget:

| เครื่องมือ | Budget ที่ใช้ | หมายเหตุ |
|---|---|---|
| CATG | iterations: 10, 50, 100 | `./dconcolic <iter>` |
| Botsing | search time: 60s, 120s, 240s | `-Dsearch_budget=...` |
| ChatGPT / Gemini | effort: 1 pass, 1 feedback round | ตีความจากเวลาที่รันจริง |

ทุก config รันซ้ำ **3 ครั้ง** แล้วหาค่าเฉลี่ย (ตามข้อกำหนด 1.7)

---

## 5. Universal Test Standards

ชุดทดสอบจากทุกสายงานต้องปฏิบัติตามนี้จึงจะคอมไพล์/ประเมินบน Defects4J ได้:

1. **Framework Hygiene:** ใช้ `org.junit.Test` + `org.junit.Assert.*` เท่านั้น
   (JUnit 4, ห้าม JUnit 5 / Mocking framework ภายนอก)
2. **Package Declaration:** บรรทัดแรกของแต่ละไฟล์ test ต้องประกาศ `package`
   ให้ตรงกับ target class (เช่น `package org.apache.commons.lang3.math;`)
3. **Timeout Guard:** ทุก `@Test` ต้องมี timeout เช่น `@Test(timeout = 4000)`
4. **Deterministic:** ห้าม `System.currentTimeMillis()` / ค่าสุ่มไม่ fix seed
5. **Test class ตั้งชื่อตาม convention:**
   - CATG: `*_CATGTest.java`
   - Botsing: `*_BotsingTest.java`
   - ChatGPT: `*_ChatGPTTest.java`
   - Gemini: `*_GeminiTest.java`

---

## 6. Evaluation Metrics (เกณฑ์ประเมิน)

| Metric | คำนิยาม | วิธีวัด |
|---|---|---|
| **Line Coverage** | % บรรทัดที่ถูก execute ใน target class | `defects4j coverage` (line total) |
| **Branch Coverage** | % กิ่งเงื่อนไขที่ถูก execute | `defects4j coverage` (branch total) |
| **Fault Detection Rate (FDR)** | % ของ bug ที่ชุดทดสอบตรวจพบ = fail บน buggy + pass บน fixed | `defects4j test` / รัน test suite |
| **Test count** | จำนวน @Test ที่สร้างได้ & คอมไพล์ผ่าน | นับจาก source |
| **Efficiency** | เวลาที่ใช้สร้างชุดทดสอบ (generation time) | จับเวลา |
| **Token usage (AI)** | จำนวน token ที่ใช้ต่อชุดทดสอบ | จาก API/platform log |

> ทุก metric บันทึกต่อ (project, bug, tool, budget, repetition)

---

## 7. Workflow

```
Defects4J bug (project, bugID)
  - checkout buggy (b) และ fixed (f)
  - compile (defects4j compile)
  - ระบุ target class จาก classes.modified
  - สร้างชุดทดสอบด้วยเครื่องมือ (ตาม budget)
  - รันชุดทดสอบบน buggy version   → บันทึก pass/fail
  - รันชุดทดสอบบน fixed version    → บันทึก pass/fail
  - defects4j coverage             → บันทึก line/branch coverage
  - fault detection = fail(buggy) && pass(fixed)
  - บันทึกลง CSV (results/)
```

### ตัวอย่าง commands (Lang-1)

```bash
# JAVA 11
export JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-11.0.32.101-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
export D4J_HOME=/d/Lab_SQA/defects4j

defects4j checkout -p Lang -v 1b -w /d/Lab_SQA/Defects4J_Projects/Lang_1_buggy
defects4j checkout -p Lang -v 1f -w /d/Lab_SQA/Defects4J_Projects/Lang_1_fixed
defects4j compile -w /d/Lab_SQA/Defects4J_Projects/Lang_1_buggy
defects4j coverage -w /d/Lab_SQA/Defects4J_Projects/Lang_1_buggy -i org.apache.commons.lang3.math.NumberUtils
```

---

## 8. Structure & การบันทึกผล

```
SQA_Project_Group9/
│
├── CATG/
│   ├── Code/
│   ├── Configuration/
│   ├── Result_Round1/
│   ├── Result_Round2/
│   └── Test/
│
├── Botsing/
│   ├── Code/
│   ├── Configuration/
│   ├── Result_Round1/
│   ├── Result_Round2/
│   └── Test/
│
├── ChatGPT/
│   ├── Prompt/
│   ├── Result/
│   └── TestCode/
│
├── Gemini/
│   ├── Prompt/
│   ├── Result/
│   └── TestCode/
│
├── Comparison/
│   ├── tables/
│   └── graphs/
│
├── Report/
│
├── Presentation/
│
├── scripts/
│   ├── runner/
│   ├── coverage/
│   └── resume/
│
├── results/
│   └── all_metrics.csv
│
└── README.md
```

---

## 9. ขั้นตอนการทำซ้ำ (Reproduce)

1. ตั้ง Java 11 + `D4J_HOME`
2. checkout/compile buggy-fixed ด้วย Defects4J
3. รันเครื่องมือตาม Configuration ที่บันทึกไว้ใน `*/Configuration/`
4. นำ test suite ที่สร้างไปวางใน project แล้วรันตาม section 7
5. ตรวจผลใน `results/benchmark_results.csv`

---