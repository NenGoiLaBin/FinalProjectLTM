# PDF TO DOC CONVERTER - Final Project

Hệ thống chuyển đổi file PDF sang DOC với xử lý nền sử dụng queue system.

## Tính năng chính

- ✅ Đăng ký và đăng nhập người dùng
- ✅ Upload file PDF
- ✅ Chuyển đổi PDF sang DOC tự động (xử lý nền)
- ✅ Xem danh sách tác vụ chuyển đổi
- ✅ Tải file PDF gốc và file DOC đã chuyển đổi
- ✅ Theo dõi trạng thái chuyển đổi real-time

## Kiến trúc

- **Mô hình**: MVC (Model-View-Controller)
- **Database**: MySQL
- **Server**: Apache Tomcat
- **Xử lý nền**: Queue system với worker thread
- **Thư viện**: Apache PDFBox, Apache POI

## Cấu trúc dự án

```
FinalProjectLTM/
├── src/main/java/com/finalproject/
│   ├── controller/     # Controllers (Servlets)
│   ├── model/         # Models (User, ConversionTask)
│   ├── dao/           # Data Access Objects
│   ├── service/       # Business Logic (Queue, Converter)
│   ├── config/        # Configuration
│   ├── util/          # Utilities
│   └── listener/       # Application Listener
├── src/main/webapp/
│   ├── WEB-INF/       # Web configuration
│   └── *.jsp          # Views
├── database/
│   └── schema.sql     # Database schema
├── pom.xml            # Maven dependencies
├── THIET_KE_MVC.md    # Tài liệu thiết kế MVC
└── HUONG_DAN_CAI_DAT.md # Hướng dẫn cài đặt
```

## Yêu cầu hệ thống

- Java JDK 8+
- Apache Tomcat 9.0+
- MySQL 8.0+
- Maven 3.6+

## Hướng dẫn nhanh

1. **Cài đặt database**:
   ```bash
   mysql -u root -p < database/schema.sql
   ```

2. **Cấu hình database** trong `src/main/webapp/WEB-INF/web.xml`

3. **Build project**:
   ```bash
   mvn clean package
   ```

4. **Deploy**:
   - Copy `target/PDFConverter.war` vào `%CATALINA_HOME%/webapps`
   - Khởi động Tomcat

5. **Truy cập**: 
   - Trên cùng máy: http://localhost:8080/PDFConverter
   - Từ máy khác: http://IP_SERVER:8080/PDFConverter
   - Xem hướng dẫn chi tiết: [HUONG_DAN_DEPLOY_2_MAY.md](HUONG_DAN_DEPLOY_2_MAY.md)

## Tài liệu chi tiết

- [Hướng dẫn cài đặt chi tiết](HUONG_DAN_CAI_DAT.md)
- [Thiết kế mô hình MVC](THIET_KE_MVC.md)
- [Hướng dẫn deploy server và client trên 2 máy khác nhau](HUONG_DAN_DEPLOY_2_MAY.md)

## Tác giả

Final Project - LTM

