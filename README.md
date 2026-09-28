# SQA-Project-Group9
โปรเจกต์นี้เป็นส่วนหนึ่งของรายวิชา CP353201 Software Quality Assurance
ปีการศึกษา 1/2569

โครงงานมีวัตถุประสงค์เพื่อศึกษาและเปรียบเทียบความสามารถของ **Automatic Test Case Generation Algorithms** และ **AI-Assisting Tools / Generative AI** ในการสร้าง Test สำหรับ Java projects จาก **Defects4J dataset**

## Tools ที่ใช้ในการทดลอง

### Automatic Test Case Generation Algorithms

1. **Botsing**
2. **CATG**

### AI-Assisting Tools / Generative AI

1. **Gemini**
2. **Claude**

## Dataset

ใช้ **Defects4J** เป็น dataset สำหรับการทดลอง โดยใช้ Java projects และ defects ที่อยู่ภายใน dataset ตาม configuration ที่กำหนดไว้

รายละเอียด version ของ Defects4J และรายการ projects ที่ใช้ทดลองจะแสดงไว้ใน

`Configuration/`

## Project Structure

```text
SQA-Project-Group9/
│
├── Botsing/                  # ผลการทดลอง Automated Test Case Generation ด้วย Botsing
│   ├── Code/                 # Source code และสคริปต์ที่เกี่ยวข้องกับ Botsing
│   ├── Configuration/       # Configuration และพารามิเตอร์สำหรับการรัน Botsing
│   ├── Result_Round1/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 1
│   ├── Result_Round2/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 2
│   └── Test/                # Test cases / test suites ที่ Botsing สร้างขึ้น
│
├── CATG/                    # ผลการทดลอง Automated Test Case Generation ด้วย CATG
│   ├── Code/                 # Source code และสคริปต์ที่เกี่ยวข้องกับ CATG
│   ├── Configuration/       # Configuration และพารามิเตอร์สำหรับการรัน CATG
│   ├── Result_Round1/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 1
│   ├── Result_Round2/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 2
│   └── Test/                # Test cases / test suites ที่ CATG สร้างขึ้น
│
├── Claude/                  # ผลการทดลอง AI-assisted Test Generation ด้วย Claude
│   ├── Prompt/              # Prompt ที่ใช้ส่งให้ Claude
│   ├── Result/              # ผลลัพธ์และ Log จากการประมวลผลของ Claude
│   └── TestCode/            # Java Unit Test ที่ Claude สร้างขึ้น
│
├── Gemini/                  # ผลการทดลอง AI-assisted Test Generation ด้วย Gemini
│   ├── Prompt/              # Prompt ที่ใช้ส่งให้ Gemini
│   ├── Result/              # ผลลัพธ์และ Log จากการประมวลผลของ Gemini
│   └── TestCode/            # Java Unit Test ที่ Gemini สร้างขึ้น
│
├── Comparison/              # ผลการเปรียบเทียบ Botsing, CATG, Claude และ Gemini
├── Report/                  # เอกสารรายงานโครงงาน
├── Presentation/            # สไลด์สำหรับการนำเสนอ
└── README.md                # ภาพรวมโครงงาน โครงสร้างโปรเจกต์ และวิธีการใช้งาน
```

## รายละเอียดโฟลเดอร์

| โฟลเดอร์         | รายละเอียด                                                    |
| ---------------- | ------------------------------------------------------------- |
| `Code/`          | Source code และ scripts ที่ใช้ในการทดลอง                      |
| `Configuration/` | Configuration, environment และค่าที่ใช้ในการทดลอง             |
| `Result_Round1/` | เอกสาร/ผลลัพธ์จาก การศึกษาและออกแบบ ในรอบที่ 1 เช่น Algorithm study, วิธีการทำงาน, Prompt ที่ออกแบบ |
| `Result_Round2/` | ผลการทดลองจริง จากการนำ Algorithm/AI ไปสร้างและรัน Test กับ Defects4J เช่น test result, coverage, fault detection และ performance |
| `Test/`          | Test cases และ test suites ที่สร้างขึ้น                       |
| `Comparison/`    | ผลการเปรียบเทียบระหว่าง Algorithms และ AI Tools               |
| `Report/`        | รายงานฉบับสมบูรณ์                                             |
| `Presentation/`  | Presentation และไฟล์ที่ใช้สำหรับ Demo                         |


## Evaluation Metrics

ผลการทดลองจะพิจารณาตัวชี้วัดที่เกี่ยวข้อง เช่น Test Coverage, Fault Detection Rate และ Code Coverage Ratio

## Experiment Results

ผลการทดลองและการเปรียบเทียบระหว่าง Botsing, CATG, Claude และ Gemini อยู่ที่

```text
Comparison/
```

และรายงานฉบับสมบูรณ์อยู่ที่

```text
Report/
```
### หมายเหตุเกี่ยวกับ `.gitkeep`

ไฟล์ `.gitkeep` ใช้สำหรับให้ Git สามารถเก็บโฟลเดอร์ที่ยังไม่มีไฟล์ได้

เมื่อมีการเพิ่มไฟล์งานจริงในโฟลเดอร์แล้ว สามารถลบไฟล์ `.gitkeep` ออกได้

