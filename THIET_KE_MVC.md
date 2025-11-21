# THIẾT KẾ MÔ HÌNH MVC - PDF TO DOC CONVERTER

## 1. TỔNG QUAN MÔ HÌNH MVC

Dự án được xây dựng theo mô hình **MVC (Model-View-Controller)**, một kiến trúc phần mềm phổ biến giúp tách biệt logic nghiệp vụ, giao diện người dùng và điều khiển luồng xử lý.

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT (Browser)                      │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP Request/Response
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ LoginServlet │  │ UploadServlet│  │ TaskServlet  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                 │                  │           │
└─────────┼─────────────────┼──────────────────┼──────────┘
          │                 │                  │
          ▼                 ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                      MODEL LAYER                        │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────┐ │
│  │ User.java│  │ConversionTask│  │  DAO Classes     │ │
│  └────┬─────┘  └──────┬───────┘  └────────┬─────────┘ │
│       │               │                    │           │
└───────┼───────────────┼────────────────────┼───────────┘
        │               │                    │
        ▼               ▼                    ▼
┌─────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                       │
│              MySQL (pdfconverter)                       │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│                      VIEW LAYER                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │login.jsp │  │dashboard │  │tasks.jsp │            │
│  └──────────┘  └──────────┘  └──────────┘            │
└─────────────────────────────────────────────────────────┘
```

---

## 2. CẤU TRÚC THƯ MỤC

```
FinalProjectLTM/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/finalproject/
│   │   │       ├── config/          # Cấu hình
│   │   │       │   └── DatabaseConfig.java
│   │   │       ├── controller/      # CONTROLLER LAYER
│   │   │       │   ├── LoginServlet.java
│   │   │       │   ├── LogoutServlet.java
│   │   │       │   ├── RegisterServlet.java
│   │   │       │   ├── UploadServlet.java
│   │   │       │   ├── TaskServlet.java
│   │   │       │   └── DownloadServlet.java
│   │   │       ├── dao/             # DATA ACCESS LAYER
│   │   │       │   ├── UserDAO.java
│   │   │       │   └── ConversionTaskDAO.java
│   │   │       ├── model/           # MODEL LAYER
│   │   │       │   ├── User.java
│   │   │       │   └── ConversionTask.java
│   │   │       ├── service/         # BUSINESS LOGIC
│   │   │       │   ├── ConversionQueue.java
│   │   │       │   ├── ConversionWorker.java
│   │   │       │   └── PDFToDOCConverter.java
│   │   │       ├── listener/        # Application Listener
│   │   │       │   └── ApplicationListener.java
│   │   │       └── util/            # Utilities
│   │   │           └── DatabaseConnection.java
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml          # Cấu hình web
│   │       ├── index.jsp            # VIEW LAYER
│   │       ├── login.jsp
│   │       ├── register.jsp
│   │       ├── dashboard.jsp
│   │       └── tasks.jsp
│   └── test/
├── database/
│   └── schema.sql                   # Database schema
├── pom.xml                          # Maven configuration
├── THIET_KE_MVC.md                  # Tài liệu này
└── HUONG_DAN_CAI_DAT.md            # Hướng dẫn cài đặt
```

---

## 3. CHI TIẾT CÁC LỚP

### 3.1. MODEL LAYER

#### User.java
- **Mục đích**: Đại diện cho thông tin người dùng
- **Thuộc tính**:
  - `id`: ID người dùng
  - `username`: Tên đăng nhập
  - `password`: Mật khẩu
  - `email`: Email
  - `fullName`: Họ và tên

#### ConversionTask.java
- **Mục đích**: Đại diện cho tác vụ chuyển đổi PDF sang DOC
- **Thuộc tính**:
  - `id`: ID tác vụ
  - `userId`: ID người dùng sở hữu
  - `originalFileName`: Tên file PDF gốc
  - `originalFilePath`: Đường dẫn file PDF
  - `convertedFileName`: Tên file DOC đã chuyển đổi
  - `convertedFilePath`: Đường dẫn file DOC
  - `status`: Trạng thái (PENDING, PROCESSING, COMPLETED, FAILED)
  - `createdAt`: Thời gian tạo
  - `completedAt`: Thời gian hoàn thành
  - `errorMessage`: Thông báo lỗi (nếu có)

---

### 3.2. CONTROLLER LAYER

#### LoginServlet.java
- **Chức năng**: Xử lý đăng nhập người dùng
- **URL**: `/login`
- **Phương thức**:
  - `doGet()`: Hiển thị form đăng nhập
  - `doPost()`: Xác thực thông tin đăng nhập

#### RegisterServlet.java
- **Chức năng**: Xử lý đăng ký tài khoản mới
- **URL**: `/register`
- **Phương thức**:
  - `doGet()`: Hiển thị form đăng ký
  - `doPost()`: Tạo tài khoản mới

#### UploadServlet.java
- **Chức năng**: Xử lý upload file PDF và tạo tác vụ chuyển đổi
- **URL**: `/upload`
- **Phương thức**:
  - `doPost()`: Nhận file PDF, lưu vào server, tạo task và đưa vào queue

#### TaskServlet.java
- **Chức năng**: Hiển thị danh sách tác vụ của người dùng
- **URL**: `/tasks`
- **Phương thức**:
  - `doGet()`: Lấy danh sách tasks và hiển thị

#### DownloadServlet.java
- **Chức năng**: Tải file PDF hoặc DOC về máy
- **URL**: `/download`
- **Phương thức**:
  - `doGet()`: Stream file về client

#### LogoutServlet.java
- **Chức năng**: Đăng xuất người dùng
- **URL**: `/logout`
- **Phương thức**:
  - `doGet()`: Hủy session và chuyển về trang đăng nhập

---

### 3.3. DAO LAYER (Data Access Object)

#### UserDAO.java
- **Chức năng**: Thao tác với bảng `users` trong database
- **Phương thức**:
  - `authenticate()`: Xác thực đăng nhập
  - `getUserById()`: Lấy thông tin user theo ID
  - `register()`: Đăng ký user mới

#### ConversionTaskDAO.java
- **Chức năng**: Thao tác với bảng `conversion_tasks` trong database
- **Phương thức**:
  - `createTask()`: Tạo task mới
  - `getTasksByUserId()`: Lấy danh sách tasks của user
  - `getTaskById()`: Lấy task theo ID
  - `getPendingTasks()`: Lấy danh sách tasks đang chờ
  - `updateTask()`: Cập nhật thông tin task

---

### 3.4. SERVICE LAYER (Business Logic)

#### ConversionQueue.java
- **Chức năng**: Quản lý hàng đợi các tác vụ chuyển đổi
- **Pattern**: Singleton
- **Phương thức**:
  - `getInstance()`: Lấy instance duy nhất
  - `startWorker()`: Khởi động worker thread
  - `stopWorker()`: Dừng worker thread
  - `addTask()`: Thêm task vào queue
  - `getQueueSize()`: Lấy số lượng task trong queue

#### ConversionWorker.java
- **Chức năng**: Worker thread xử lý các task trong queue
- **Pattern**: Runnable (chạy trong background thread)
- **Phương thức**:
  - `run()`: Vòng lặp lấy task từ queue và xử lý
  - `processTask()`: Xử lý một task cụ thể
  - `stop()`: Dừng worker

#### PDFToDOCConverter.java
- **Chức năng**: Chuyển đổi file PDF sang DOCX
- **Thư viện sử dụng**:
  - Apache PDFBox: Đọc nội dung PDF
  - Apache POI: Tạo file DOCX
- **Phương thức**:
  - `convert()`: Thực hiện chuyển đổi

---

### 3.5. VIEW LAYER

#### index.jsp
- **Mục đích**: Trang chủ, chuyển hướng đến đăng nhập

#### login.jsp
- **Mục đích**: Form đăng nhập
- **Thành phần**:
  - Input username
  - Input password
  - Nút đăng nhập
  - Link đăng ký

#### register.jsp
- **Mục đích**: Form đăng ký
- **Thành phần**:
  - Input username
  - Input password
  - Input email
  - Input fullName
  - Nút đăng ký
  - Link đăng nhập

#### dashboard.jsp
- **Mục đích**: Trang chính sau khi đăng nhập
- **Thành phần**:
  - Form upload file PDF
  - Hướng dẫn sử dụng
  - Header với thông tin user

#### tasks.jsp
- **Mục đích**: Hiển thị danh sách tác vụ chuyển đổi
- **Thành phần**:
  - Bảng danh sách tasks
  - Trạng thái của từng task
  - Nút tải file PDF/DOC
  - Auto-refresh mỗi 5 giây

---

### 3.6. UTILITY LAYER

#### DatabaseConnection.java
- **Chức năng**: Quản lý kết nối database
- **Pattern**: Singleton
- **Phương thức**:
  - `initialize()`: Khởi tạo connection pool
  - `getInstance()`: Lấy instance
  - `getConnection()`: Lấy connection từ pool

#### DatabaseConfig.java
- **Chức năng**: Đọc cấu hình database từ web.xml
- **Thuộc tính**:
  - `driver`: JDBC driver
  - `url`: Database URL
  - `username`: Username
  - `password`: Password

---

## 4. LUỒNG XỬ LÝ CHÍNH

### 4.1. Luồng đăng nhập
```
Client → LoginServlet.doPost() 
      → UserDAO.authenticate() 
      → Database
      → Session.setAttribute("user")
      → Redirect to dashboard.jsp
```

### 4.2. Luồng upload và chuyển đổi
```
Client → UploadServlet.doPost()
      → Lưu file PDF vào server
      → ConversionTaskDAO.createTask()
      → ConversionQueue.addTask()
      → ConversionWorker (background thread)
      → PDFToDOCConverter.convert()
      → ConversionTaskDAO.updateTask()
      → Client xem kết quả tại tasks.jsp
```

### 4.3. Luồng xem danh sách tasks
```
Client → TaskServlet.doGet()
      → ConversionTaskDAO.getTasksByUserId()
      → Database
      → tasks.jsp (hiển thị danh sách)
```

---

## 5. XỬ LÝ NỀN (BACKGROUND PROCESSING)

### 5.1. Queue System
- Sử dụng `BlockingQueue` để quản lý hàng đợi
- Đảm bảo thread-safe khi nhiều user upload đồng thời
- Worker thread chạy liên tục, lấy task từ queue và xử lý

### 5.2. Worker Thread
- Chạy như daemon thread, không chặn việc shutdown server
- Tự động xử lý các task theo thứ tự FIFO
- Cập nhật trạng thái task trong database sau mỗi bước

### 5.3. Khởi động tự động
- `ApplicationListener` khởi động queue worker khi ứng dụng deploy
- Dừng worker khi ứng dụng undeploy

---

## 6. BẢO MẬT

### 6.1. Session Management
- Kiểm tra session trước mỗi request quan trọng
- Redirect về login nếu chưa đăng nhập

### 6.2. File Access Control
- Chỉ cho phép user tải file của chính mình
- Kiểm tra `userId` trước khi cho phép download

### 6.3. Input Validation
- Kiểm tra định dạng file (chỉ chấp nhận PDF)
- Giới hạn kích thước file (50MB)

---

## 7. DATABASE SCHEMA

### Bảng users
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Bảng conversion_tasks
```sql
CREATE TABLE conversion_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    original_file_path VARCHAR(500) NOT NULL,
    converted_file_name VARCHAR(255),
    converted_file_path VARCHAR(500),
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    error_message TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 8. DEPENDENCIES

### Apache PDFBox
- Đọc và trích xuất nội dung từ file PDF

### Apache POI
- Tạo và ghi file DOCX

### MySQL Connector
- Kết nối với MySQL database

### Commons FileUpload
- Xử lý upload file multipart

---

## 9. KẾT LUẬN

Dự án được thiết kế theo mô hình MVC rõ ràng:
- **Model**: User, ConversionTask
- **View**: Các file JSP
- **Controller**: Các Servlet

Hệ thống xử lý nền sử dụng queue và worker thread để đảm bảo:
- Không block request của client
- Xử lý tuần tự các tác vụ
- Có thể mở rộng để xử lý nhiều worker thread

Database được thiết kế với quan hệ rõ ràng và index để tối ưu truy vấn.

