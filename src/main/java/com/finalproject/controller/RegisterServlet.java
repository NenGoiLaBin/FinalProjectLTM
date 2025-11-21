package com.finalproject.controller;

import com.finalproject.dao.UserDAO;
import com.finalproject.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {
  private UserDAO userDAO;

  @Override
  public void init() throws ServletException {
    userDAO = new UserDAO();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher("register.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String email = request.getParameter("email");
    String fullName = request.getParameter("fullName");

    User user = new User(username, password, email, fullName);

    if (userDAO.register(user)) {
      request.setAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
      request.getRequestDispatcher("register.jsp").forward(request, response);
    } else {
      request.setAttribute("error", "Đăng ký thất bại. Tên đăng nhập có thể đã tồn tại.");
      request.getRequestDispatcher("register.jsp").forward(request, response);
    }
  }
}
