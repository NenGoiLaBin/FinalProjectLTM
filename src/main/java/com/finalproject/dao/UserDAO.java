package com.finalproject.dao;

import com.finalproject.model.User;
import com.finalproject.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

  public User authenticate(String username, String password) {
    if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
      System.out.println("[UserDAO] Username hoặc password rỗng!");
      return null;
    }

    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username.trim());
      stmt.setString(2, password);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        System.out.println("[UserDAO] Đăng nhập thành công: " + username);
        return user;
      } else {
        System.out.println("[UserDAO] Không tìm thấy user: " + username);
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO] Lỗi khi đăng nhập: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("[UserDAO] Lỗi khác: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public User getUserById(int id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        return user;
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO] Lỗi khi lấy user theo ID: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public boolean register(User user) {
    String sql = "INSERT INTO users (username, password, email, full_name) VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, user.getUsername());
      stmt.setString(2, user.getPassword());
      stmt.setString(3, user.getEmail());
      stmt.setString(4, user.getFullName());

      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("[UserDAO] Lỗi khi đăng ký user: " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }
}
