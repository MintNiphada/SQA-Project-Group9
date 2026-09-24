
# Search-based Crash Reproduction Report (Defects4J - Lang_1b)

## 📌 Project Overview

* **Target Bug:** Defects4J `Lang_1b`
* **Target Class:** `org.apache.commons.lang3.math.NumberUtils`
* **Exception:** `java.lang.NumberFormatException`
* **Tool Used:** Botsing Framework
* **Target Frame:** Frame 5 (`NumberUtils.createInteger`)

---

## 🛠️ Environment Requirements

* **OS:** WSL / Ubuntu
* **JDK Version:** OpenJDK 8
* **Classpath Dependencies:** `botsing.jar`, `target/classes`, `target/test-classes`

---

## 🚀 Reproduction Steps

### Step 1: Run Botsing to Generate Crash Reproduction Test

```bash
/usr/lib/jvm/java-8-openjdk-amd64/bin/java \
  -Dtools_jar_location="/usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar" \
  -jar botsing.jar \
  -project_cp $(cat cp_compile.txt):target/classes:target/test-classes \
  -crash_log crash_log.txt \
  -target_frame 5 \
  -D search_budget=30 \
  -D minimize=false \
  -D test_dir=botsing-tests
```

### Step 2: Move Test Cases to Source Directory
```bash
cp -r botsing-tests/* src/test/java/

```
### Step 3: Compile & Execute Test Suite with Java 8
```bash
# Compile
/usr/lib/jvm/java-8-openjdk-amd64/bin/javac \
  -cp $(cat cp_compile.txt):target/classes:target/test-classes:botsing.jar \
  -d target/test-classes \
  src/test/java/org/apache/commons/lang3/math/NumberUtils_ESTest*.java

# Execute
/usr/lib/jvm/java-8-openjdk-amd64/bin/java \
  -cp $(cat cp_compile.txt):target/classes:target/test-classes:botsing.jar \
  org.junit.runner.JUnitCore org.apache.commons.lang3.math.NumberUtils_ESTest
```

