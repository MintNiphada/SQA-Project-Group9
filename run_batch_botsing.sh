#!/bin/bash

PROJECT="Lang"
BUGS=(1 2 3 4 5)
SEARCH_BUDGET=60

BOTSING_JAR="/home/thadpeecha-673380584-2/SQA_Project/defects4j_workspace/Lang_1b/botsing.jar"
JAVA11_DIR="/usr/lib/jvm/java-11-openjdk-amd64"
JAVA8_BIN="/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java"

if [ ! -f "$JAVA8_BIN" ]; then
    JAVA8_BIN="java"
fi

echo "=== System Check ==="
echo "Botsing JAR : $BOTSING_JAR"
echo "Java 8 Bin  : $JAVA8_BIN"
echo "===================="

mkdir -p results_botsing

for BUG in "${BUGS[@]}"; do
    echo "=========================================="
    echo ">>> Starting Botsing on $PROJECT ${BUG}b <<<"
    echo "=========================================="
    
    WORK_DIR="/tmp/${PROJECT}_${BUG}b"
    rm -rf $WORK_DIR
    
    # 1. Defects4J Operations (Java 11)
    JAVA_HOME="$JAVA11_DIR" PATH="$JAVA11_DIR/bin:$PATH" defects4j checkout -p $PROJECT -v "${BUG}b" -w $WORK_DIR
    JAVA_HOME="$JAVA11_DIR" PATH="$JAVA11_DIR/bin:$PATH" defects4j compile -w $WORK_DIR
    JAVA_HOME="$JAVA11_DIR" PATH="$JAVA11_DIR/bin:$PATH" defects4j test -w $WORK_DIR
    
    # 2. Clean Crash Log (ลบบรรทัดหัวข้อ --- ออกเพื่อให้ Botsing อ่าน Stack Trace ได้)
    CRASH_LOG="$WORK_DIR/clean_crash_log.txt"
    grep -v "^---" "$WORK_DIR/failing_tests" > "$CRASH_LOG"
    
    # 3. Run Botsing (Java 8)
    "$JAVA8_BIN" -jar "$BOTSING_JAR" \
        -crash_log "$CRASH_LOG" \
        -target_frame 5 \
        -project_cp "$WORK_DIR/target/classes" \
        -Dsearch_budget=$SEARCH_BUDGET \
        -Dtest_dir="./results_botsing/${PROJECT}_${BUG}b"
        
    echo ">>> Finished $PROJECT ${BUG}b <<<"
    echo ""
done

echo "=== All Bugs Completed! ==="