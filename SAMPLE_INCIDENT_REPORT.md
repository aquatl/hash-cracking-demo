Đây là incident report được chạy trước trên máy của mình. Nếu bạn chạy trên máy của bạn sẽ có một `INCIDENT_REPORT.md` riêng ở thư mục gốc của dự án.

# Báo cáo sự cố: Vụ rò rỉ AcmeCorp

**Ngày:** 2026-11-18T03:54:30.935151813  
**Số người dùng bị ảnh hưởng:** 300  
**Không gian brute-force của kẻ tấn công:** charset "abcdefghijklmnopqrstuvwxyz0123456789" (36 ký tự), độ dài tối đa 3 → 47988 khả năng (K)

## Kết quả

| Tình huống | Cách tấn công | Số người bị bẻ | Thời gian của kẻ tấn công | Bộ nhớ của kẻ tấn công | Đánh giá |
|---|---|---|---|---|---|
| 1. SHA-1, không salt | Brute-force + bảng băm tính sẵn (dùng chung) | 270 / 300 (90.0%) | 58.32 ms | 4.4 MB (bảng O(K)) | Thảm họa — tốn công một lần, bẻ được mọi người dùng trong không gian |
| 2. SHA-256, không salt | Brute-force + bảng băm tính sẵn (dùng chung) | 270 / 300 (90.0%) | 49.27 ms | 4.4 MB (bảng O(K)) | Vẫn là thảm họa — đổi hàm băm không giúp được gì nếu thiếu salt |
| 3. SHA-256, có salt | Brute-force riêng cho từng người (không dùng lại được bảng tính sẵn) | 270 / 300 (90.0%) | 1.28 giây | không đáng kể (không lưu bảng nào, O(1)) | Mật khẩu trong không gian brute-force vẫn bị bẻ; chi phí tăng theo numUsers x K |
| 4. PBKDF2 (310000 vòng), có salt | Brute-force riêng cho từng người, đo mẫu thật rồi ngoại suy | chỉ là ước tính — không chạy hết | ước tính 13.5 ngày để brute-force hết với mọi người dùng | không đáng kể (không lưu bảng nào, O(1)) | Tấn công trở nên bất khả thi khi quy mô lớn |
