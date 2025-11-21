package com.finalproject.controller;

import com.finalproject.dao.ConversionTaskDAO;
import com.finalproject.model.ConversionTask;
import com.finalproject.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class TaskServlet extends HttpServlet {
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
    List<ConversionTask> tasks = taskDAO.getTasksByUserId(user.getId());

    request.setAttribute("tasks", tasks);
    request.getRequestDispatcher("tasks.jsp").forward(request, response);
  }
}
