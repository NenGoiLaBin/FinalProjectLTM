package com.finalproject.controller;

import com.finalproject.dao.UserDAO;
import com.finalproject.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet {
  private UserDAO userDAO;

  @Override
  public void init() throws ServletException {
    userDAO = new UserDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("user") != null) {
      response.sendRedirect("dashboard.jsp");
    } else {
      request.getRequestDispatcher("login.jsp").forward(request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String username = request.getParameter("username");
    String password = request.getParameter("password");

    System.out.println("[LoginServlet] Nhận yêu cầu đăng nhập từ: " + username);

    if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
      request.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
      request.getRequestDispatcher("login.jsp").forward(request, response);
      return;
    }

    try {
      User user = userDAO.authenticate(username, password);

      if (user != null) {
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        System.out.println("[LoginServlet] Đăng nhập thành công, chuyển đến dashboard");
        response.sendRedirect("dashboard.jsp");
      } else {
        System.out.println("[LoginServlet] Đăng nhập thất bại");
        request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
        request.getRequestDispatcher("login.jsp").forward(request, response);
      }
    } catch (Exception e) {
      System.err.println("[LoginServlet] Lỗi khi xử lý đăng nhập: " + e.getMessage());
      e.printStackTrace();
      request.setAttribute("error", "Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại sau.");
      request.getRequestDispatcher("login.jsp").forward(request, response);
    }
  }
}
