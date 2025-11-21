package com.finalproject.model;

import java.util.Date;

public class ConversionTask {
  public enum TaskStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
  }

  private int id;
  private int userId;
  private String originalFileName;
  private String originalFilePath;
  private String convertedFileName;
  private String convertedFilePath;
  private TaskStatus status;
  private Date createdAt;
  private Date completedAt;
  private String errorMessage;

  public ConversionTask() {
    this.status = TaskStatus.PENDING;
    this.createdAt = new Date();
  }

  public ConversionTask(int userId, String originalFileName, String originalFilePath) {
    this();
    this.userId = userId;
    this.originalFileName = originalFileName;
    this.originalFilePath = originalFilePath;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  public String getOriginalFilePath() {
    return originalFilePath;
  }

  public void setOriginalFilePath(String originalFilePath) {
    this.originalFilePath = originalFilePath;
  }

  public String getConvertedFileName() {
    return convertedFileName;
  }

  public void setConvertedFileName(String convertedFileName) {
    this.convertedFileName = convertedFileName;
  }

  public String getConvertedFilePath() {
    return convertedFilePath;
  }

  public void setConvertedFilePath(String convertedFilePath) {
    this.convertedFilePath = convertedFilePath;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Date completedAt) {
    this.completedAt = completedAt;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
