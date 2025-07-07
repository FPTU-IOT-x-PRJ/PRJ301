// src/java/dao/QuizDAO.java
package dao;

import dal.DBContext;
import entity.Quiz;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuizDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(QuizDAO.class.getName());

    // --- SQL Constants ---
    private static final String INSERT_QUIZ_SQL = "INSERT INTO Quizzes (lessonId, title, description, createdAt, updatedAt) VALUES (?, ?, ?, GETDATE(), GETDATE())";
    private static final String SELECT_QUIZ_BY_ID_SQL = "SELECT * FROM Quizzes WHERE id = ?";
    private static final String SELECT_QUIZZES_BY_LESSON_ID_SQL = "SELECT * FROM Quizzes WHERE lessonId = ? ORDER BY createdAt DESC";
    private static final String UPDATE_QUIZ_SQL = "UPDATE Quizzes SET title = ?, description = ?, updatedAt = GETDATE() WHERE id = ?";
    private static final String DELETE_QUIZ_SQL = "DELETE FROM Quizzes WHERE id = ?";

    public int addQuiz(Quiz quiz) {
        int generatedId = -1;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_QUIZ_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, quiz.getLessonId());
            ps.setString(2, quiz.getTitle());
            ps.setString(3, quiz.getDescription());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        quiz.setId(generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return generatedId;
    }

    public Quiz getQuizById(int id) {
        Quiz quiz = null;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_QUIZ_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quiz = extractQuizFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return quiz;
    }

    public List<Quiz> getQuizzesByLessonId(int lessonId) {
        List<Quiz> quizzes = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_QUIZZES_BY_LESSON_ID_SQL)) {
            ps.setInt(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(extractQuizFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return quizzes;
    }

    public boolean updateQuiz(Quiz quiz) {
        boolean rowUpdated = false;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_QUIZ_SQL)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setInt(3, quiz.getId());
            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowUpdated;
    }
    
    // Khi xóa Quiz, cần xóa các Question và AnswerOption liên quan trước (sử dụng ON DELETE CASCADE trong DB hoặc xóa thủ công)
    public boolean deleteQuiz(int id) {
        boolean rowDeleted = false;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(DELETE_QUIZ_SQL)) {
            ps.setInt(1, id);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowDeleted;
    }


    private Quiz extractQuizFromResultSet(ResultSet rs) throws SQLException {
        return new Quiz(
                rs.getInt("id"),
                rs.getInt("lessonId"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("createdAt").toLocalDateTime(),
                rs.getTimestamp("updatedAt").toLocalDateTime()
        );
    }
    
    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                LOGGER.log(Level.SEVERE, "SQLState: " + ((SQLException) e).getSQLState());
                LOGGER.log(Level.SEVERE, "Error Code: " + ((SQLException) e).getErrorCode());
                LOGGER.log(Level.SEVERE, "Message: " + e.getMessage());
                Throwable t = e.getCause();
                while (t != null) {
                    LOGGER.log(Level.SEVERE, "Cause: " + t.getMessage(), t);
                    t = t.getCause();
                }
            }
        }
    }
}