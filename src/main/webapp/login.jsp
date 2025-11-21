<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <!DOCTYPE html>
  <html>

  <head>
    <meta charset="UTF-8">
    <title>Đăng nhập - PDF to DOC Converter</title>
    <style>
      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        font-family: Arial, sans-serif;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      .login-container {
        background: white;
        padding: 40px;
        border-radius: 10px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
        width: 400px;
      }

      h2 {
        text-align: center;
        margin-bottom: 30px;
        color: #333;
      }

      .form-group {
        margin-bottom: 20px;
      }

      label {
        display: block;
        margin-bottom: 5px;
        color: #555;
        font-weight: bold;
      }

      input[type="text"],
      input[type="password"] {
        width: 100%;
        padding: 12px;
        border: 1px solid #ddd;
        border-radius: 5px;
        font-size: 14px;
      }

      input[type="submit"] {
        width: 100%;
        padding: 12px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border: none;
        border-radius: 5px;
        font-size: 16px;
        cursor: pointer;
        font-weight: bold;
      }

      input[type="submit"]:hover {
        opacity: 0.9;
      }

      .error {
        color: red;
        text-align: center;
        margin-bottom: 15px;
        padding: 10px;
        background-color: #ffe6e6;
        border-radius: 5px;
      }

      .register-link {
        text-align: center;
        margin-top: 20px;
        color: #666;
      }

      .register-link a {
        color: #667eea;
        text-decoration: none;
      }
    </style>
  </head>

  <body>
    <div class="login-container">
      <h2>Đăng nhập</h2>
      <% if (request.getAttribute("error") !=null) { %>
        <div class="error">
          <%= request.getAttribute("error") %>
        </div>
        <% } %>
          <form action="login" method="post">
            <div class="form-group">
              <label for="username">Tên đăng nhập:</label>
              <input type="text" id="username" name="username" required>
            </div>
            <div class="form-group">
              <label for="password">Mật khẩu:</label>
              <input type="password" id="password" name="password" required>
            </div>
            <input type="submit" value="Đăng nhập">
          </form>
          <div class="register-link">
            <p>Chưa có tài khoản? <a href="register.jsp">Đăng ký ngay</a></p>
          </div>
    </div>
  </body>

  </html>