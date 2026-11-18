## 1. Thông tin sinh viên

Họ tên: Lê Anh Quang  
Mã sinh viên: 25021951  
Lớp: 2627I_UET.CS1058_9  

## 2. Mục tiêu của project

**Tình huống**: Công ty tưởng tượng của bạn — AcmeCorp — có nhiều người dùng và cơ sở dữ liệu mật khẩu vừa bị lộ.

**Câu hỏi**: Kẻ tấn công mất bao lâu để hoàn thành bẻ khóa mật khẩu của tất cả mọi người từ hash?

Dự án Java này ứng dụng phương pháp **nghiên cứu độ phức tạp thời gian, không gian bộ nhớ** của DSA để đánh giá, so sánh mức độ an toàn của bốn cách băm mật khẩu khác nhau bằng cách mô phỏng một cuộc tấn công **brute-force thuần túy** vào từng cách băm. Đồng thời cho thấy tầm quan trọng của một thuật toán tốt (ở đây là thuật toán băm).

**Thực tế có thật:** [LinkedIn 2012](https://techcrunch.com/2016/05/18/117-million-linkedin-emails-and-passwords-from-a-2012-hack-just-got-posted-online) — mật khẩu của 117 triệu tài khoản chỉ được băm SHA-1 không salt (giống mô phỏng 1), nên hàng trăm nghìn mật khẩu bị bẻ trong nháy mắt.

## 3. Một vài khái niệm cần biết

Nếu bạn đã biết rồi thì có thể bỏ qua.

<details>
<summary><b>Hàm băm (hash function)</b></summary>

Nhận dữ liệu bất kỳ (ở đây là mật khẩu) và trả một chuỗi độ dài cố định — VD: SHA-1 luôn ra 40 ký tự hex, SHA-256 luôn ra 64 ký tự hex. Cùng đầu vào thì luôn cùng đầu ra. Quan trọng: hàm này là **một chiều**, tức không có cách nào tính ngược từ hash ra chuỗi đầu vào. Hệ thống chỉ lưu hash; khi đăng nhập, nó băm mật khẩu bạn gõ rồi so sánh với hash đã lưu.

</details>

<details>
<summary><b>Brute-force</b></summary>

Vì không thể tính ngược, kẻ tấn công chỉ có thể đoán mật khẩu, băm rồi so sánh. Brute-force là thử tất cả chuỗi có thể trong một bộ ký tự (charset) và độ dài cho trước. Đây là kiểu tấn công chậm nhưng dễ phân tích độ phức tạp — và là kiểu được cài đặt trong dự án.

</details>

<details>
<summary><b>Không gian mật khẩu</b></summary>

Tập hợp tất cả chuỗi mà brute-force cần thử, sinh bằng code từ một bộ ký tự (charset) với độ dài tối đa (maxLength). Nếu charset có `C` ký tự và độ dài tối đa là `L`, số chuỗi cần thử là:

```
K = C¹ + C² + ... + C^L
```

Đây là hàm **mũ** theo `L` — mỗi ký tự cho phép thêm vào mật khẩu đều nhân độ khó brute-force lên `C` lần. Đây chính là lý do mật khẩu dài hơn vài ký tự lại an toàn hơn cực nhiều.

</details>

<details>
<summary><b>Salt</b></summary>

Là chuỗi ngẫu nhiên, riêng cho từng user, được ghép vào mật khẩu trước khi băm: `hash(salt + mật khẩu)`. Salt không bí mật, lưu ngay cạnh hash. Tác dụng là làm cho hai người dùng cùng mật khẩu có hash khác nhau, khiến kẻ tấn công không thể tính một bảng dùng chung cho tất cả mọi người.

</details>

<details>
<summary><b>Hàm băm chậm (PBKDF2, bcrypt, scrypt, Argon2)</b></summary>

Hàm băm được thiết kế để tốn thời gian. VD: PBKDF2 lặp hàm băm hàng trăm nghìn lần cho mỗi mật khẩu. User thật chỉ đợi thêm một chút khi đăng nhập, nhưng kẻ tấn công phải chịu chi phí đó cho mỗi lần đoán nhân với toàn bộ không gian mật khẩu.

</details>

<details>
<summary><b>Dictionary attack và rainbow table — Đọc thêm, không sử dụng trong mô phỏng</b></summary>

Đây là hai kỹ thuật hacker real hay dùng để tấn công nhanh hơn brute-force thuần túy:

- **Dictionary attack**: sử dụng danh sách mật khẩu phổ biến hoặc từng bị rò rỉ  (VD: `rockyou.txt` với 14 triệu mật khẩu), băm trong thời gian thực, so sánh với hash mật khẩu. Do phần lớn user dùng mật khẩu yếu, nó vô cùng hiệu quả mà không cần thử hết không gian mật khẩu.

- **Rainbow table**: từ danh sách mật khẩu phổ biến tính toán sẵn bảng `hash -> mật khẩu`. Tốc độ cực nhanh nhưng tốn rất nhiều dung lượng bộ nhớ. 

</details>

## 4. Bốn mô phỏng

| # | Cách băm | Cách tấn công |
|---|---|---|
| 1 | SHA-1, không salt | Brute-force toàn bộ không gian mật khẩu một lần, lưu vào bảng băm `hash -> mật khẩu`, tra bảng cho từng người dùng |
| 2 | SHA-256, không salt | Giống mô phỏng 1, chỉ đổi thuật toán băm — để đánh giá mức độ an toàn của thuật toán |
| 3 | SHA-256, có salt | Brute-force lại từ đầu cho từng người dùng (không dùng lại được bảng của mô phỏng 1/2) |
| 4 | PBKDF2 (310.000 vòng), có salt | Brute-force từng người dùng, nhưng mỗi lần đoán rất chậm nên chỉ đo tốc độ thật trên một mẫu nhỏ rồi suy tính cho toàn bộ |

## 5. Ứng dụng của DSA trong bài toán

**Tổng quan:** Bảng băm (`HashMap`) và nghiên cứu độ phức tạp không gian–thời gian.

Ký hiệu dùng trong phần này:

| Ký hiệu | Nghĩa | Giá trị mặc định trong demo |
|---|---|---|
| `U` | số người dùng | 300 |
| `C` | số ký tự trong charset brute-force | 36 (chữ thường a-z + số 0-9) |
| `L` | độ dài tối đa của mật khẩu trong không gian brute-force | 3 |
| `K` | kích thước không gian mật khẩu = `C¹+C²+...+C^L` | 47.988 |
| `k` | số vòng lặp của PBKDF2 | hằng `ITERATIONS` = 310.000 trong `SlowHashBenchmark` |

**Chi tiết tính toán lí thuyết**:

<details>
<summary><b>Mô phỏng 1 & 2: Brute-force + bảng băm tính sẵn, không salt</b> (<code>BruteForceAttacker</code>)</summary>

- *Cách làm*: băm TOÀN BỘ `K` chuỗi trong không gian mật khẩu một lần, lưu vào `HashMap<hash, chuỗi>`. Với mỗi người dùng, tra bảng bằng hash của họ — O(1) trung bình.
- *Độ phức tạp*: xây bảng tốn O(K) lần băm, mỗi lần tra O(1), tổng O(K + U). Bộ nhớ O(K) (phải lưu `K` chuỗi và hash).
- *Ý nghĩa*: chi phí chỉ tốn một lần rồi dùng lại cho mọi người dùng. Mô phỏng 1 (SHA-1) và mô phỏng 2 (SHA-256) dùng chung một lớp code, chỉ khác hàm băm truyền vào, để chứng minh: sử dụng các thuật toán không tốt cho mục đích và implement sai cách (không salt) cực kì có hại.

</details>

<details>
<summary><b>Mô phỏng 3: Brute-force riêng cho từng người, có salt</b> (<code>SaltedBruteForceAttacker</code>) — làm hỏng việc dùng chung bảng tính trước</summary>

- *Cách làm*: hash của mỗi người giờ là `sha256(salt_riêng + mật khẩu)`. Bảng ở mô phỏng 1/2 vô dụng vì cùng một chuỗi thử cho ra hash khác nhau ở mỗi người. Chương trình phải brute-force lại toàn không gian mật khẩu cho mỗi người dùng (dừng sớm nếu tìm thấy).
- *Độ phức tạp*: tổng O(U·K) trường hợp xấu nhất. Bộ nhớ O(1) — không lưu bảng nào, chỉ so khớp trực tiếp.
- *Ý nghĩa*: không còn dùng chung được cache (bảng tính sẵn). Salt chặn bảng tính trước dùng chung, không chặn brute-force từng mật khẩu — chỉ là kẻ tấn công phải trả giá hash (gần như) toàn bộ không gian cho từng người thay vì một lần.

</details>

<details>
<summary><b>Mô phỏng 4: Băm chậm (PBKDF2)</b> (<code>SlowHashBenchmark</code>) — tăng hằng số của mỗi lần đoán</summary>

- *Cách làm*: mỗi lần băm lặp `k` vòng (HMAC-SHA256), vẫn có salt, vẫn là brute-force từng người.
- *Độ phức tạp*: tổng O(U·K·k). Về lý thuyết (ký hiệu O) chỉ thêm một thừa số `k`, nhưng `k` lên tới hàng trăm nghìn nên trên thực tế khác biệt cực lớn.
- *Ý nghĩa*: ở mô phỏng 4, chạy THẬT PBKDF2 trên một mẫu nhỏ (vài người dùng × vài chuỗi trong không gian mật khẩu) để đo số hash mỗi giây thật của máy, rồi tính `thời gian ≈ (U × K) / số hash mỗi giây`. Chương trình không chạy hết vì sẽ mất nhiều ngày đến hàng năm (maybe) => thuật toán và salt có độ an toàn cao.

</details>

Tóm tắt:

| Mô phỏng | Thời gian của kẻ tấn công | Bộ nhớ của kẻ tấn công |
|---|---|---|
| 1. SHA-1, không salt | O(K + U) | O(K) |
| 2. SHA-256, không salt | O(K + U) | O(K) |
| 3. SHA-256, có salt | O(U·K) | O(1) |
| 4. PBKDF2, có salt | O(U·K·k) | O(1) |

## 6. Giới hạn của mô phỏng

Đây là mô phỏng được đơn giản hóa so với thực tiễn để demo chạy được trong vài giây trên laptop thường. Các điểm đơn giản hóa:

- **Không gian mật khẩu brute-force nhỏ.** Mặc định chỉ gồm chữ thường + số (36 ký tự) và độ dài tối đa 3 ký tự (`K` ≈ 48.000 khả năng). Mật khẩu thật thường dài 8+ ký tự với bảng chữ cái lớn (hoa, thường, số, ký tự đặc biệt), khiến `K` lớn hơn demo này rất nhiều lần.
- **Số người dùng nhỏ.** Mặc định 300 (có thể chỉnh bằng `--users`). Rò rỉ thật thì hàng triệu.
- **Giả định 90% người dùng có mật khẩu trong không gian brute-force, 10% nằm ngoài.** Thực tế có một số người nhận thức được nguy cơ, sử dụng mật khẩu rất dài và an toàn. Ta cho số đó chiếm 10%.
- **Chạy trên một CPU, không có GPU.** Thực tế GPU được ứng dụng để tăng tốc.  
- **Mô phỏng 4 không brute-force hết, chỉ đo mẫu nhỏ rồi ngoại suy tuyến tính.**

Tóm lại, demo không phản ánh tốc độ hay tỉ lệ bẻ khóa thực tiễn, chỉ dùng để so sánh đánh giá.

## 7. Cách chạy

Yêu cầu JDK 17+. Không thư viện ngoài — có hết trong thư viện chuẩn `java.security` / `javax.crypto` trong JDK.

Bash trên Linux:

```bash
# compile
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")

# chạy với giá trị mặc định (300 người dùng, charset 36 ký tự, độ dài tối đa 3)
java -cp out com.acmebreach.Main

# hoặc tự chỉnh
java -cp out com.acmebreach.Main --users 100 --max-length 2 --fast
```

Powershell:

```powershell
# Gom toàn bộ file .java vào biến và biên dịch
$files = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName

javac -encoding UTF-8 -d out $files

# Chạy chương trình với giá trị mặc định
java -cp out com.acmebreach.Main

# Hoặc tự chỉnh tham số
java -cp out com.acmebreach.Main --users 100 --max-length 2 --fast
```

Nếu Powershell cho ra chữ linh tinh, thì do nó không hỗ trợ tiếng việt. Chuyển sang hỗ trợ tiếng việt trong phiên hiện tại bằng cách:

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$OutputEncoding = [System.Text.Encoding]::UTF8
```

Các tùy chọn:

| Cờ | Tác dụng | Mặc định |
|---|---|---|
| `--users N` | số người dùng giả trong vụ "rò rỉ" | 300 |
| `--max-length N` | độ dài tối đa của không gian brute-force | 3 |
| `--charset STR` | bộ ký tự brute-force | `abcdefghijklmnopqrstuvwxyz0123456789` (36 ký tự) |
| `--secure-fraction D` | tỉ lệ người dùng có mật khẩu NẰM NGOÀI không gian brute-force | 0.10 |
| `--fast` | bỏ các khoảng dừng giữa các dòng | tắt |
| `--report PATH` | nơi ghi báo cáo sự cố dạng markdown | `INCIDENT_REPORT.md` |

**Lưu ý về hiệu năng:** kích thước không gian mật khẩu `K = C¹+C²+...+C^maxLength` tăng theo hàm MŨ. Tăng `--max-length` hoặc dùng `--charset` lớn hơn sẽ khiến demo chạy rất lâu, đặc biệt là mô phỏng 3 (chi phí O(U·K)). Chương trình sẽ từ chối chạy và báo lỗi nếu `K` vượt quá 50 triệu. Với giá trị mặc định, một lần chạy đầy đủ ( cả mô phỏng 4 với PBKDF2 thật trên mẫu nhỏ) mất khoảng 10 giây.

Chạy xong, hãy mở `INCIDENT_REPORT.md` — đó là một bản báo cáo sự cố nhỏ với bảng kết quả và phần "bài học rút ra", viết cho vui nhưng phần số liệu trong đó là thật của lần chạy đó.

## 8. Cấu trúc dự án

```
src/main/java/com/acmebreach/
├── Main.java                          điểm bắt đầu, chạy cả bốn mô phỏng
├── hashing/HashUtil.java              SHA-1, SHA-256, PBKDF2 + hàm hỗ trợ
├── bruteforce/Keyspace.java           sinh và đếm không gian mật khẩu brute-force
├── data/
│   ├── User.java                      một dòng người dùng giả
│   └── UserDatabase.java              tạo bảng người dùng giả 
├── attack/
│   ├── BruteForceAttacker.java        mô phỏng 1 & 2 (không salt, bảng tính sẵn dùng chung)
│   ├── SaltedBruteForceAttacker.java  mô phỏng 3 (có salt, brute-force riêng từng người)
│   └── SlowHashBenchmark.java         mô phỏng 4 (PBKDF2, đo mẫu thật rồi ngoại suy)
├── report/
│   ├── Format.java                    định dạng thời gian/dung lượng/phần trăm
│   └── IncidentReport.java            ghi báo cáo markdown
└── story/Narrator.java                    in ra màn hình
```
