package com.finalproject.controller;

import com.finalproject.dao.ConversionTaskDAO;
import com.finalproject.model.ConversionTask;
import com.finalproject.model.User;
import com.finalproject.service.ConversionQueue;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class UploadServlet extends HttpServlet {
  private static final int MAX_FILE_SIZE = 50 * 1024 * 1024;
  private static final int MAX_REQUEST_SIZE = 50 * 1024 * 1024;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("login.jsp");
      return;
    }

    User user = (User) session.getAttribute("user");
    String uploadDir = getServletContext().getInitParameter("uploadDirectory");

    File uploadDirectory = new File(uploadDir);
    if (!uploadDirectory.exists()) {
      uploadDirectory.mkdirs();
    }

    if (ServletFileUpload.isMultipartContent(request)) {
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setSizeThreshold(MAX_FILE_SIZE);
      factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setSizeMax(MAX_REQUEST_SIZE);

      try {
        List<FileItem> items = upload.parseRequest(request);
        for (FileItem item : items) {
          if (!item.isFormField() && item.getSize() > 0) {
            String fileName = item.getName();
            if (fileName.toLowerCase().endsWith(".pdf")) {
              String filePath = uploadDir + File.separator + System.currentTimeMillis() + "_" + fileName;
              File uploadedFile = new File(filePath);
              item.write(uploadedFile);

              ConversionTask task = new ConversionTask(
                  user.getId(),
                  fileName,
                  filePath);

              ConversionTaskDAO taskDAO = new ConversionTaskDAO();
              int taskId = taskDAO.createTask(task);

              if (taskId > 0) {
                task.setId(taskId);
                ConversionQueue.getInstance().addTask(task);

                response.sendRedirect("tasks?message=upload_success");
              } else {
                response.sendRedirect("tasks?message=upload_failed");
              }
              return;
            } else {
              response.sendRedirect("tasks?message=invalid_file");
              return;
            }
          }
        }
      } catch (Exception e) {
        System.err.println("[UploadServlet] Error uploading file: " + e.getMessage());
        e.printStackTrace();
        response.sendRedirect("tasks?message=upload_error");
      }
    } else {
      response.sendRedirect("tasks?message=no_file");
    }
  }
}
