@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ========================================
REM    SCRIPT DEPLOY ỨNG DỤNG PDF CONVERTER
REM ========================================

echo.
echo ========================================
echo    DEPLOY PDF CONVERTER APPLICATION
echo ========================================
echo.

REM Cấu hình đường dẫn
set PROJECT_DIR=%~dp0
set TOMCAT_HOME=C:\Users\viett\Downloads\apache-tomcat-9.0.107
set WAR_FILE=%PROJECT_DIR%target\PDFConverter.war
set TOMCAT_WEBAPPS=%TOMCAT_HOME%\webapps
set TOMCAT_BIN=%TOMCAT_HOME%\bin

REM Chuyển đến thư mục dự án
cd /d "%PROJECT_DIR%"

echo [INFO] Thư mục dự án: %PROJECT_DIR%
echo [INFO] Thư mục Tomcat: %TOMCAT_HOME%
echo.

REM ========================================
REM    BƯỚC 1: BUILD PROJECT
REM ========================================
echo [BƯỚC 1] Kiểm tra và build project...

if not exist "%WAR_FILE%" (
    echo [INFO] File WAR không tồn tại, đang build project...
    call mvn clean package -DskipTests
    if !ERRORLEVEL! NEQ 0 (
        echo [ERROR] Build thất bại! Vui lòng kiểm tra lại.
        pause
        exit /b 1
    )
    echo [SUCCESS] Build thành công!
) else (
    echo [INFO] File WAR đã tồn tại: %WAR_FILE%
)

if not exist "%WAR_FILE%" (
    echo [ERROR] File WAR không được tạo sau khi build!
    pause
    exit /b 1
)

echo.

REM ========================================
REM    BƯỚC 2: DỪNG TOMCAT
REM ========================================
echo [BƯỚC 2] Dừng Tomcat server...

if exist "%TOMCAT_BIN%\shutdown.bat" (
    cd /d "%TOMCAT_BIN%"
    call shutdown.bat >nul 2>&1
    timeout /t 3 /nobreak >nul
    echo [SUCCESS] Đã gửi lệnh dừng Tomcat
) else (
    echo [WARNING] Không tìm thấy shutdown.bat, có thể Tomcat không được cài đặt đúng
)

echo.

REM ========================================
REM    BƯỚC 3: XÓA ỨNG DỤNG CŨ
REM ========================================
echo [BƯỚC 3] Xóa ứng dụng cũ...

if exist "%TOMCAT_WEBAPPS%\PDFConverter.war" (
    del /F /Q "%TOMCAT_WEBAPPS%\PDFConverter.war"
    echo [INFO] Đã xóa file WAR cũ
)

if exist "%TOMCAT_WEBAPPS%\PDFConverter" (
    rmdir /S /Q "%TOMCAT_WEBAPPS%\PDFConverter"
    echo [INFO] Đã xóa thư mục ứng dụng cũ
)

echo [SUCCESS] Đã dọn dẹp ứng dụng cũ
echo.

REM ========================================
REM    BƯỚC 4: COPY FILE WAR MỚI
REM ========================================
echo [BƯỚC 4] Copy file WAR mới...

if not exist "%TOMCAT_WEBAPPS%" (
    echo [ERROR] Thư mục webapps không tồn tại: %TOMCAT_WEBAPPS%
    echo [ERROR] Vui lòng kiểm tra đường dẫn Tomcat
    pause
    exit /b 1
)

copy /Y "%WAR_FILE%" "%TOMCAT_WEBAPPS%\PDFConverter.war" >nul
if !ERRORLEVEL! NEQ 0 (
    echo [ERROR] Copy file WAR thất bại!
    echo [ERROR] Kiểm tra:
    echo    - File WAR có tồn tại: %WAR_FILE%
    echo    - Thư mục webapps có quyền ghi: %TOMCAT_WEBAPPS%
    echo    - Đường dẫn Tomcat có đúng không
    pause
    exit /b 1
)

echo [SUCCESS] Đã copy file WAR mới
echo.

REM ========================================
REM    BƯỚC 5: KHỞI ĐỘNG TOMCAT
REM ========================================
echo [BƯỚC 5] Khởi động Tomcat server...

if not exist "%TOMCAT_BIN%\startup.bat" (
    echo [ERROR] Không tìm thấy startup.bat: %TOMCAT_BIN%\startup.bat
    echo [ERROR] Vui lòng kiểm tra đường dẫn Tomcat
    pause
    exit /b 1
)

cd /d "%TOMCAT_BIN%"
start "Tomcat Server" cmd /k startup.bat

echo [SUCCESS] Đã khởi động Tomcat
echo.

REM ========================================
REM    HOÀN TẤT
REM ========================================
echo ========================================
echo    DEPLOY THÀNH CÔNG!
echo ========================================
echo.
echo [INFO] Vui lòng đợi 10-15 giây để Tomcat deploy ứng dụng...
echo.
echo [INFO] Sau đó truy cập:
echo    - Local: http://localhost:8080/PDFConverter
echo    - Network: http://[IP_SERVER]:8080/PDFConverter
echo.
echo [INFO] Để xem log, mở cửa sổ Tomcat Server vừa mở
echo.
pause

endlocal
