package com.finalproject.service;

import com.finalproject.dao.ConversionTaskDAO;
import com.finalproject.model.ConversionTask;
import java.util.concurrent.BlockingQueue;

public class ConversionWorker implements Runnable {
  private BlockingQueue<ConversionTask> queue;
  private volatile boolean running = true;
  private PDFToDOCConverter converter;
  private ConversionTaskDAO taskDAO;

  public ConversionWorker(BlockingQueue<ConversionTask> queue) {
    this.queue = queue;
    this.converter = new PDFToDOCConverter();
    this.taskDAO = new ConversionTaskDAO();
  }

  @Override
  public void run() {
    while (running) {
      try {
        ConversionTask task = queue.take();
        processTask(task);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private void processTask(ConversionTask task) {
    try {
      task.setStatus(ConversionTask.TaskStatus.PROCESSING);
      taskDAO.updateTask(task);

      String convertedFilePath = converter.convert(task.getOriginalFilePath());

      if (convertedFilePath != null) {
        String convertedFileName = convertedFilePath.substring(
            convertedFilePath.lastIndexOf("\\") + 1);

        task.setConvertedFileName(convertedFileName);
        task.setConvertedFilePath(convertedFilePath);
        task.setStatus(ConversionTask.TaskStatus.COMPLETED);
        task.setCompletedAt(new java.util.Date());
      } else {
        task.setStatus(ConversionTask.TaskStatus.FAILED);
        task.setErrorMessage("Chuyển đổi thất bại");
        task.setCompletedAt(new java.util.Date());
      }

      taskDAO.updateTask(task);

    } catch (Exception e) {
      task.setStatus(ConversionTask.TaskStatus.FAILED);
      task.setErrorMessage("Lỗi: " + e.getMessage());
      task.setCompletedAt(new java.util.Date());
      taskDAO.updateTask(task);
      System.err.println("[ConversionWorker] Error processing task " + task.getId() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void stop() {
    running = false;
  }
}
