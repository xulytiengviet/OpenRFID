## 🇻🇳 OpenRFID Lab — Phân tích thẻ, chuẩn ISO và DIY Chainway C72

Bộ tài liệu tiếng Việt bổ sung cho repository này, tập trung vào **tương thích RFID UHF / ISO**, **tầm đọc có kiểm chứng**, và **cách dùng mã nguồn Kotlin hiện có để đọc thẻ Alien ALN-9662 Higgs-3 trên Chainway C72**.

- 📖 **[Trang phân tích tương tác (docs/index.html)](docs/index.html)** — kiến trúc, ma trận thẻ ISO, đánh giá tầm xa, mô phỏng công suất tương đối, biểu mẫu phép đo xuất CSV.
- 🛠️ **[Hướng dẫn DIY C72 + Alien ALN-9662](docs/DIY_C72_ALN9662.md)** — công cụ, SDK, build/debug, Kotlin, hạn chế TID/EPC, protocol đo tái lập.
- 🌐 **Việt hóa Android bước đầu:** cấu hình tiếng Việt trong **app/src/main/res/values-vi/strings.xml**, nhãn màn hình chọn chức năng và luồng đọc thẻ đã được đưa sang Android string resources. Các màn hình khác vẫn cần hoàn thiện quốc tế hóa.
- ⚠️ **Trạng thái khoa học:** tương thích giao thức được đối chiếu với tài liệu hãng và ISO/GS1; **chưa báo cáo thử nghiệm vật lý C72 + ALN-9662**. Tầm 30 m quảng bá của C72 đo bằng thẻ Alien H3 chống kim loại khác ALN-9662 Short.
- 📡 **Pháp lý:** cấu hình RF phụ thuộc giới hạn vùng, công suất và quy định tại Việt Nam; không tùy tiện mở khóa băng tần/công suất.

**Xuất bản trang web:** Repository hiện chưa bật GitHub Pages. Vào **Settings → Pages → Build and deployment → Deploy from a branch**, chọn **main** và **/docs**, sau đó Save; khi Pages hoàn tất, trang sẽ phục vụ từ địa chỉ Pages do GitHub cung cấp.

> OpenRFID ở đây là fork của [rfidsoftwares/OpenRFID-Handheld-Android-Middleware](https://github.com/rfidsoftwares/OpenRFID-Handheld-Android-Middleware). Phần mềm gốc và quyền tác giả/Apache-2.0 tiếp tục được bảo lưu; các tài liệu bổ sung không thay thế tài liệu SDK chính thức của nhà sản xuất.

---

# OpenRFID-Handheld-Android-Middleware

**Universal Open-Source Android Handheld RFID Middleware & Suite**  
*Copyright (c) 2026 RFID Software India Private Limited ([https://rfidsoftwares.com](https://rfidsoftwares.com))*

---

## 📌 Repository Description

> **Universal open-source Android handheld RFID middleware & suite supporting Chainway, Seuic, Zebra, ATID & Honeywell readers. Features a multi-vendor HAL, real-time tag inventory table, EPC writing, security locking, and audio feedback.**

---

## 📥 Download APK

[![Download APK](https://img.shields.io/badge/Download-Latest%20APK-brightgreen?style=for-the-badge&logo=android)](https://github.com/rfidsoftwares/OpenRFID-Handheld-Android-Middleware/releases/latest/download/app-debug.apk)

You can download the latest pre-compiled Android APK directly to your device:

* 📱 **[Direct Download Latest APK (app-debug.apk)](https://github.com/rfidsoftwares/OpenRFID-Handheld-Android-Middleware/releases/latest/download/app-debug.apk)**
* 📦 **[View All Releases & Assets](https://github.com/rfidsoftwares/OpenRFID-Handheld-Android-Middleware/releases)**

> **Note**: When installing the APK on your handheld device or Android phone, allow *"Install from Unknown Sources"* if prompted by your browser.

---

## 🚀 Key Features

* **Multi-Vendor Hardware Abstraction Layer (HAL)**: Unified driver abstraction supporting all major handheld RFID hardware vendors:
  * **Chainway** (C72, C71, C66) — Native SDK (`cw-deviceapi20191022.jar`)
  * **Seuic** (AUTOID9U, Cruise) — Native SDK (`uhf.jar`, `scankey.jar`)
  * **Zebra** (MC3300R, RFD8500, RFD40, RFD90) — Native SDK (`rfidapi3lib-2.0.5.275.aar`)
  * **ATID** (AT911N, AT870, AT388) — Native SDK (`atid.dev.rfid.jar`)
  * **Honeywell** (CT40, EDA51, IH25) — Native Mobility SDK
  * **Mock Software Reader** — Full deterministic emulator for testing on standard smartphones & Android Studio emulators without physical hardware.
* **Eager Reader Initialization**: Hardware RFID chip is powered on and warmed up asynchronously upon entering screens for instant scan response.
* **Tabular Tag Inventory View**: Real-time updating table showing `# | EPC | RSSI | Count | Time` with support for **Single Shot** and **Continuous Live Scan** modes.
* **Tag Security & Locking**: Dedicated Tag Lock & Unlock screen supporting Access Password authentication and Gen2 memory bank security controls.
* **EPC Rewrite & Data Writing**: Update tag EPC values (HEX or ASCII) and write to USER/RESERVED memory banks.
* **Configurable Audio Beep Sound**: Integrated `SoundBeeper` utilizing system `ToneGenerator` with audio toggle switch in UHF Configuration settings.
* **Auto-Hardware Detection & Dropdown Override**: Automatic detection via `Build.MODEL`/`Build.MANUFACTURER` with explicit driver dropdown override.
* **Corporate Branding**: Native light/dark theme support, custom branded splash screen, and corporate assets from RFID Software India Private Limited.

---

## 🏗️ Architecture Overview

```text
OpenRFID Handheld Android Middleware Architecture
─────────────────────────────────────────────────────────────────────────────
 ┌─────────────────────────────────────────────────────────────────────────┐
 │                   UI Layer (Material 3 Components)                      │
 │  ModeSelector | ReadTag | WriteTag | UpdateEpc | TagLock | UhfConfig    │
 └────────────────────────────────────┬────────────────────────────────────┘
                                      │
 ┌────────────────────────────────────▼────────────────────────────────────┐
 │               Multi-Vendor Hardware Abstraction Layer (HAL)              │
 │                         RfidDriverFactory                               │
 └──────┬──────────────┬──────────────┬──────────────┬──────────────┬──────┘
        │              │              │              │              │
 ┌──────▼──────┐┌──────▼──────┐┌──────▼──────┐┌──────▼──────┐┌──────▼──────┐
 │  Chainway   ││    Seuic    ││    Zebra    ││    ATID     ││  Honeywell  │
 │ C72/C71/C66 ││  AUTOID9U   ││  MC3300R    ││   AT911N    ││    CT40     │
 └─────────────┘└─────────────┘└─────────────┘└─────────────┘└─────────────┘
```

---

## 🛠️ Build & Development Instructions

### Prerequisites
* JDK 17
* Android Studio Iguana / Jellyfish (or Gradle 8.2+)
* Android SDK 34 (Build-Tools 34.0.0)

### Building the Debug APK
```bash
git clone https://github.com/rfidsoftwares/OpenRFID-Handheld-Android-Middleware.git
cd OpenRFID-Handheld-Android-Middleware
.\gradlew.bat assembleDebug
```
Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Running Unit Tests
```bash
.\gradlew.bat testDebugUnitTest
```

---

## 🏢 Corporate Contact & Support

**RFID Software India Private Limited**  
* Website: [https://rfidsoftwares.com](https://rfidsoftwares.com)  
* Product: OpenRFID Handheld Android Middleware  
* License: Open Source Middleware for Enterprise RFID Systems
