# BankSystem – Bài 2: Code Quality (Checkstyle + Logging)

## Chạy
```bash
mvn checkstyle:check   # kiểm tra style (cũng tự chạy ở phase validate)
mvn clean package      # build; fail nếu có vi phạm Checkstyle
```

## Tích hợp Checkstyle
- Plugin `maven-checkstyle-plugin` 3.6.0 + Checkstyle 10.20.1, gắn vào phase `validate`.
- Chuẩn: **Google Java Style** (`google_checks.xml`, đóng gói sẵn trong Checkstyle).
- `google_checks.xml` để severity là *warning* nên cần `violationSeverity=warning`
  + `failOnViolation=true`, nếu không `check` sẽ pass dù còn vi phạm.
- Kết quả: **383 vi phạm → 0 vi phạm**.

## Các lỗi chính đã sửa
| Nhóm | Ví dụ | Cách sửa |
|---|---|---|
| Lỗi biên dịch | `/ Vi phạm...` (dấu `/` thừa), `package bank_system` chỉ có ở 1 file, `doWithdrawing throws Exception` mà caller chỉ bắt `BankException` | Đưa về cùng package `banksystem`, sửa `throws BankException`, xóa dòng lỗi |
| Đặt tên | `checking_type`, `_accNum`, `B`, `c_list`, `get_type_string`, tham số `a`, `iB` | `CHECKING_TYPE`, `accountNumber`, `balance`, `customerList`, `getTypeString`, `amount`, `initialBalance` |
| Định dạng | thụt lề sai, `{` xuống dòng, hàm 1 dòng, if không ngoặc, dòng > 100 ký tự | 2 spaces, K&R brace, luôn có `{}`, ngắt dòng |
| Import | `java.util.*`, `java.io.*` thừa | import cụ thể, đúng thứ tự |
| Javadoc | thiếu/không có `@param`, `@return` | bổ sung đầy đủ, câu đầu kết thúc bằng dấu chấm |
| Magic number/string | `new Transaction(3, ...)`, `1000.0`, `5000.0`, `"\\d{9}"`, `case 1:` | `TYPE_*`, `MAX_WITHDRAW`, `MIN_BALANCE`, `ID_PATTERN` |
| Smell | `+=` String trong vòng lặp, catch `Exception`, code lặp, hàm `readCustomerList` lồng sâu | `Collectors.joining`, catch exception cụ thể, tách `processLine`/`addAccountFromLine`/`joinCustomerInfo` |
| Log | `System.out/err.println` rải rác | SLF4J + Logback |

## Logging (SLF4J + Logback)
Cấu hình ở `src/main/resources/logback.xml`: ghi ra console và file `logs/bank-system.log`
(xoay vòng theo ngày, giữ 30 ngày). Mỗi lớp dùng `LoggerFactory.getLogger(X.class)`;
dùng placeholder `{}` thay vì cộng chuỗi để không tốn công format khi level bị tắt.

| Level | Dùng khi | Ví dụ trong code | Lý do chọn |
|---|---|---|---|
| ERROR | Lỗi thật sự, hệ thống không hoàn thành được việc | `readCustomerList` gặp `IOException`/dữ liệu sai số → đọc dừng giữa chừng | Cần cảnh báo ngay, kèm stack trace |
| WARN | Nghiệp vụ bị từ chối nhưng hệ thống vẫn ổn | Nạp/rút bị từ chối (số tiền không hợp lệ, thiếu số dư, vượt hạn mức), dòng dữ liệu sai định dạng, loại tài khoản lạ | Bất thường, đáng theo dõi (vd. dấu hiệu gian lận) nhưng không phải lỗi hệ thống |
| INFO | Sự kiện nghiệp vụ quan trọng, luôn nên có ở production | Giao dịch thành công (tài khoản, loại, số tiền, số dư trước → sau); đọc xong N khách hàng | Là "nhật ký kiểm toán" của ngân hàng, ít dòng, đọc được |
| DEBUG | Chi tiết để dev gỡ lỗi | Thêm từng khách hàng, dựng tóm tắt giao dịch, bắt đầu xử lý nạp tiền | Rất nhiều dòng, tắt ở production (đổi logger `banksystem` sang INFO) |

Dữ liệu được ghi: số tài khoản (định danh giao dịch), loại giao dịch, số tiền, số dư trước/sau
(truy vết và đối soát), lý do bị từ chối. Không ghi số CMND ở mức INFO trở lên; ở production nên
che bớt số tài khoản và tên (PII) hoặc tắt DEBUG.

---

# Bài 3: CI/CD automation (GitHub Actions)

File workflow: `.github/workflows/ci.yml` (thư mục gốc repo phải là thư mục chứa `pom.xml`).

| Yêu cầu đề | Cách đáp ứng |
|---|---|
| Trigger `push` và `pull_request` | `on: push:` và `on: pull_request:` |
| Chạy vòng đời Maven, đảm bảo `test` và `package` thành công | Bước `mvn -B test` rồi `mvn -B package -DskipTests` (cả hai đều chạy `validate` = Checkstyle trước) |
| `upload-artifact` lưu file `.jar` | `actions/upload-artifact@v4`, `path: target/*.jar`, `if-no-files-found: error` (không có jar thì job fail) |
| Cố tình gây lỗi và debug bằng execution logs | Xem mục dưới |

Unit tests (JUnit 5): 16 test trong `src/test/java/banksystem/` (Transaction, CheckingAccount, SavingsAccount, Bank).

## Chứng minh debug bằng execution logs
1. Tạo nhánh: `git checkout -b test-ci-fail`
2. Cố tình gây lỗi, ví dụ trong `CheckingAccountTest.depositIncreasesBalanceAndRecordsTransaction`
   đổi `assertEquals(1500, ...)` thành `assertEquals(1600, ...)`. Commit và push.
3. Vào tab **Actions** -> run bị đánh dấu đỏ -> job `build` -> mở bước **Run unit tests**.
4. Trong log đọc được:
   - Dòng `Tests run: 16, Failures: 1` và `BUILD FAILURE`.
   - Dòng `expected: <1600.0> but was: <1500.0>` kèm `CheckingAccountTest.java:<số dòng>` -> biết đúng file và dòng sai.
   - Log Logback của ứng dụng ngay phía trên, ví dụ
     `INFO Transaction OK: account=1001, ... balance 1000.0 -> 1500.0`
     -> xác nhận code chạy đúng (balance thật là 1500), lỗi nằm ở test.
   - Mục **Artifacts** có `surefire-reports` để tải báo cáo chi tiết.
5. Sửa lại test, push -> run xanh, artifact `bank-system-jar` xuất hiện ở cuối trang run.

Biến thể: làm lỗi Checkstyle (đổi thụt lề 1 dòng) -> job fail ngay ở phase `validate`
với dòng `[WARN] ... [Indentation]` chỉ rõ file:dòng:cột.

---

# Bài 4: Kiểm thử đa hệ điều hành (Matrix Strategy)

## 1. Matrix Strategy trong `ci.yml`
```yaml
strategy:
  fail-fast: false
  matrix:
    os: [ubuntu-latest, windows-latest, macos-latest]
runs-on: ${{ matrix.os }}
```
- Cùng một job được nhân thành 3 job chạy song song, mỗi OS một job.
- `fail-fast: false`: một OS lỗi thì các OS còn lại vẫn chạy tiếp, để so sánh kết quả.
- `defaults.run.shell: bash`: dùng cùng cú pháp lệnh trên cả 3 OS.
- Tên artifact có `${{ matrix.os }}` vì tên trùng nhau giữa các job sẽ bị lỗi.
- `.gitattributes` ép LF, tránh Windows checkout ra CRLF làm lệch Checkstyle.
- `-Dfile.encoding=UTF-8` cho Surefire, vì Windows mặc định không phải UTF-8.

## 2. Cố tình gây lỗi đường dẫn cứng
Code hiện tại (`DataFiles.resolve`) đã là bản ĐÚNG. Để làm thí nghiệm, tạo nhánh `test-os-fail`
và sửa `src/main/java/banksystem/DataFiles.java`:
```java
public static Path resolve(String directory, String fileName) {
  return Paths.get(directory + "\\" + fileName);   // viết cứng dấu \ của Windows
}
```
Commit, push, mở tab Actions. Kết quả dự kiến (đã thử trên Linux):
| OS | Kết quả | Lý do |
|---|---|---|
| windows-latest | PASS | `\` là dấu phân cách hợp lệ trên Windows |
| ubuntu-latest, macos-latest | FAIL 3 test | `\` chỉ là một ký tự trong tên file, không phải dấu phân cách |

Lỗi trong log (bước *Run unit tests* của job ubuntu/macos):
```
expected: <data/customers.txt> but was: <data\customers.txt>
java.nio.file.NoSuchFileException: /tmp/junit.../customers.txt   (có \ trước customers.txt)
```
Biến thể ngược: test viết cứng `assertEquals("data/customers.txt", path.toString())`
sẽ PASS trên Linux/macOS nhưng FAIL trên Windows (`data\customers.txt`).

## 3. Refactor
Dùng `java.nio.file.Paths.get(directory, fileName)` (hoặc `Path.resolve`). Java tự chọn dấu phân cách theo OS.
Test so sánh bằng `File.separator` hoặc `Path`, không viết cứng `/` hay `\`:
```java
assertEquals("data" + File.separator + "customers.txt", path.toString());
assertEquals(tempDir.resolve("customers.txt"), path);
```
Test đọc file dùng `@TempDir` của JUnit nên không phụ thuộc thư mục cố định trên máy.
Push lại bản đúng -> cả 3 OS xanh.
