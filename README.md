# SQA-Project-Group9

โปรเจกต์นี้เป็นส่วนหนึ่งของรายวิชา CP353201 Software Quality Assurance  
ปีการศึกษา 1/2569

โครงงานมีวัตถุประสงค์เพื่อศึกษาและเปรียบเทียบความสามารถของ **Automatic Test Case Generation Algorithms** และ **AI-Assisting Tools / Generative AI** ในการสร้าง Test สำหรับ Java projects จาก **Defects4J dataset**

---

## Tools ที่ใช้ในการทดลอง

### Automatic Test Case Generation Algorithms
1. **Botsing**
2. **CATG**

### AI-Assisting Tools / Generative AI
1. **Gemini**
2. **DeepSeek**

---

## การแบ่งหน้าที่การทำงาน (Team Responsibilities)

| ผู้รับผิดชอบ | เครื่องมือ / ส่วนงาน | รายละเอียดหน้าที่ความรับผิดชอบ |
| :--- | :--- | :--- |
| **สมาชิกคนที่ 1** | **CATG** | ศึกษาและตั้งค่าอัลกอริทึม CATG, กำหนดค่า Configuration, สั่งรันการทดลองสร้าง Test Case ร่วมกับ Defects4J Dataset และสรุปผลการทดลองใน `Result_Round1/` และ `Result_Round2/` |
| **สมาชิกคนที่ 2** | **Botsing** | ศึกษาและตั้งค่าอัลกอริทึม Botsing, กำหนด พารามิเตอร์สำหรับการสร้าง Test Suite เพื่อทำ Crash Replication บน Defects4J และสรุปผลการทดลองใน `Result_Round1/` และ `Result_Round2/` |
| ศศิวิตรา วงษ์รุ่งอรุณเลิศ 673380602-6 | **Gemini & DeepSeek** | ออกแบบ Prompt Engineering, พัฒนาอัตโนมัติสคริปต์รัน Pipeline (API Automation), จัดการระบบซิงก์และวัดผล Line/Branch Coverage ของ AI บน Defects4J |

---

## Dataset

ใช้ **Defects4J** เป็น dataset สำหรับการทดลอง โดยใช้ Java projects และ defects ที่อยู่ภายใน dataset ตาม configuration ที่กำหนดไว้

รายละเอียด version ของ Defects4J และรายการ projects ที่ใช้ทดลองจะแสดงไว้ใน `Configuration/`

---

## Project Structure

```text
SQA-Project-Group9/
│
├── Botsing/                 # ผลการทดลอง Automated Test Case Generation ด้วย Botsing
│   ├── Code/                # Source code และสคริปต์ที่เกี่ยวข้องกับ Botsing
│   ├── Configuration/       # Configuration และพารามิเตอร์สำหรับการรัน Botsing
│   ├── Result_Round1/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 1
│   ├── Result_Round2/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 2
│   └── Test/                # Test cases / test suites ที่ Botsing สร้างขึ้น
│
├── CATG/                    # ผลการทดลอง Automated Test Case Generation ด้วย CATG
│   ├── Code/                # Source code และสคริปต์ที่เกี่ยวข้องกับ CATG
│   ├── Configuration/       # Configuration และพารามิเตอร์สำหรับการรัน CATG
│   ├── Result_Round1/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 1
│   ├── Result_Round2/       # ผลงานและผลการทดลองสำหรับการส่งงานรอบที่ 2
│   └── Test/                # Test cases / test suites ที่ CATG สร้างขึ้น
│
├── DeepSeek/                # ผลการทดลอง AI-assisted Test Generation ด้วย DeepSeek
│   ├── Prompt/              # Prompt ที่ใช้ส่งให้ DeepSeek
│   ├── Result/              # ผลลัพธ์และ Log จากการประมวลผลของ DeepSeek
│   └── TestCode/            # Java Unit Test ที่ DeepSeek สร้างขึ้น
│
├── Gemini/                  # ผลการทดลอง AI-assisted Test Generation ด้วย Gemini
│   ├── Prompt/              # Prompt ที่ใช้ส่งให้ Gemini
│   ├── Result/              # ผลลัพธ์และ Log จากการประมวลผลของ Gemini
│   └── TestCode/            # Java Unit Test ที่ Gemini สร้างขึ้น
│
├── Comparison/              # ผลการเปรียบเทียบ Botsing, CATG, DeepSeek และ Gemini
├── Report/                  # เอกสารรายงานโครงงาน
├── Presentation/            # สไลด์สำหรับการนำเสนอ
└── README.md                # ภาพรวมโครงงาน โครงสร้างโปรเจกต์ และวิธีการใช้งาน
