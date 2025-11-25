package com.finalproject.service;

import com.finalproject.model.ConversionTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConversionQueue {
  private static ConversionQueue instance;
  private BlockingQueue<ConversionTask> queue;
  private ConversionWorker worker;
  private Thread workerThread;

  private ConversionQueue() {
    this.queue = new LinkedBlockingQueue<>();
  }

  public static synchronized ConversionQueue getInstance() {
    if (instance == null) {
      instance = new ConversionQueue();
    }
    return instance;
  }

  public void startWorker() {
    if (worker == null || !workerThread.isAlive()) {
      worker = new ConversionWorker(queue);
      workerThread = new Thread(worker);
      workerThread.setDaemon(true);
      workerThread.start();
    }
  }

  public void stopWorker() {
    if (worker != null) {
      worker.stop();
    }
  }

  public void addTask(ConversionTask task) {
    try {
      queue.put(task);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("[ConversionQueue] Error adding task to queue: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public int getQueueSize() {
    return queue.size();
  }
}
