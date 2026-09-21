# CATG Progress - 21 Sep 2026

## 1. Environment

* OS: Windows 11
* CATG location: `/d/Lab_SQA/CATG`
* Java: JDK 8
* CVC4: 1.8
* CVC4 executable: `tools/cvc4/cvc4.exe`
* CATG JAR: `build/libs/CATG-0.2.jar`

> CATG เป็นอัลกอริทึมที version ที่ใช้เป็น version เก่า จึงใช้ Java 8 
> โดยไม่ควรเปลี่ยน Java ของเครื่องเป็น version 8 ทั้งหมด เพราะ Defects4J ใช้ version 11

---

## 2. CATG Configuration

Main configuration file:

```text
catg.conf
```

Important configuration:

```text
catg.solverClass=janala.solvers.CVC4Solver
catg.strategyClass=janala.solvers.AbstractRefineStrategy
catg.inputsFile=inputs
catg.historyFile=history
catg.formulaFile=formula
catg.coverageFile=coverage.catg
```

โดย

* `CVC4Solver`  ใช้ CVC4 เป็น constraint solver
* `AbstractRefineStrategy`  ใช้สำหรับค้นหา path ใหม่
* `inputs`  เก็บ generated input
* `history` เก็บ path/search history
* `formula`  เก็บ constraints
* `coverage.catg`  เก็บ coverage information

---

## 3. Build & Check CATG

Check CATG JAR:

```bash
ls -lh build/libs/CATG-0.2.jar
```

Check integration test class:

```bash
ls -lh build/classes/java/integration/tests/test_q1_q4_min.class
```

Check CVC4:

```bash
which cvc4
cvc4 --version
```

Expected:

```text
CVC4 version 1.8
```

---

## 4. Test Program

ใช้ test program สำหรับ verify ว่า CATG สามารถทำ concolic execution และ generate input ได้:

```text
src/integration/java/tests/test_q1_q4_min.java
```

Main class:

```text
tests.test_q1_q4_min
```

Test program มี symbolic boolean inputs 3 ตัว:

```java
current_class.e = CATG.readBool(false);
current_class.b = CATG.readBool(false);
current_class.c = CATG.readBool(false);
```

และตรวจสอบ condition:

```java
if (!((b == true) || (e == true) || (c == true))) {
    System.out.println("Precondition Error on q1_q4");
}
```

ดังนั้น CATG จะสามารถ explore combination ของ `e`, `b`, `c` ได้

---

## 5. `dconcolic` Launcher

ปรับ `dconcolic` ให้สามารถ run กับ CATG ที่ build ด้วย Gradle ในปัจจุบัน

ใช้ CATG JAR:

```text
build/libs/CATG-0.2.jar
```

และ integration classes:

```text
build/classes/java/integration
```

Classpath ที่ใช้งานได้:

```bash
-cp "build/classes/java/integration;build/libs/CATG-0.2.jar;lib/*"
```

`dconcolic` จะกำหนด Java 8 ก่อน run CATG:

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-8.0.504.1-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## 6. How to Run

ก่อนเริ่ม experiment ใหม่ ให้ลบ generated files จากรอบก่อน:

```bash
rm -f inputs history history.old test.log
```

สร้าง initial input:

```bash
printf "1\n1\n1\n" > inputs
```

Run CATG:

```bash
./dconcolic 3 tests.test_q1_q4_min
```

รูปแบบ command:

```text
./dconcolic <iterations> <main-class>
```

ตัวอย่าง:

```bash
./dconcolic 3 tests.test_q1_q4_min
```

หมายถึงให้ CATG ทำงานทั้งหมด 3 iterations

---

## 7. Current Result

จากการ run ล่าสุด:

```text
Now testing tests.test_q1_q4_min

========== Input 1 ==========
--- Generated input ---
1
0
1
--- History generated ---

========== Input 2 ==========
--- Generated input ---
0
0
1
--- History generated ---

========== Input 3 ==========
--- Generated input ---
0
0
0
--- History generated ---

========== CATG finished ==========
```

Final `inputs`:

```text
0
0
0
```

Generated input sequence:

```text
Initial input : 1 1 1
Iteration 1   : 1 0 1
Iteration 2   : 0 0 1
Iteration 3   : 0 0 0
```

### Result

CATG สามารถ:

* Execute test program
* Read symbolic inputs
* Generate new input values
* Explore different execution paths
* Save `history` สำหรับใช้ในการ search ต่อ

ดังนั้น **CATG environment และ basic concolic input generation ทำงานได้แล้ว**

---

## 8. Important Finding

ปัญหาก่อนหน้า:

```text
Error: inputs (The system cannot find the file specified)
```

สาเหตุคือ `dconcolic` ลบ `inputs` ก่อน execution ครั้งแรก

จึงเปลี่ยน workflow เป็น:

```bash
rm -f inputs history history.old test.log
printf "1\n1\n1\n" > inputs
./dconcolic 3 tests.test_q1_q4_min
```

ตอนนี้ CATG สามารถอ่าน initial input และ generate input ใหม่ได้

---

## 9. Important: Meaning of `history`

`history` ไม่ได้หมายถึง test pass/fail

`history` ใช้เก็บ information เกี่ยวกับ:

* execution paths
* branch information
* path constraints
* search state

ดังนั้น:

```text
history exists ≠ test failed
history does not exist ≠ test passed
```

จึงไม่ควรใช้การมี/ไม่มี `history` เป็นตัวตัดสินว่า test ผ่านหรือไม่

---

## 10. Current Status

| Component                     | Status     |
| ----------------------------- | ---------- |
| Java 8                        |  Working  |
| CVC4 1.8                      |  Working  |
| CATG JAR                      |  Working  |
| `catg.conf`                   |  Working  |
| `dconcolic`                   |  Working  |
| Test class                    |  Working  |
| Initial `inputs`              |  Working  |
| Generated inputs              |  Working  |
| `history` generation          |  Working  |
| Basic CATG concolic execution |  Verified |

---

## 11. Next Step: Apply CATG to Defects4J

ตอนนี้ `test_q1_q4_min` ใช้สำหรับ **verify CATG environment** เท่านั้น

ขั้นต่อไปคือนำ CATG ไปใช้กับ selected Defects4J bug

Process :

```text
Defects4J Bug
      ↓
Checkout BUGGY version
      ↓
Compile project
      ↓
Identify target Class / Method
      ↓
Create CATG-compatible test harness
      ↓
Run CATG
      ↓
Generate test inputs
      ↓
Convert generated inputs to Test Cases
      ↓
Run generated tests on BUGGY version
      ↓
Check whether the bug is triggered
      ↓
Run the same tests on FIXED version
      ↓
Compare results
      ↓
Collect Coverage / Bug Detection results
```

---

# 12. Instructions for Teammates

## Step 1: Setup CATG

Clone project repository and enter the CATG directory (ตัวอย่าง Path):

```bash
cd /d/Lab_SQA/CATG
```

CATG:
- CATG location: /d/Lab_SQA/CATG
- CATG is the concolic testing engine used to generate test inputs.

CVC4:
- CVC4 version: 1.8
- CVC4 is used as the constraint solver for CATG.
- CVC4 executable: /d/Lab_SQA/CATG/tools/cvc4/cvc4.exe
```

## Step 2: Set Java 8

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-8.0.504.1-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
```

Check:

```bash
java -version
```

ต้องเป็น Java 8

## Step 3: Check CVC4

```bash
which cvc4
cvc4 --version
```

ต้องได้ CVC4 1.8

## Step 4: Check CATG JAR

```bash
ls -lh build/libs/CATG-0.2.jar
```

## Step 5: Check test class

```bash
ls -lh build/classes/java/integration/tests/test_q1_q4_min.class
```

## Step 6: Prepare initial input

```bash
rm -f inputs history history.old test.log
printf "1\n1\n1\n" > inputs
```

## Step 7: Run CATG

```bash
./dconcolic 3 tests.test_q1_q4_min
```

## Step 8: Check generated input

```bash
cat inputs
```

ถ้า CATG ทำงานปกติ จะเห็น input ที่ถูก generate ใหม่ เช่น:

```text
0
0
0
```

สามารถตรวจสอบ generated files ได้ด้วย:

```bash
ls -lh inputs history history.old
```

---

## 13. Notes for Team

* `test_q1_q4_min` เป็นเพียง **verification test** สำหรับตรวจสอบ CATG environment
* ยังไม่ใช่ final test ของ Defects4J
* ไม่ควรแก้ CATG core เช่น `History.java` หรือ `AbstractRefineStrategy.java` ในขั้นตอนนี้
* CATG ใช้ Java 8 ส่วน Defects4J/Botsing อาจใช้ Java version อื่น
* ใช้ `JAVA_HOME` เพื่อ switch Java version แทนการเปลี่ยน Java ของเครื่องทั้งหมด
* ขั้นต่อไปคือศึกษาวิธีสร้าง **CATG test harness สำหรับ method/class ของ Defects4J** และนำ generated input ไปใช้เป็น 
test case จริง
