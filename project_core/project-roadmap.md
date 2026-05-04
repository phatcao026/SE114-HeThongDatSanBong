# Lộ trình Phát triển Dự án Đặt Sân & Ghép Trận
**Thời gian: 8 tuần**

Tài liệu này mô tả roadmap triển khai dự án theo mô hình 4 thành viên, với frontend Android Studio và backend Spring Boot.

---

## 1. Tech Stack sử dụng

- **Frontend:** Android Studio
- **Backend:** Java Spring Boot
- **ORM:** Hibernate / JPA
- **Database:** PostgreSQL chạy bằng Docker
- **Công cụ quản lý DB:** DBeaver
- **Realtime:** WebSocket / SSE
- **Khóa phân tán:** Redis
- **Thanh toán:** MoMo / VNPay / Stripe
- **Auth:** JWT
- **Quản lý API:** RESTful API

---

## 2. Cơ cấu nhóm

Nhóm gồm 4 thành viên:

- **Android Developer 1**
  - Làm UI/UX Android
  - Màn hình đăng nhập, tìm sân, đặt sân
  - Danh sách booking

- **Android Developer 2**
  - Làm chat, notification, ghép trận
  - Màn hình profile, admin cơ bản
  - Tích hợp API còn lại

- **Backend Developer**
  - Xây dựng Spring Boot backend
  - Thiết kế database và API
  - Hibernate/JPA, Docker PostgreSQL, Redis lock, payment, websocket

- **BM**
  - Phân tích nghiệp vụ
  - Quản lý tiến độ
  - Kiểm thử tính năng
  - Làm báo cáo, slide, tài liệu

---

## 3. Giai đoạn 1: Khởi tạo và phân tích hệ thống (Tuần 1)

### Mục tiêu
Chốt yêu cầu và tạo nền tảng ban đầu.

### Việc cần làm
- Xác định rõ chức năng chính:
  - đặt sân
  - ghép trận
  - chat
  - thanh toán
  - admin
- Thiết kế sơ đồ database
- Thiết kế API core
- Chốt UI màn hình chính trên Android
- Xác định entity Hibernate/JPA cần có

### Kết quả mong đợi
- Có tài liệu nghiệp vụ
- Có ERD sơ bộ
- Có danh sách API
- Có flow màn hình chính

---

## 4. Giai đoạn 2: Xây dựng nền tảng hệ thống (Tuần 2)

### Backend
- Tạo project Spring Boot
- Tạo PostgreSQL bằng Docker
- Kết nối backend với database
- Cấu hình JPA/Hibernate, Lombok, Validation
- Xây dựng auth API
- Xây dựng user profile API

### Android
- Tạo project Android Studio
- Setup navigation
- Tạo màn hình login/register
- Tạo màn hình profile

### BM
- Rà soát yêu cầu
- Theo dõi tiến độ
- Chuẩn hóa tài liệu

---

## 5. Giai đoạn 3: Quản lý sân và booking (Tuần 3 - Tuần 4)

### Backend
- CRUD sân bóng
- CRUD khung giờ
- API xem lịch trống
- API đặt sân
- Redis lock chống trùng lịch nếu áp dụng

### Android
- Màn danh sách sân
- Màn chi tiết sân
- Màn đặt sân
- Màn lịch trống

### BM
- Kiểm thử nghiệp vụ booking
- Test tình huống nhiều người đặt cùng lúc
- Ghi nhận bug và góp ý logic

---

## 6. Giai đoạn 4: Thanh toán và xác nhận booking (Tuần 5)

### Backend
- Tích hợp payment gateway
- Xử lý webhook thanh toán
- Cập nhật trạng thái booking

### Android
- Màn thanh toán cọc
- Màn kết quả thanh toán
- Lịch sử đặt sân

### BM
- Chuẩn bị demo thanh toán
- Kiểm thử trạng thái thành công / thất bại

---

## 7. Giai đoạn 5: Ghép trận, chat và thông báo realtime (Tuần 6)

### Backend
- Match post API
- Match request API
- Conversation / message API
- WebSocket cho chat và notification

### Android
- Bảng tin ghép trận
- Màn chi tiết bài đăng
- Chat realtime
- Thông báo realtime

### BM
- Kiểm thử luồng ghép trận
- Soạn dữ liệu demo
- Rà soát logic hiển thị

---

## 8. Giai đoạn 6: Admin và thống kê (Tuần 7)

### Backend
- Admin API quản lý user
- Admin API quản lý sân
- Admin API quản lý bài ghép trận
- Dashboard thống kê

### Android
- Màn admin cơ bản
- Màn thống kê đơn giản

### BM
- Xác thực tính đúng đắn của dữ liệu
- Chuẩn bị phần mô tả trong báo cáo

---

## 9. Giai đoạn 7: Kiểm thử và hoàn thiện (Tuần 8)

### Backend
- Sửa bug
- Tối ưu database
- Viết dữ liệu mẫu
- Hoàn thiện API docs
- Kiểm tra dữ liệu bằng DBeaver

### Android
- Hoàn thiện UI
- Fix lỗi responsive
- Test máy thật

### BM
- Làm slide
- Viết báo cáo
- Soạn script thuyết trình
- Chuẩn bị video demo

---

## 10. Điểm nhấn của dự án

Các điểm nổi bật để trình bày với giảng viên:

- Ứng dụng Android thực tế, dễ demo
- Booking sân có chống trùng lịch bằng Redis nếu cần
- Ghép trận và chat realtime
- Thanh toán cọc
- Dashboard admin và thống kê
- PostgreSQL chạy bằng Docker, thao tác bằng DBeaver
- Kiến trúc rõ ràng, phù hợp đồ án nhóm

---

## 11. Kết luận

Roadmap này giúp nhóm triển khai dự án theo từng giai đoạn rõ ràng, dễ quản lý và dễ bảo vệ. Với frontend Android Studio và backend Spring Boot, nhóm có thể tập trung vào tính năng cốt lõi, giảm rủi ro khi tích hợp và nâng cao chất lượng demo cuối kỳ.