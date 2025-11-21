# HƯỚNG DẪN CÀI ĐẶT VÀ SỬ DỤNG HỆ THỐNG PDF TO DOC CONVERTER

## MỤC LỤC
1. [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
2. [Cài đặt Java JDK](#cài-đặt-java-jdk)
3. [Cài đặt Apache Tomcat](#cài-đặt-apache-tomcat)
4. [Cài đặt MySQL Database](#cài-đặt-mysql-database)
5. [Cài đặt Maven](#cài-đặt-maven)
6. [Cấu hình dự án](#cấu-hình-dự-án)
7. [Build và Deploy](#build-và-deploy)
8. [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)

---

## YÊU CẦU HỆ THỐNG

- **Hệ điều hành**: Windows 10/11, Linux, hoặc macOS
- **Java JDK**: Version 8 trở lên
- **Apache Tomcat**: Version 9.0 trở lên
- **MySQL**: Version 8.0 trở lên
- **Maven**: Version 3.6 trở lên

---

## CÀI ĐẶT JAVA JDK

### Bước 1: Tải Java JDK
1. Truy cập: https://www.oracle.com/java/technologies/downloads/
2. Tải JDK 8 hoặc phiên bản mới hơn
3. Chọn phiên bản phù hợp với hệ điều hành của bạn

### Bước 2: Cài đặt
1. Chạy file cài đặt đã tải về
2. Làm theo hướng dẫn của trình cài đặt
3. Ghi nhớ đường dẫn cài đặt (thường là `C:\Program Files\Java\jdk-xx`)

### Bước 3: Cấu hình biến môi trường
1. Mở **System Properties** → **Environment Variables**
2. Tạo biến mới `JAVA_HOME` trỏ đến thư mục JDK (ví dụ: `C:\Program Files\Java\jdk-1.8.0_xxx`)
3. Thêm `%JAVA_HOME%\bin` vào biến `Path`
4. Mở Command Prompt và kiểm tra:
   ```bash
   java -version
   javac -version
   ```

---

## CÀI ĐẶT APACHE TOMCAT

### Bước 1: Tải Apache Tomcat
1. Truy cập: https://tomcat.apache.org/download-90.cgi
2. Tải file ZIP (64-bit Windows zip) hoặc file phù hợp với hệ điều hành

### Bước 2: Giải nén
1. Giải nén file ZIP vào thư mục (ví dụ: `C:\apache-tomcat-9.0.xx`)
2. Ghi nhớ đường dẫn này

### Bước 3: Cấu hình biến môi trường
1. Tạo biến mới `CATALINA_HOME` trỏ đến thư mục Tomcat
2. Thêm `%CATALINA_HOME%\bin` vào biến `Path`

### Bước 4: Kiểm tra cài đặt
1. Mở Command Prompt
2. Chạy lệnh:
   ```bash
   cd %CATALINA_HOME%\bin
   startup.bat
   ```
3. Mở trình duyệt và truy cập: http://localhost:8080
4. Nếu thấy trang chào mừng Tomcat thì cài đặt thành công

### Bước 5: Dừng Tomcat
- Chạy lệnh: `shutdown.bat` hoặc đóng cửa sổ Command Prompt

---

## CÀI ĐẶT MYSQL DATABASE

### Bước 1: Tải MySQL
1. Truy cập: https://dev.mysql.com/downloads/installer/
2. Tải MySQL Installer for Windows

### Bước 2: Cài đặt
1. Chạy file cài đặt
2. Chọn **Developer Default** hoặc **Server only**
3. Làm theo hướng dẫn, đặt mật khẩu cho user `root`
4. Ghi nhớ mật khẩu này (sẽ cần cho cấu hình dự án)

### Bước 3: Kiểm tra cài đặt
1. Mở Command Prompt
2. Chạy lệnh:
   ```bash
   mysql -u root -p
   ```
3. Nhập mật khẩu root
4. Nếu vào được MySQL prompt thì cài đặt thành công

### Bước 4: Tạo database
1. Trong MySQL prompt, chạy các lệnh:
   ```sql
   CREATE DATABASE pdfconverter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE pdfconverter;
   ```
2. Hoặc chạy file SQL có sẵn:
   ```bash
   mysql -u root -p < database/schema.sql
   ```

---

## CÀI ĐẶT MAVEN

### Bước 1: Tải Maven
1. Truy cập: https://maven.apache.org/download.cgi
2. Tải file Binary zip archive

### Bước 2: Giải nén
1. Giải nén vào thư mục (ví dụ: `C:\apache-maven-3.9.x`)
2. Ghi nhớ đường dẫn này

### Bước 3: Cấu hình biến môi trường
1. Tạo biến mới `MAVEN_HOME` trỏ đến thư mục Maven
2. Thêm `%MAVEN_HOME%\bin` vào biến `Path`

### Bước 4: Kiểm tra
1. Mở Command Prompt mới
2. Chạy lệnh:
   ```bash
   mvn -version
   ```

---

## CẤU HÌNH DỰ ÁN

### Bước 1: Cấu hình database
1. Mở file `src/main/webapp/WEB-INF/web.xml`
2. Tìm các thẻ `<context-param>` và cập nhật:
   ```xml
   <param-name>dbUrl</param-name>
   <param-value>jdbc:mysql://localhost:3306/pdfconverter?useSSL=false&amp;serverTimezone=UTC&amp;characterEncoding=UTF-8</param-value>
   
   <param-name>dbUsername</param-name>
   <param-value>root</param-value>
   
   <param-name>dbPassword</param-name>
   <param-value>YOUR_PASSWORD_HERE</param-value>
   ```
3. Thay `YOUR_PASSWORD_HERE` bằng mật khẩu MySQL của bạn

### Bước 2: Cấu hình thư mục upload
1. Trong file `web.xml`, tìm thẻ:
   ```xml
   <param-name>uploadDirectory</param-name>
   <param-value>C:/uploads/pdfconverter</param-value>
   ```
2. Thay đổi đường dẫn nếu cần (đảm bảo thư mục tồn tại hoặc có quyền tạo)

---

## BUILD VÀ DEPLOY

### Bước 1: Build project
1. Mở Command Prompt
2. Di chuyển đến thư mục dự án:
   ```bash
   cd C:\Users\viett\OneDrive\Desktop\WORK\VSCODE\JAVA\FinalProjectLTM
   ```
3. Chạy lệnh build:
   ```bash
   mvn clean package
   ```
4. File WAR sẽ được tạo tại: `target/PDFConverter.war`

### Bước 2: Deploy lên Tomcat
1. Dừng Tomcat nếu đang chạy
2. Copy file `target/PDFConverter.war` vào thư mục `%CATALINA_HOME%\webapps`
3. Khởi động lại Tomcat:
   ```bash
   cd %CATALINA_HOME%\bin
   startup.bat
   ```
4. Tomcat sẽ tự động giải nén và deploy ứng dụng

### Bước 3: Kiểm tra
1. Mở trình duyệt
2. Truy cập: http://localhost:8080/PDFConverter
3. Nếu thấy trang chào mừng thì deploy thành công

---

## HƯỚNG DẪN SỬ DỤNG

### 1. Đăng ký tài khoản
1. Truy cập: http://localhost:8080/PDFConverter
2. Click vào "Đăng ký ngay"
3. Điền thông tin:
   - Tên đăng nhập
   - Mật khẩu
   - Email
   - Họ và tên
4. Click "Đăng ký"
5. Sau khi đăng ký thành công, click "Đăng nhập"

### 2. Đăng nhập
1. Nhập tên đăng nhập và mật khẩu
2. Click "Đăng nhập"
3. Sau khi đăng nhập thành công, bạn sẽ vào Dashboard

### 3. Upload và chuyển đổi file PDF
1. Tại Dashboard, click "Chọn file" và chọn file PDF từ máy tính
2. Click "Upload và chuyển đổi"
3. Hệ thống sẽ hiển thị thông báo thành công
4. File sẽ được đưa vào hàng đợi để xử lý nền

### 4. Xem kết quả chuyển đổi
1. Click vào "Quản lý tác vụ" ở header
2. Bạn sẽ thấy danh sách tất cả các tác vụ chuyển đổi
3. Trạng thái sẽ tự động cập nhật:
   - **Đang chờ**: File đã được upload, chờ xử lý
   - **Đang xử lý**: Hệ thống đang chuyển đổi file
   - **Hoàn thành**: Chuyển đổi thành công
   - **Thất bại**: Có lỗi xảy ra trong quá trình chuyển đổi

### 5. Tải file
1. Tại trang "Quản lý tác vụ"
2. Click "Tải PDF" để tải file gốc
3. Khi trạng thái là "Hoàn thành", click "Tải DOC" để tải file đã chuyển đổi

### 6. Đăng xuất
- Click "Đăng xuất" ở header để thoát khỏi hệ thống

---

## XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi kết nối database
- Kiểm tra MySQL đã khởi động chưa
- Kiểm tra username/password trong `web.xml`
- Kiểm tra database `pdfconverter` đã được tạo chưa

### Lỗi upload file
- Kiểm tra quyền ghi vào thư mục upload
- Kiểm tra kích thước file (tối đa 50MB)
- Đảm bảo file là định dạng PDF

### Lỗi chuyển đổi
- Kiểm tra file PDF có bị lỗi không
- Kiểm tra log của Tomcat để xem chi tiết lỗi
- Đảm bảo có đủ dung lượng ổ cứng

### Port 8080 đã được sử dụng
- Thay đổi port trong file `%CATALINA_HOME%\conf\server.xml`
- Tìm dòng `<Connector port="8080"` và đổi sang port khác

---

## LIÊN HỆ HỖ TRỢ

Nếu gặp vấn đề trong quá trình cài đặt hoặc sử dụng, vui lòng kiểm tra:
- Log của Tomcat tại: `%CATALINA_HOME%\logs`
- Log của MySQL
- Console output khi chạy ứng dụng

