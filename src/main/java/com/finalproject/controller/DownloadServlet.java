package com.finalproject.controller;

import com.finalproject.dao.ConversionTaskDAO;
import com.finalproject.model.ConversionTask;
import com.finalproject.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DownloadServlet extends HttpServlet {
  private ConversionTaskDAO taskDAO;

  @Override
  public void init() throws ServletException {
    taskDAO = new ConversionTaskDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("login.jsp");
      return;
    }

    User user = (User) session.getAttribute("user");
    String taskIdParam = request.getParameter("taskId");
    String type = request.getParameter("type");

    if (taskIdParam == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu ID tác vụ");
      return;
    }

    try {
      int taskId = Integer.parseInt(taskIdParam);
      ConversionTask task = taskDAO.getTaskById(taskId);

      if (task == null || task.getUserId() != user.getId()) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập");
        return;
      }

      String filePath;
      String fileName;

      if ("original".equals(type)) {
        filePath = task.getOriginalFilePath();
        fileName = task.getOriginalFileName();
      } else {
        if (task.getStatus() != ConversionTask.TaskStatus.COMPLETED) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File chưa sẵn sàng");
          return;
        }
        filePath = task.getConvertedFilePath();
        fileName = task.getConvertedFileName();
      }

      File file = new File(filePath);
      if (!file.exists()) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy file");
        return;
      }

      response.setContentType("application/octet-stream");
      response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
      response.setContentLength((int) file.length());

      FileInputStream in = new FileInputStream(file);
      OutputStream out = response.getOutputStream();

      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }

      in.close();
      out.flush();

    } catch (NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID tác vụ không hợp lệ");
      System.err.println("[DownloadServlet] Lỗi khi parse task ID: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("[DownloadServlet] Lỗi khi tải file: " + e.getMessage());
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tải file");
    }
  }
}
