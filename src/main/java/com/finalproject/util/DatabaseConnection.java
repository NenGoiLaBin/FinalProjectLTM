package com.finalproject.util;

import com.finalproject.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static DatabaseConnection instance;
  private DatabaseConfig config;

  private DatabaseConnection(DatabaseConfig config) {
    this.config = config;
    try {
      Class.forName(config.getDriver());
    } catch (ClassNotFoundException e) {
      System.err.println("[DatabaseConnection] Lỗi khi load JDBC driver: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void initialize(DatabaseConfig config) {
    if (instance == null) {
      instance = new DatabaseConnection(config);
    }
  }

  public static DatabaseConnection getInstance() {
    if (instance == null) {
      throw new IllegalStateException(
          "DatabaseConnection chưa được khởi tạo! Kiểm tra ApplicationListener có được cấu hình trong web.xml không.");
    }
    return instance;
  }

  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
        config.getUrl(),
        config.getUsername(),
        config.getPassword());
  }
}
