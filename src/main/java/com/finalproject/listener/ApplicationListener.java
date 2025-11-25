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
      System.out.println("[ApplicationListener] Initializing database connection...");
      DatabaseConfig config = new DatabaseConfig(sce.getServletContext());
      DatabaseConnection.initialize(config);
      System.out.println("[ApplicationListener] Database connection initialized!");

      try {
        DatabaseConnection.getInstance().getConnection().close();
        System.out.println("[ApplicationListener] Database connection test successful!");
      } catch (Exception e) {
        System.err.println("[ApplicationListener] Error testing database connection: " + e.getMessage());
        e.printStackTrace();
      }

      System.out.println("[ApplicationListener] Starting conversion queue...");
      ConversionQueue.getInstance().startWorker();
      System.out.println("[ApplicationListener] Conversion queue started!");

      System.out.println("[ApplicationListener] Application initialized successfully!");
    } catch (Exception e) {
      System.err.println("[ApplicationListener] Error initializing application: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ConversionQueue.getInstance().stopWorker();
    System.out.println("[ApplicationListener] Application stopped - Conversion queue stopped");
  }
}
