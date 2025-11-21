# HƯỚNG DẪN DEPLOY SERVER VÀ CLIENT TRÊN 2 MÁY KHÁC NHAU

## TỔNG QUAN KIẾN TRÚC

```
┌─────────────────┐                    ┌─────────────────┐
│   MÁY CLIENT    │                    │   MÁY SERVER    │
│   (Browser)     │  HTTP Request      │   (Tomcat)      │
│                 │ ──────────────────>│                 │
│                 │  HTTP Response     │                 │
│                 │ <──────────────────│                 │
└─────────────────┘                    └─────────────────┘
                                               │
                                               ▼
                                       ┌─────────────────┐
                                       │   MÁY DATABASE  │
                                       │   (MySQL)       │
                                       └─────────────────┘
```

## YÊU CẦU

### Máy Server:
- Java JDK 8+
- Apache Tomcat 9.0+
- MySQL 8.0+ (có thể trên cùng máy hoặc máy khác)
- Maven 3.6+ (để build)

### Máy Client:
- Chỉ cần trình duyệt web (Chrome, Firefox, Edge...)
- Kết nối mạng đến máy Server

---

## BƯỚC 1: CẤU HÌNH MÁY SERVER

### 1.1. Cấu hình Tomcat để lắng nghe trên tất cả interfaces

1. Mở file: `%CATALINA_HOME%\conf\server.xml`
2. Tìm dòng:
   ```xml
   <Connector port="8080" protocol="HTTP/1.1"
              connectionTimeout="20000"
              redirectPort="8443" />
   ```
3. Thêm thuộc tính `address="0.0.0.0"`:
   ```xml
   <Connector port="8080" protocol="HTTP/1.1"
              address="0.0.0.0"
              connectionTimeout="20000"
              redirectPort="8443" />
   ```
4. Lưu file và khởi động lại Tomcat

**Giải thích**: 
- `address="0.0.0.0"` cho phép Tomcat lắng nghe trên tất cả network interfaces
- Mặc định Tomcat chỉ lắng nghe trên `localhost` (127.0.0.1)

### 1.2. Kiểm tra IP của máy Server

**Windows:**
```bash
ipconfig
```
Tìm `IPv4 Address` (ví dụ: `192.168.1.100`)

**Linux/Mac:**
```bash
ifconfig
# hoặc
ip addr
```

Ghi nhớ IP này để client kết nối.

### 1.3. Cấu hình Firewall

**Windows Firewall:**
1. Mở **Windows Defender Firewall** → **Advanced Settings**
2. Click **Inbound Rules** → **New Rule**
3. Chọn **Port** → Next
4. Chọn **TCP**, nhập port `8080` → Next
5. Chọn **Allow the connection** → Next
6. Chọn tất cả profiles → Next
7. Đặt tên: "Tomcat 8080" → Finish

**Linux (iptables):**
```bash
sudo ufw allow 8080/tcp
# hoặc
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### 1.4. Cấu hình Database Connection

**Nếu MySQL trên cùng máy Server:**
- Giữ nguyên `localhost` trong `web.xml`

**Nếu MySQL trên máy khác:**
1. Mở file: `src/main/webapp/WEB-INF/web.xml`
2. Thay đổi:
   ```xml
   <param-value>jdbc:mysql://localhost:3306/pdfconverter</param-value>
   ```
   Thành:
   ```xml
   <param-value>jdbc:mysql://IP_DATABASE:3306/pdfconverter?useSSL=false&amp;serverTimezone=UTC&amp;characterEncoding=UTF-8</param-value>
   ```
   (Thay `IP_DATABASE` bằng IP của máy chứa MySQL)

3. Đảm bảo MySQL cho phép remote connection:
   ```sql
   -- Trên MySQL server
   CREATE USER 'root'@'%' IDENTIFIED BY 'password';
   GRANT ALL PRIVILEGES ON pdfconverter.* TO 'root'@'%';
   FLUSH PRIVILEGES;
   ```

### 1.5. Build và Deploy ứng dụng

1. Build project:
   ```bash
   mvn clean package
   ```

2. Copy file `target/PDFConverter.war` vào `%CATALINA_HOME%\webapps`

3. Khởi động Tomcat:
   ```bash
   cd %CATALINA_HOME%\bin
   startup.bat
   ```

4. Kiểm tra server đang chạy:
   - Mở browser trên máy Server
   - Truy cập: `http://localhost:8080/PDFConverter`
   - Nếu thấy trang web thì server đã sẵn sàng

---

## BƯỚC 2: CẤU HÌNH MÁY CLIENT

### 2.1. Kết nối đến Server

1. Mở trình duyệt web
2. Truy cập: `http://IP_SERVER:8080/PDFConverter`
   - Thay `IP_SERVER` bằng IP của máy Server (ví dụ: `192.168.1.100`)
   - Ví dụ: `http://192.168.1.100:8080/PDFConverter`

### 2.2. Kiểm tra kết nối

- Nếu thấy trang đăng nhập → Kết nối thành công!
- Nếu không kết nối được, kiểm tra:
  - Firewall trên máy Server
  - IP address có đúng không
  - Tomcat có đang chạy không
  - Port 8080 có bị chặn không

---

## BƯỚC 3: CÁC TÌNH HUỐNG DEPLOY

### Tình huống 1: Server và Database trên cùng máy

```
┌─────────────────────────────────┐
│         MÁY SERVER              │
│  ┌──────────┐  ┌──────────┐   │
│  │  Tomcat  │  │  MySQL   │   │
│  │  :8080   │  │  :3306   │   │
│  └──────────┘  └──────────┘   │
└─────────────────────────────────┘
         ▲
         │ HTTP
         │
┌────────┴────────┐
│  MÁY CLIENT     │
│  (Browser)      │
└─────────────────┘
```

**Cấu hình:**
- `web.xml`: `jdbc:mysql://localhost:3306/pdfconverter`
- Client truy cập: `http://IP_SERVER:8080/PDFConverter`

### Tình huống 2: Server, Database, Client trên 3 máy khác nhau

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   CLIENT    │      │   SERVER    │      │  DATABASE   │
│  (Browser)  │─────>│   (Tomcat)  │─────>│   (MySQL)   │
└─────────────┘      └─────────────┘      └─────────────┘
```

**Cấu hình:**
- `web.xml`: `jdbc:mysql://IP_DATABASE:3306/pdfconverter`
- Client truy cập: `http://IP_SERVER:8080/PDFConverter`

### Tình huống 3: Deploy trên Internet (Cloud Server)

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   CLIENT    │      │  CLOUD       │      │  DATABASE   │
│  (Browser)  │─────>│  SERVER      │─────>│   (MySQL)   │
│             │      │  (Public IP) │      │             │
└─────────────┘      └─────────────┘      └─────────────┘
```

**Cấu hình:**
- `web.xml`: `jdbc:mysql://IP_DATABASE:3306/pdfconverter`
- Client truy cập: `http://PUBLIC_IP:8080/PDFConverter`
- **Lưu ý**: Cần cấu hình Security Group/Firewall trên cloud để mở port 8080

---

## BƯỚC 4: KIỂM TRA VÀ XỬ LÝ LỖI

### Kiểm tra Server đang lắng nghe

**Windows:**
```bash
netstat -an | findstr 8080
```
Kết quả mong đợi:
```
TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING
```

**Linux:**
```bash
netstat -tuln | grep 8080
# hoặc
ss -tuln | grep 8080
```

### Lỗi thường gặp

#### 1. "Connection refused" hoặc "Không thể kết nối"
**Nguyên nhân:**
- Firewall chặn port 8080
- Tomcat chỉ lắng nghe trên localhost
- IP address sai

**Giải pháp:**
- Kiểm tra firewall
- Thêm `address="0.0.0.0"` vào `server.xml`
- Kiểm tra lại IP address

#### 2. "This site can't be reached"
**Nguyên nhân:**
- Server không chạy
- Port bị chặn bởi firewall
- Network không kết nối

**Giải pháp:**
- Kiểm tra Tomcat có đang chạy không
- Ping từ client đến server: `ping IP_SERVER`
- Kiểm tra firewall

#### 3. "Database connection failed"
**Nguyên nhân:**
- MySQL không cho phép remote connection
- IP database sai trong `web.xml`
- Firewall chặn port 3306

**Giải pháp:**
- Cấu hình MySQL cho phép remote access
- Kiểm tra IP database
- Mở port 3306 trên firewall (nếu database ở máy khác)

---

## BƯỚC 5: BẢO MẬT (QUAN TRỌNG)

### 5.1. Thay đổi port mặc định

Thay vì dùng port 8080 (dễ bị tấn công), nên đổi sang port khác:

1. Sửa `server.xml`:
   ```xml
   <Connector port="9090" protocol="HTTP/1.1"
              address="0.0.0.0"
              connectionTimeout="20000"
              redirectPort="8443" />
   ```

2. Client truy cập: `http://IP_SERVER:9090/PDFConverter`

### 5.2. Sử dụng HTTPS (SSL/TLS)

Để bảo mật hơn, nên cấu hình HTTPS:
- Tạo SSL certificate
- Cấu hình Tomcat với HTTPS
- Client truy cập: `https://IP_SERVER:8443/PDFConverter`

### 5.3. Giới hạn IP truy cập

Có thể giới hạn chỉ một số IP được truy cập server (cấu hình trong firewall hoặc reverse proxy).

---

## TÓM TẮT

✅ **Server và Client có thể chạy trên 2 máy khác nhau**

**Các bước chính:**
1. Cấu hình Tomcat: `address="0.0.0.0"` trong `server.xml`
2. Mở port 8080 trên firewall
3. Client truy cập: `http://IP_SERVER:8080/PDFConverter`
4. Nếu database ở máy khác: cấu hình `web.xml` với IP database

**Lưu ý:**
- Đảm bảo cả 2 máy cùng mạng (LAN) hoặc có thể kết nối Internet
- Kiểm tra firewall trên cả 2 máy
- Ghi nhớ IP của máy Server để client kết nối

---

## VÍ DỤ THỰC TẾ

**Máy Server:**
- IP: `192.168.1.100`
- Tomcat: Port 8080
- MySQL: Port 3306 (cùng máy)

**Máy Client:**
- IP: `192.168.1.50`
- Browser: Chrome

**Cách truy cập:**
1. Mở Chrome trên máy Client
2. Nhập: `http://192.168.1.100:8080/PDFConverter`
3. Sử dụng ứng dụng bình thường!

