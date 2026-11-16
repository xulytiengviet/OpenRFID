# DIY OpenRFID: Chainway C72 + Alien ALN-9662 Higgs-3

**Cập nhật 16/11/2026 · Tài liệu kỹ thuật, chưa có kết quả thử nghiệm trên C72 thật.**

[Trang tương tác OpenRFID Lab](index.html) · [Mã nguồn](https://github.com/xulytiengviet/OpenRFID)

## 1. Tương thích cần phân biệt

| Lớp | Chainway C72 | Alien ALN-9662 | Kết luận |
|---|---|---|---|
| Vô tuyến | UHF EPC Class 1 Gen2, ISO/IEC 18000-6C | Alien Higgs-3, EPC Gen2, ISO/IEC 18000-6C | Tương thích về giao thức |
| Tần số | Biến thể 865–868 / 920–925 / 902–928 MHz | Inlay hoạt động dải UHF rộng | Cần region hợp lệ và giao nhau |
| Bộ nhớ | Có lệnh inventory/đọc vùng nhớ theo SDK | EPC 96–480 bit, USER 512 bit, TID | Giới hạn theo IC, lock và SDK |
| Tầm đọc | Hãng công bố tối đa 30 m với **Alien H3 anti-metal 130×42 mm** | **ALN-9662 Short khác với thẻ đo trên** | Không thể suy ra 30 m cho ALN-9662 |
| Hệ điều hành/SDK | Android 11/13, các đời module khác nhau | Tag thụ động, không quan tâm Android | Phải kiểm tra SDK đúng firmware |

**Tiêu chuẩn mới:** ISO/IEC 18000-63:2026 (Edition 4); GS1 EPC UHF Gen2 v3.0.1 (26/02/2026). Tên ISO 18000-6C vẫn hay gặp trong datasheet. Hỗ trợ protocol Gen2 cơ bản không đồng nghĩa hỗ trợ mọi extension của Gen2v2/Gen2X.

**Lưu ý 96-bit:** 96 bit = 24 ký tự hex chỉ là kích thước thường gặp. Không hardcode cố định 24 ký tự; EPC có thể dài hơn. EPC không phải chứng thực chống sao chép. TID của Higgs-3 thường chỉ đọc và không nên coi là writable.

## 2. Kiểm kê bộ thiết bị

- C72 có module **UHF** và firmware phù hợp; xác định CM2000-1 / CM710-1 / E Series từ nhà cung cấp. Không suy ra module chỉ theo tên model.
- Thẻ Alien ALN-9662 Higgs-3, tối thiểu ba chiếc khác EPC, dán thử trên bìa carton/nhựa, tránh kim loại trong baseline.
- Máy tính có Android Studio, JDK 17, Android SDK, ADB; cáp USB.
- Bản sao mã nguồn OpenRFID hiện tại; lưu SHA commit và phiên bản SDK Chainway.
- Xác minh vùng tần số, mức phát cho phép của thiết bị và văn bản hiện hành trước khi phát RF.

**Trước khi sửa code:** chạy app UHF demo của Chainway trên C72 để inventory ALN-9662 ở 0,5–1 m. Nếu demo không đọc, ưu tiên xử lý phần cứng/region/ăng-ten, không vội sửa APK.

## 3. Clone, build và cài trên Windows

~~~powershell
git clone https://github.com/xulytiengviet/OpenRFID.git
cd OpenRFID
.\gradlew.bat --version
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
adb devices
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
~~~

File app/build.gradle hiện có compileSdk 35, targetSdk 35, minSdk 24, Java 17. Nếu lỗi build, kiểm tra Android SDK, Gradle, dependency, JDK và SDK native theo log; không xem lệnh trên là xác nhận build đã được chạy.

## 4. SDK Chainway và API thực tế

- Nguồn SDK chính thức: https://www.chainway.net/Support/Info/10 (trang hỗ trợ có gói Android Studio 2026 tại thời điểm rà soát).
- Fork hiện chứa **app/libs/cw-deviceapi20191022.jar**. Không khẳng định JAR 2019 tương thích mọi máy Android 11/13.
- Source ChainwayUhfReaderGateway.kt hiện gọi lớp **com.rscja.deviceapi.RFIDWithUHFUART**, chữ UART viết hoa. Không đổi sang RFIDWithUHFUart chỉ vì mẫu trên mạng.
- Kiểm tra các method của đúng SDK: getInstance, init, startInventoryTag, readTagFromBuffer, stopInventory, free, getPower/setPower; có thể có overload khác nhau theo SDK. Khi thay JAR bằng AAR chính thức, loại bỏ xung đột duplicate class và tuân thủ điều kiện cấp phép của vendor.
- Với RFID UHF tích hợp qua SDK nội bộ, **không bắt buộc** thêm Bluetooth/Location nếu không dùng chức năng đó. Reader Bluetooth ngoài yêu cầu bộ quyền khác, đặc biệt từ Android 12.

## 5. Tận dụng source đã có

| Thành phần | Đường dẫn |
|---|---|
| Interface chung | app/src/main/java/com/rfidsoftwares/rfid/driver/RfidReaderDriver.kt |
| Factory chọn driver | app/src/main/java/com/rfidsoftwares/rfid/driver/RfidDriverFactory.kt |
| C72 native adapter | app/src/main/java/com/rfidsoftwares/rfid/driver/ChainwayC72Driver.kt |
| Giao tiếp native | app/src/main/java/com/rfidsoftwares/rfid/ChainwayUhfReaderGateway.kt |
| UI reader | app/src/main/java/com/rfidsoftwares/ui/screens/ReadTagFragment.kt |
| Reader giả lập | app/src/main/java/com/rfidsoftwares/rfid/driver/MockRfidDriver.kt |

Quy trình: khởi tạo → inventory → đọc buffer thường xuyên trên nền → chuẩn hóa EPC → loại trùng/đếm → hiển thị RSSI → stop → release.

Đoạn mã Kotlin sau minh họa **một phương thức ứng dụng** sử dụng interface đã có, không phải toàn bộ Activity:

~~~kotlin
import com.rfidsoftwares.rfid.driver.RfidDriverFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

suspend fun scanAlien(context: android.content.Context): Map<String, Int> =
  withContext(Dispatchers.IO) {
    val reader = RfidDriverFactory.createDriver("chainway_c72")
    val counts = linkedMapOf<String, Int>()
    check(reader.init(context)) { "C72 init failed" }
    try {
      check(reader.startInventory()) { "UHF inventory failed" }
      val end = android.os.SystemClock.elapsedRealtime() + 4000
      while (android.os.SystemClock.elapsedRealtime() < end) {
        for (tag in reader.readBufferedTagEvents()) {
          val epc = tag.epc.trim().uppercase()
          if (epc.isNotBlank()) counts[epc] = (counts[epc] ?: 0) + 1
        }
        delay(35)
      }
      counts
    } finally {
      reader.stopInventory()
      reader.release()
    }
  }
~~~

Có thể dùng API này trong Fragment/Activity qua lifecycle coroutine, cập nhật UI theo lô; phân biệt tag duy nhất với số lượt đọc. Giá trị poll 35 ms là ví dụ, phải tinh chỉnh theo SDK và kết quả đo CPU. Không bật Kill/Lock trong luồng đọc thử.

## 6. Các lỗi cần kiểm toán ở fork

1. ChainwayC72Driver.init hiện gộp gateway/tag-access bằng **gOk || tOk**; kết quả init có thể thành công dù gateway inventory lỗi. Yêu cầu riêng gateway init cho nghiệp vụ inventory.
2. ChainwayUhfReaderGateway.setPower hiện có nhánh trả true sau khi gọi helper setter mà không xác nhận setter đã được áp dụng; nên sửa helper trả Boolean và readback từ SDK.
3. Reflection gọi startInventoryTag/readTagFromBuffer theo tên method; cần kiểm tra overload đúng trên SDK của máy.
4. readBufferedTagEvents đọc tối đa một tag mỗi lần gọi; worker cần polling cân bằng với tránh tăng CPU và có cancellation.
5. Interface có thao tác write vào bank TID nhưng đa số TID Higgs-3 là chỉ đọc: UI và backend nên chặn ghi TID, đừng thử thao tác rủi ro.
6. Không tự xác nhận regulatory region chỉ bằng chuỗi trả về của cấu hình. Nếu chưa xác thực region và power, không phát sóng.

## 7. Quy trình đo tầm đọc tái lập

- Ghi rõ Android, firmware, module, SDK, Git commit, phiên bản tag, dải tần/region, anten và power **được phép**.
- Thử các khoảng cách 0,5; 1; 2; 3; 5; 8; 10 m (bỏ các khoảng không phù hợp), mỗi điểm ít nhất 3 phiên, mỗi phiên 10 lần cố định. Thử một tag một lần trước khi đa tag.
- Thay đổi **một biến mỗi lần**: carton / nhựa / kim loại / gần nước, hướng tag, góc reader, vị trí và công suất được phép.
- Ghi số lần phát hiện đúng EPC, số lần thử, lượt đọc, số EPC duy nhất, thời gian, RSSI nếu có, khoảng cách, vật liệu và phiên bản firmware.
- Tính P_read(d) = số lần phát hiện đúng / số lượt thử. Quy định trước ngưỡng vận hành ví dụ 95%; công bố khoảng tin cậy và toàn bộ bảng phép đo. Chỉ báo “đọc xa nhất” sẽ thiên lệch.
- Nhập thử nghiệm thật trong trang docs/index.html để xuất CSV. Form chỉ ghi trong bộ nhớ tab trình duyệt và không điều khiển UHF.

**Tiêu chí nghiệm thu đề xuất (chưa kiểm thử):** khởi tạo ổn định trong 100 lượt on/off, không treo app, stop/release sạch, EPC/count chính xác, xử lý lỗi rõ ràng, đọc lại power/region, CSV tái lập.

## 8. Tần số tại Việt Nam

**Hardware support không đồng nghĩa quyền phát sóng.** C72 có lựa chọn 920–925 MHz, nhưng việc khai thác tại Việt Nam phải xét đúng Thông tư 08/2021/TT-BTTTT, các sửa đổi/hợp nhất 2025, điều kiện của thiết bị và công suất/tần số được chứng nhận. Thông tư **67/2026/TT-BKHCN** (ban hành 06/11/2026, **có hiệu lực 24/12/2026**) quy hoạch các băng có 919,4–925 MHz phục vụ thông tin di động đường sắt: phải kiểm tra tác động khi triển khai sau thời điểm này. Không hướng dẫn vô hiệu hóa khoá vùng hoặc tăng công suất ngoài điều kiện hợp pháp.

## 9. Nguồn tham khảo

- https://github.com/xulytiengviet/OpenRFID
- https://github.com/rfidsoftwares/OpenRFID-Handheld-Android-Middleware
- https://www.chainway.net/Products/Info/42
- https://www.chainway.net/Support/Info/10
- https://www.alientechnology.com/products/files-welcome/
- https://www.iso.org/standard/87122.html
- https://ref.gs1.org/standards/gen2/
- https://pub.dev/packages/rfid_c72_plugin
- https://vanban.chinhphu.vn/?docid=220264&orggroupid=4&pageid=27160

Phần mềm và site tuân thủ quyền tác giả/giấy phép upstream (repo hiện chứa Apache-2.0). Không nêu đã kiểm thử thiết bị thực khi chưa có kết quả thực địa.
