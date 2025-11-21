<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <% if (session.getAttribute("user")==null) { response.sendRedirect("login.jsp"); return; } %>
      <!DOCTYPE html>
      <html>

      <head>
        <meta charset="UTF-8">
        <title>Dashboard - PDF to DOC Converter</title>
        <style>
          * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
          }

          body {
            font-family: Arial, sans-serif;
            background-color: #f5f5f5;
          }

          .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
          }

          .header h1 {
            font-size: 24px;
          }

          .user-info {
            display: flex;
            align-items: center;
            gap: 20px;
          }

          .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
          }

          .card {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            margin-bottom: 30px;
          }

          .card h2 {
            margin-bottom: 20px;
            color: #333;
          }

          .upload-area {
            border: 2px dashed #667eea;
            border-radius: 10px;
            padding: 40px;
            text-align: center;
            background-color: #f9f9f9;
          }

          .upload-area.dragover {
            background-color: #e6e6ff;
          }

          input[type="file"] {
            margin: 20px 0;
          }

          .btn {
            padding: 12px 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            text-decoration: none;
            display: inline-block;
          }

          .btn:hover {
            opacity: 0.9;
          }

          .btn-secondary {
            background: #6c757d;
          }

          .logout-btn {
            background: rgba(255, 255, 255, 0.2);
            border: 1px solid white;
          }

          .logout-btn:hover {
            background: rgba(255, 255, 255, 0.3);
          }
        </style>
      </head>

      <body>
        <div class="header">
          <h1>PDF to DOC Converter</h1>
          <div class="user-info">
            <span>Xin chào, ${user.fullName}</span>
            <a href="tasks" class="btn logout-btn">Quản lý tác vụ</a>
            <a href="logout" class="btn logout-btn">Đăng xuất</a>
          </div>
        </div>

        <div class="container">
          <div class="card">
            <h2>Upload file PDF</h2>
            <form action="upload" method="post" enctype="multipart/form-data">
              <div class="upload-area">
                <p>Chọn file PDF để chuyển đổi sang DOC</p>
                <input type="file" name="file" accept=".pdf" required>
                <br>
                <button type="submit" class="btn">Upload và chuyển đổi</button>
              </div>
            </form>
          </div>

          <div class="card">
            <h2>Hướng dẫn sử dụng</h2>
            <ol style="line-height: 2;">
              <li>Chọn file PDF từ máy tính của bạn</li>
              <li>Nhấn nút "Upload và chuyển đổi"</li>
              <li>Hệ thống sẽ xử lý file trong nền</li>
              <li>Vào mục "Quản lý tác vụ" để xem kết quả</li>
              <li>Tải file DOC đã chuyển đổi về máy</li>
            </ol>
          </div>
        </div>
      </body>

      </html>