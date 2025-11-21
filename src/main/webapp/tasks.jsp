<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
      <% if (session.getAttribute("user")==null) { response.sendRedirect("login.jsp"); return; } %>
        <!DOCTYPE html>
        <html>

        <head>
          <meta charset="UTF-8">
          <title>Quản lý tác vụ - PDF to DOC Converter</title>
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
            }

            .btn {
              padding: 8px 20px;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: white;
              border: none;
              border-radius: 5px;
              cursor: pointer;
              text-decoration: none;
              display: inline-block;
              font-size: 14px;
            }

            .btn:hover {
              opacity: 0.9;
            }

            .btn-secondary {
              background: #6c757d;
            }

            .btn-success {
              background: #28a745;
            }

            .logout-btn {
              background: rgba(255, 255, 255, 0.2);
              border: 1px solid white;
            }

            .logout-btn:hover {
              background: rgba(255, 255, 255, 0.3);
            }

            table {
              width: 100%;
              border-collapse: collapse;
              margin-top: 20px;
            }

            th,
            td {
              padding: 12px;
              text-align: left;
              border-bottom: 1px solid #ddd;
            }

            th {
              background-color: #667eea;
              color: white;
            }

            tr:hover {
              background-color: #f5f5f5;
            }

            .status {
              padding: 5px 10px;
              border-radius: 5px;
              font-size: 12px;
              font-weight: bold;
            }

            .status-pending {
              background-color: #ffc107;
              color: #000;
            }

            .status-processing {
              background-color: #17a2b8;
              color: white;
            }

            .status-completed {
              background-color: #28a745;
              color: white;
            }

            .status-failed {
              background-color: #dc3545;
              color: white;
            }

            .message {
              padding: 15px;
              margin-bottom: 20px;
              border-radius: 5px;
            }

            .message-success {
              background-color: #d4edda;
              color: #155724;
              border: 1px solid #c3e6cb;
            }

            .message-error {
              background-color: #f8d7da;
              color: #721c24;
              border: 1px solid #f5c6cb;
            }
          </style>
        </head>

        <body>
          <div class="header">
            <h1>PDF to DOC Converter</h1>
            <div class="user-info">
              <span>Xin chào, ${user.fullName}</span>
              <a href="dashboard.jsp" class="btn logout-btn">Dashboard</a>
              <a href="logout" class="btn logout-btn">Đăng xuất</a>
            </div>
          </div>

          <div class="container">
            <div class="card">
              <h2>Danh sách tác vụ chuyển đổi</h2>

              <c:if test="${param.message == 'upload_success'}">
                <div class="message message-success">
                  File đã được upload thành công và đang được xử lý!
                </div>
              </c:if>
              <c:if test="${param.message == 'upload_failed'}">
                <div class="message message-error">
                  Upload thất bại. Vui lòng thử lại!
                </div>
              </c:if>
              <c:if test="${param.message == 'invalid_file'}">
                <div class="message message-error">
                  File không hợp lệ. Chỉ chấp nhận file PDF!
                </div>
              </c:if>

              <c:if test="${empty tasks}">
                <p style="margin-top: 20px; color: #666;">Bạn chưa có tác vụ nào. <a href="dashboard.jsp">Upload file
                    ngay</a></p>
              </c:if>

              <c:if test="${not empty tasks}">
                <table>
                  <thead>
                    <tr>
                      <th>STT</th>
                      <th>Tên file</th>
                      <th>Trạng thái</th>
                      <th>Thời gian tạo</th>
                      <th>Thời gian hoàn thành</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="task" items="${tasks}" varStatus="loop">
                      <tr>
                        <td>${loop.index + 1}</td>
                        <td>${task.originalFileName}</td>
                        <td>
                          <span class="status status-${task.status.name().toLowerCase()}">
                            <c:choose>
                              <c:when test="${task.status == 'PENDING'}">Đang chờ</c:when>
                              <c:when test="${task.status == 'PROCESSING'}">Đang xử lý</c:when>
                              <c:when test="${task.status == 'COMPLETED'}">Hoàn thành</c:when>
                              <c:when test="${task.status == 'FAILED'}">Thất bại</c:when>
                            </c:choose>
                          </span>
                        </td>
                        <td>
                          <fmt:formatDate value="${task.createdAt}" pattern="dd/MM/yyyy HH:mm:ss" />
                        </td>
                        <td>
                          <c:if test="${not empty task.completedAt}">
                            <fmt:formatDate value="${task.completedAt}" pattern="dd/MM/yyyy HH:mm:ss" />
                          </c:if>
                          <c:if test="${empty task.completedAt}">
                            -
                          </c:if>
                        </td>
                        <td>
                          <a href="download?taskId=${task.id}&type=original" class="btn btn-secondary">Tải PDF</a>
                          <c:if test="${task.status == 'COMPLETED'}">
                            <a href="download?taskId=${task.id}&type=converted" class="btn btn-success">Tải DOC</a>
                          </c:if>
                          <c:if test="${task.status == 'FAILED'}">
                            <span style="color: red; font-size: 12px;">${task.errorMessage}</span>
                          </c:if>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </c:if>
            </div>
          </div>

          <script>
            // Auto refresh every 5 seconds to update task status
            setTimeout(function () {
              location.reload();
            }, 5000);
          </script>
        </body>

        </html>