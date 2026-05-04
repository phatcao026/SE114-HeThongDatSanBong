# Kế Hoạch Thực Hiện Dự Án Đặt Sân & Ghép Trận
**Thời gian thực hiện: 2 tháng (8 tuần)**

Dự án được phát triển theo mô hình chia chức năng rõ ràng, ưu tiên hoàn thành các tính năng cốt lõi trước, sau đó mở rộng sang các tính năng realtime, ghép trận và quản trị hệ thống.

## 1. Thông tin tổng quan

- **Frontend:** Android Studio
- **Backend:** Spring Boot
- **ORM:** Hibernate / JPA
- **Database:** PostgreSQL chạy bằng Docker
- **Công cụ thao tác DB:** DBeaver
- **Realtime:** WebSocket / SSE
- **Khóa phân tán:** Redis
- **Thanh toán:** MoMo / VNPay / Stripe tùy khả năng tích hợp

## 2. Cơ cấu nhóm thực hiện

Dự án có 4 thành viên:

- **2 thành viên Android**
  - Thiết kế giao diện ứng dụng Android
  - Làm màn hình đăng nhập, tìm sân, đặt sân, chat, thông báo
  - Tích hợp API backend
- **1 thành viên Backend**
  - Xây dựng API bằng Spring Boot
  - Thiết kế database, xử lý booking, auth, chat, notification
  - Tích hợp Hibernate, Redis lock, websocket, payment
- **1 thành viên BM**
  - Phân tích yêu cầu nghiệp vụ
  - Viết tài liệu, quản lý tiến độ, test nghiệp vụ
  - Hỗ trợ báo cáo, slide, demo

---

## 3. Giai đoạn 1: Phân tích yêu cầu và thiết kế hệ thống (Tuần 1)

### Mục tiêu
Chốt rõ phạm vi dự án và kiến trúc tổng thể trước khi bắt tay vào code.

### Công việc
- Phân tích nghiệp vụ chính:
  - Đặt sân online
  - Ghép trận
  - Thanh toán cọc
  - Chat nội bộ
  - Thông báo realtime
- Thiết kế luồng người dùng:
  - Đăng ký / đăng nhập
  - Tìm sân
  - Đặt sân
  - Tạo bài ghép trận
  - Nhận kèo / chat
- Thiết kế UI sơ bộ cho ứng dụng Android
- Thiết kế database schema ban đầu
- Xác định danh sách API cần có
- Xác định các entity JPA/Hibernate cần xây dựng

### Sản phẩm đầu ra
- Tài liệu yêu cầu hệ thống
- Wireframe sơ bộ
- ERD sơ bộ
- Danh sách API core

---

## 4. Giai đoạn 2: Khởi tạo dự án và xây dựng module nền tảng (Tuần 2)

### Mục tiêu
Tạo được bộ khung dự án chạy được ở cả frontend và backend.

### Công việc
- Khởi tạo project Android Studio
- Khởi tạo project Spring Boot
- Tạo PostgreSQL bằng Docker
- Kết nối backend với PostgreSQL
- Cấu hình Hibernate/JPA, CORS, biến môi trường
- Chuẩn hóa cấu trúc package backend
- Xây dựng module xác thực:
  - đăng ký
  - đăng nhập
  - phân quyền theo role
- Xây dựng module hồ sơ cá nhân

### Phân công
- **Android 1:** layout đăng nhập/đăng ký, hồ sơ cá nhân
- **Android 2:** màn hình điều hướng, profile, cơ bản UI
- **Backend:** auth API, entity, repository, config DB, Docker DB
- **BM:** chốt nghiệp vụ, kiểm tra tài liệu, test luồng

### Sản phẩm đầu ra
- App Android kết nối được backend
- API auth chạy ổn
- Database chạy bằng Docker và thao tác được bằng DBeaver

---

## 5. Giai đoạn 3: Quản lý sân và đặt sân (Tuần 3 - Tuần 4)

### Mục tiêu
Hoàn thiện chức năng cốt lõi nhất của hệ thống: tìm sân và đặt sân.

### Công việc
- Quản lý sân bóng:
  - thêm / sửa / xóa sân
  - cập nhật giá
  - cập nhật trạng thái sân
- Quản lý khung giờ:
  - tạo time slot
  - xem lịch trống
- Tìm kiếm sân:
  - theo loại sân
  - theo giá
  - theo ngày
- Đặt sân:
  - chọn sân
  - chọn khung giờ
  - giữ chỗ
  - tạo booking
- Áp dụng Redis Lock để tránh trùng lịch nếu cần

### Phân công
- **Android 1:** màn tìm sân, danh sách sân, booking flow
- **Android 2:** UI chi tiết sân, lịch trống, trạng thái booking
- **Backend:** field/time-slot/booking API, Hibernate entity, booking logic
- **BM:** test nghiệp vụ đặt sân, kiểm tra cạnh tranh booking

### Sản phẩm đầu ra
- User tìm được sân
- User đặt được sân
- Không bị trùng lịch khi nhiều người cùng đặt

---

## 6. Giai đoạn 4: Thanh toán và xác nhận đặt sân (Tuần 5)

### Mục tiêu
Bổ sung bước thanh toán cọc và xác nhận booking.

### Công việc
- Tích hợp thanh toán:
  - MoMo / VNPay / Stripe tùy khả năng
- Tạo trạng thái booking:
  - pending
  - deposit_paid
  - confirmed
  - cancelled
- Xử lý webhook thanh toán
- Cập nhật trạng thái booking theo kết quả thanh toán

### Phân công
- **Android 1:** màn thanh toán
- **Android 2:** màn trạng thái thanh toán / lịch sử booking
- **Backend:** payment API, webhook, trạng thái hóa đơn
- **BM:** test luồng thanh toán, soạn kịch bản demo

### Sản phẩm đầu ra
- Người dùng thanh toán cọc
- Booking được cập nhật đúng trạng thái

---

## 7. Giai đoạn 5: Ghép trận, chat và thông báo realtime (Tuần 6)

### Mục tiêu
Tạo tính năng xã hội cho hệ thống.

### Công việc
- Tạo bảng tin ghép trận
- Đăng bài tìm đối thủ / tìm người
- Gửi yêu cầu nhận kèo
- Tạo conversation khi chấp nhận kèo
- Chat realtime
- Thông báo realtime khi có sự kiện mới

### Phân công
- **Android 1:** danh sách bài ghép trận
- **Android 2:** chat, notification, chi tiết bài viết
- **Backend:** match post, request, conversation, message, websocket
- **BM:** kiểm tra luồng ghép trận, tạo dữ liệu demo

### Sản phẩm đầu ra
- Người dùng đăng bài ghép trận
- Có thể nhận kèo và chat trực tiếp
- Có thông báo realtime

---

## 8. Giai đoạn 6: Admin và báo cáo hệ thống (Tuần 7)

### Mục tiêu
Bổ sung chức năng quản trị và thống kê.

### Công việc
- Quản lý người dùng
- Quản lý sân
- Quản lý bài ghép trận
- Thống kê tổng quan:
  - số user
  - số sân
  - số booking
  - số bài ghép trận
- Thiết kế dashboard đơn giản cho admin

### Phân công
- **Android 1:** giao diện admin cơ bản
- **Android 2:** dashboard / thống kê
- **Backend:** admin API, thống kê dữ liệu
- **BM:** chuẩn bị báo cáo và phần mô tả chức năng

### Sản phẩm đầu ra
- Có màn admin
- Có dữ liệu thống kê
- Dự án nhìn hoàn chỉnh hơn

---

## 9. Giai đoạn 7: Kiểm thử, hoàn thiện và chuẩn bị bảo vệ (Tuần 8)

### Mục tiêu
Hoàn thiện toàn bộ hệ thống và chuẩn bị thuyết trình.

### Công việc
- Kiểm thử chức năng chính
- Sửa lỗi giao diện và logic
- Tối ưu truy vấn database
- Tạo dữ liệu mẫu
- Viết tài liệu hướng dẫn
- Chuẩn bị slide và video demo
- Thao tác kiểm tra dữ liệu bằng DBeaver

### Phân công
- **Android 1:** fix UI/UX
- **Android 2:** fix flow, test thiết bị
- **Backend:** fix API, database, performance
- **BM:** slide, báo cáo, script thuyết trình

### Sản phẩm đầu ra
- Ứng dụng chạy ổn định
- Có dữ liệu demo
- Có tài liệu và slide bảo vệ

---

## 10. Kết luận

Dự án được chia theo từng tuần để đảm bảo hoàn thành các chức năng cốt lõi trước, sau đó mở rộng sang realtime, chat, thanh toán và quản trị. Với cách tổ chức này, nhóm 4 người có thể triển khai hiệu quả và dễ demo sản phẩm cuối cùng.