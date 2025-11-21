package com.finalproject.dao;

import com.finalproject.model.ConversionTask;
import com.finalproject.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConversionTaskDAO {

  public int createTask(ConversionTask task) {
    String sql = "INSERT INTO conversion_tasks (user_id, original_file_name, original_file_path, status, created_at) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

      stmt.setInt(1, task.getUserId());
      stmt.setString(2, task.getOriginalFileName());
      stmt.setString(3, task.getOriginalFilePath());
      stmt.setString(4, task.getStatus().name());
      stmt.setTimestamp(5, new Timestamp(task.getCreatedAt().getTime()));

      int affectedRows = stmt.executeUpdate();
      if (affectedRows > 0) {
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      System.err.println("[ConversionTaskDAO] Lỗi khi tạo task: " + e.getMessage());
      e.printStackTrace();
    }
    return -1;
  }

  public List<ConversionTask> getTasksByUserId(int userId) {
    List<ConversionTask> tasks = new ArrayList<>();
    String sql = "SELECT * FROM conversion_tasks WHERE user_id = ? ORDER BY created_at DESC";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        ConversionTask task = mapResultSetToTask(rs);
        tasks.add(task);
      }
    } catch (SQLException e) {
      System.err.println("[ConversionTaskDAO] Lỗi khi lấy danh sách task: " + e.getMessage());
      e.printStackTrace();
    }
    return tasks;
  }

  public ConversionTask getTaskById(int taskId) {
    String sql = "SELECT * FROM conversion_tasks WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, taskId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return mapResultSetToTask(rs);
      }
    } catch (SQLException e) {
      System.err.println("[ConversionTaskDAO] Lỗi khi lấy task theo ID: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public List<ConversionTask> getPendingTasks() {
    List<ConversionTask> tasks = new ArrayList<>();
    String sql = "SELECT * FROM conversion_tasks WHERE status = 'PENDING' ORDER BY created_at ASC";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        ConversionTask task = mapResultSetToTask(rs);
        tasks.add(task);
      }
    } catch (SQLException e) {
      System.err.println("[ConversionTaskDAO] Lỗi khi lấy danh sách task đang chờ: " + e.getMessage());
      e.printStackTrace();
    }
    return tasks;
  }

  public boolean updateTask(ConversionTask task) {
    String sql = "UPDATE conversion_tasks SET status = ?, converted_file_name = ?, converted_file_path = ?, completed_at = ?, error_message = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, task.getStatus().name());
      stmt.setString(2, task.getConvertedFileName());
      stmt.setString(3, task.getConvertedFilePath());
      if (task.getCompletedAt() != null) {
        stmt.setTimestamp(4, new Timestamp(task.getCompletedAt().getTime()));
      } else {
        stmt.setTimestamp(4, null);
      }
      stmt.setString(5, task.getErrorMessage());
      stmt.setInt(6, task.getId());

      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      System.err.println("[ConversionTaskDAO] Lỗi khi cập nhật task: " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  private ConversionTask mapResultSetToTask(ResultSet rs) throws SQLException {
    ConversionTask task = new ConversionTask();
    task.setId(rs.getInt("id"));
    task.setUserId(rs.getInt("user_id"));
    task.setOriginalFileName(rs.getString("original_file_name"));
    task.setOriginalFilePath(rs.getString("original_file_path"));
    task.setConvertedFileName(rs.getString("converted_file_name"));
    task.setConvertedFilePath(rs.getString("converted_file_path"));
    task.setStatus(ConversionTask.TaskStatus.valueOf(rs.getString("status")));
    task.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
    Timestamp completedAt = rs.getTimestamp("completed_at");
    if (completedAt != null) {
      task.setCompletedAt(new Date(completedAt.getTime()));
    }
    task.setErrorMessage(rs.getString("error_message"));
    return task;
  }
}
