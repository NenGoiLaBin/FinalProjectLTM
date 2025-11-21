package com.finalproject.listener;

import com.finalproject.config.DatabaseConfig;
import com.finalproject.service.ConversionQueue;
import com.finalproject.util.DatabaseConnection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ApplicationListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      System.out.println("[ApplicationListener] Đang khởi tạo kết nối database...");
      DatabaseConfig config = new DatabaseConfig(sce.getServletContext());
      DatabaseConnection.initialize(config);
      System.out.println("[ApplicationListener] Kết nối database đã được khởi tạo!");

      try {
        DatabaseConnection.getInstance().getConnection().close();
        System.out.println("[ApplicationListener] Kiểm tra kết nối database thành công!");
      } catch (Exception e) {
        System.err.println("[ApplicationListener] Lỗi khi kiểm tra kết nối database: " + e.getMessage());
        e.printStackTrace();
      }

      System.out.println("[ApplicationListener] Đang khởi động conversion queue...");
      ConversionQueue.getInstance().startWorker();
      System.out.println("[ApplicationListener] Conversion queue đã được khởi động!");

      System.out.println("[ApplicationListener] Ứng dụng đã được khởi tạo thành công!");
    } catch (Exception e) {
      System.err.println("[ApplicationListener] Lỗi khi khởi tạo ứng dụng: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ConversionQueue.getInstance().stopWorker();
    System.out.println("[ApplicationListener] Ứng dụng đã được dừng - Conversion queue đã dừng");
  }
}
