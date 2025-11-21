-- Database: pdfconverter
-- Tạo database
CREATE DATABASE IF NOT EXISTS pdfconverter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pdfconverter;

-- Bảng users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng conversion_tasks
CREATE TABLE IF NOT EXISTS conversion_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    original_file_path VARCHAR(500) NOT NULL,
    converted_file_name VARCHAR(255),
    converted_file_path VARCHAR(500),
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    error_message TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo index để tối ưu truy vấn
CREATE INDEX idx_user_id ON conversion_tasks(user_id);
CREATE INDEX idx_status ON conversion_tasks(status);
CREATE INDEX idx_created_at ON conversion_tasks(created_at);

-- Insert dữ liệu mẫu (tùy chọn)
-- Mật khẩu: admin123 (nên hash trong thực tế)
INSERT INTO users (username, password, email, full_name) VALUES
('admin', 'admin123', 'admin@example.com', 'Administrator'),
('user1', 'user123', 'user1@example.com', 'Người dùng 1');

