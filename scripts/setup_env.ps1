# setup_env.ps1 ใช้สำหรับ setup ครั้งเดียว สามารถกดรันได้เลย
# ใช้ powershell -ExecutionPolicy Bypass -File setup_env.ps1
# แก้ค่าในคอมเมนท์ CONFIG ด้านล่างให้ตรงกับเครื่องตนเอง

$ErrorActionPreference = "Stop"

# ============================ CONFIG ============================
$JAVA11_HOME  = "C:\Program Files\Eclipse Adoptium\jdk-11.0.32.101-hotspot"
$STRAWBERRY   = "C:\Strawberry\perl\bin"
$GIT_BIN      = "C:\Program Files\Git\bin"
$GIT_USR_BIN  = "C:\Program Files\Git\usr\bin"
$D4J_HOME     = "D:\Lab_SQA\defects4j"
$PROJECTS_DIR = "D:\Lab_SQA\Defects4J_Projects"
# ================================================================

$ErrorOutput = @()

function Assert-Path([string]$Name, [string]$Path) {
    if (Test-Path -LiteralPath $Path) { Write-Host "  [OK]   $Name = $Path" }
    else { Write-Host "  [MISS] $Name = $Path"; $script:ErrorOutput += $Name }
}

Write-Host "=== ตรวจ environment ของเครื่องนี้ ==="
Assert-Path "JAVA11_HOME"  $JAVA11_HOME
Assert-Path "STRAWBERRY"   $STRAWBERRY
Assert-Path "GIT_BIN"      $GIT_BIN
Assert-Path "GIT_USR_BIN"  $GIT_USR_BIN
Assert-Path "D4J_HOME"     $D4J_HOME
Assert-Path "PROJECTS_DIR" $PROJECTS_DIR

# Java version check
if (Test-Path "$JAVA11_HOME\bin\java.exe") {
    $v = & "$JAVA11_HOME\bin\java.exe" -version 2>&1 | Select-Object -First 1
    Write-Host "  (java  = $v)"
}

# ตรวจ tool CLI เพิ่มเติมที่ Defects4J ต้องใช้
Write-Host ""
Write-Host "=== ตรวจ CLI tools (ต้องผ่าน path ข้างบน) ==="
foreach ($tool in @("perl", "bash", "cvc4", "git")) {
    $which = Get-Command $tool -ErrorAction SilentlyContinue
    if ($which) { Write-Host "  [OK]   $tool -> $($which.Source)" }
    else { Write-Host "  [MISS] $tool (ไม่เจอใน PATH)"; $script:ErrorOutput += $tool }
}

if ($ErrorOutput.Count -gt 0) {
    Write-Host ""
    Write-Warning "ยังขาด: $($ErrorOutput -join ', ')"
    Write-Host "ไปติดตั้ง/แก้ path ใน CONFIG ก่อน แล้วรันใหม่"
    exit 1
}

# ============================ สร้าง config ให้ pipeline ============================
$envFile = Join-Path $PSScriptRoot "d4j_env.json"
$cfg = @{
    JAVA11_HOME  = $JAVA11_HOME
    STRAWBERRY   = $STRAWBERRY
    GIT_BIN      = $GIT_BIN
    GIT_USR_BIN  = $GIT_USR_BIN
    D4J_HOME     = $D4J_HOME
    PROJECTS_DIR = $PROJECTS_DIR
} | ConvertTo-Json
Set-Content -LiteralPath $envFile -Value $cfg -Encoding UTF8
Write-Host ""
Write-Host "[SAVED] $envFile"

# ============================ สร้างโฟลเดอร์ ============================
$dirs = @("results", "Claude\TestCode", "CATG\TestCode", "Gemini\TestCode", "Botsing\TestCode")
foreach ($d in $dirs) {
    $p = Join-Path (Split-Path $PSScriptRoot -Parent) $d
    New-Item -ItemType Directory -Path $p -Force | Out-Null
}
Write-Host "[DIRS] สร้างผลลัพธ์/TestCode โฟลเดอร์ครบแล้ว"

Write-Host ""
Write-Host "=== เสร็จแล้ว รันต่อได้: python scripts\run_experiment.py --manifest ... ==="