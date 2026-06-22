<p align="center">
  <img src="https://tapchicongthuong.vn/images/22/8/22/logo-truong-dai-hoc-cong-nghe-thong-tin.jpg" alt="UIT Logo" width="400">
</p>

<h1 align="center">HỆ THỐNG ĐẶT SÂN BÓNG</h1>

<p align="center">
  <strong>Đồ án môn học: Nhập môn Ứng dụng Di động (SE114)</strong><br><br>
  <a href="https://github.com/phatcao026/SE114-HeThongDatSanBong">
    <img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Repo">
  </a>
</p>

<p align="center">
  <a href="#gioi-thieu">Giới thiệu</a> •
  <a href="#cau-truc">Cấu trúc</a> •
  <a href="#kien-truc">Kiến trúc</a> •
  <a href="#cong-nghe">Công nghệ</a> •
  <a href="#cai-dat">Yêu cầu cài đặt</a> •
  <a href="#huong-dan">Hướng dẫn chạy</a> •
  <a href="#tai-khoan">Tài khoản Test</a>
</p>

---

## 👥 Sinh viên thực hiện

| STT | MSSV | Họ và tên | GitHub |
|:---:|:---:|:---:|:---:|
| 1 | **24521289** | Cao Tiến Phát | [phatcao026](https://github.com/phatcao026) |
| 2 | **24521018** | Trần Việt Long | [TraVisL586](https://github.com/TraVisL586) |
| 3 | **24520812** | Đỗ Đăng Khoa | [DOKA206](https://github.com/DOKA206) |
| 4 | **24520290** | Trần Quang Đạt | [FazVn](https://github.com/FazVn) |

---

<a id="gioi-thieu"></a>
## 📖 Giới Thiệu Dự Án

Đây là dự án Hệ Thống Đặt Sân Bóng được phát triển cho môn học SE114. Ứng dụng cung cấp các tính năng giúp người dùng tìm kiếm, đặt lịch sân bóng, thanh toán trực tuyến, cũng như giúp chủ sân quản lý lịch đặt sân và doanh thu một cách hiệu quả.

<a id="cau-truc"></a>
## 📂 Cấu Trúc Dự Án (Folder Structure)

```text
SE114-HeThongDatSanBong/
├── backend/                  # Mã nguồn Server (Spring Boot)
│   ├── src/main/java/        # Logic nghiệp vụ, Controller, Entity, Service...
│   ├── src/main/resources/   # Cấu hình dự án, Flyway migrations
│   ├── docker-compose.yml    # Cấu hình deploy Database & Redis qua Docker
│   └── pom.xml               # Khai báo dependency Maven
├── android/                  # Mã nguồn Client (Android App)
│   ├── app/src/main/java/    # Components, Activities, Fragments, ViewModels...
│   ├── app/src/main/res/     # Layouts, Resources, Drawables...
│   └── build.gradle          # Cấu hình build Gradle cho Android
├── docs/                     # Tài liệu chung của dự án
└── README.md                 # Tài liệu hướng dẫn dự án
```

<a id="kien-truc"></a>
## 🏛️ Kiến Trúc Hệ Thống (Architecture)

Dự án được thiết kế theo mô hình **Client-Server** với sự phân tách độc lập giữa Mobile App và Backend:

1. **Client (Android App):** 
   - Ứng dụng di động native dành cho hệ điều hành Android.
   - Đảm nhiệm phần giao diện người dùng (UI/UX), xử lý các tương tác của người dùng.
   - Giao tiếp với Backend thông qua các RESTful API.
2. **Backend (Server - Spring Boot):** 
   - Xử lý các logic nghiệp vụ (Business Logic).
   - Quản lý xác thực và phân quyền (Authentication & Authorization) sử dụng JWT.
   - Xử lý các dịch vụ thời gian thực, giao dịch thanh toán và cung cấp RESTful API.
3. **Database (PostgreSQL):** 
   - Hệ quản trị cơ sở dữ liệu quan hệ lưu trữ toàn bộ dữ liệu của hệ thống.
   - Cấu trúc database (schema) và dữ liệu khởi tạo được quản lý tự động thông qua Flyway Migration.
4. **Caching (Redis):** 
   - Hỗ trợ lưu trữ tạm thời (cache) các dữ liệu thường xuyên truy vấn để tối ưu hóa hiệu suất API.

Môi trường cơ sở dữ liệu và caching được container hóa bằng **Docker Compose** để dễ dàng khởi tạo và đồng nhất giữa các môi trường phát triển.

<a id="cong-nghe"></a>
## 🛠️ Công Nghệ Sử Dụng

### Client (Android)
- **Ngôn ngữ:** Java
- **IDE:** Android Studio
- **Kiến trúc:** MVVM (Model-View-ViewModel) / MVC
- **Khác:** Retrofit (gọi API), Glide (xử lý hình ảnh), Room Database...

### Backend
- **Framework:** Spring Boot 3.5.0, Java 21
- **Database:** PostgreSQL
- **Caching:** Redis
- **Security:** Spring Security (JWT Authentication)
- **Payment & Cloud:** Stripe API, Firebase Admin
- **Migration:** Flyway
- **Tooling:** Maven, Docker & Docker Compose

---

<a id="cai-dat"></a>
## 📋 Yêu Cầu Cài Đặt (Prerequisites)

Trước khi chạy dự án, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:
- **[Android Studio](https://developer.android.com/studio)**
- **[Java Development Kit (JDK) 21](https://jdk.java.net/21/)**
- **[Docker](https://www.docker.com/)** và **Docker Compose** (Dành cho việc chạy Database và Redis)
- **Git**

---

<a id="huong-dan"></a>
## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 1. Clone Repository

```bash
git clone https://github.com/phatcao026/SE114-HeThongDatSanBong.git
cd SE114-HeThongDatSanBong
```

### 2. Chạy Backend (Spring Boot + PostgreSQL + Redis)

Di chuyển vào thư mục `backend`:
```bash
cd backend
```

**Bước 1: Cấu hình biến môi trường**
Tạo file `.env` từ file `.env.example`:
```bash
cp .env.example .env
```
*(Cập nhật lại các biến môi trường như thông tin Database, JWT, Stripe,... nếu cần)*

**Bước 2: Khởi chạy Database và Caching bằng Docker**
Dự án sử dụng Docker Compose để khởi tạo PostgreSQL và Redis:
```bash
docker-compose up -d
```

**Bước 3: Khởi chạy Backend Application**
Sử dụng Maven Wrapper (`mvnw`) được tích hợp sẵn để chạy app:
- Trên **Windows**:
  ```cmd
  mvnw.cmd spring-boot:run
  ```
- Trên **Linux/macOS**:
  ```bash
  ./mvnw spring-boot:run
  ```

*Backend server sẽ khởi chạy tại `http://localhost:8080`.*

### 3. Quản Lý Database & Nạp Dữ Liệu Mẫu

Dự án sử dụng cơ sở dữ liệu PostgreSQL được khởi chạy qua Docker Compose. Bạn có thể sử dụng các công cụ quản lý Database và kết nối với các thông số (xem chi tiết trong `docker-compose.yml`):
- **Host:** `localhost`
- **Port:** `5432` (hoặc cổng cấu hình tương ứng)
- **Database Name:** Tùy thuộc cấu hình
- **Username/Password:** Theo cấu hình trong file docker/env.

**Nạp dữ liệu mẫu:** Hệ thống sẽ tự động thiết lập các bảng thông qua Flyway Migration khi chạy backend. Nếu có file SQL nạp dữ liệu mẫu riêng, bạn có thể chạy (Execute SQL Script) vào database sau khi các bảng đã tạo xong.

### 4. Chạy Android App

Mở ứng dụng Android Studio:

**Bước 1: Mở dự án**
Chọn **Open** và trỏ đến thư mục `/android` của dự án này.

**Bước 2: Cài đặt và Đồng bộ hóa Gradle**
Chờ hệ thống Gradle đồng bộ (sync) và tự động tải về các thư viện cần thiết.

**Bước 3: Cấu hình địa chỉ Backend**
- Nếu chạy trên **Android Emulator**, đổi BASE_URL kết nối đến backend thành `http://10.0.2.2:8080`.
- Nếu chạy trên **thiết bị thật**, bạn cần đảm bảo máy tính và điện thoại dùng chung mạng WiFi và đổi BASE_URL sang IP LAN của máy tính (ví dụ: `http://192.168.1.x:8080`).

**Bước 4: Khởi chạy Frontend**
Nhấn nút **Run** (biểu tượng hình tam giác màu xanh) trên thanh công cụ của Android Studio để bắt đầu quá trình biên dịch và cài đặt ứng dụng lên thiết bị hoặc máy ảo.

---

<a id="tai-khoan"></a>
## 👤 Danh Sách Tài Khoản Mẫu (Test Accounts)

Sau khi hệ thống khởi chạy, bạn có thể đăng ký tài khoản trực tiếp qua ứng dụng hoặc sử dụng các tài khoản đã được nạp qua dữ liệu mẫu (nếu có).

- **Admin/Chủ sân:** (Tài khoản mẫu nếu có)
- **Khách hàng (User):** (Tài khoản mẫu nếu có)

Chúc bạn trải nghiệm và phát triển dự án tốt! 🚀
