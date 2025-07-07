// src/java/dao/QuestionDAO.java
package dao;

import dal.DBContext;
import entity.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(QuestionDAO.class.getName());

    private static final String INSERT_QUESTION_SQL = "INSERT INTO Questions (quizId, questionText, questionType, createdAt, updatedAt) VALUES (?, ?, ?, GETDATE(), GETDATE())";
    private static final String SELECT_QUESTIONS_BY_QUIZ_ID_SQL = "SELECT * FROM Questions WHERE quizId = ? ORDER BY createdAt ASC";
    private static final String DELETE_QUESTIONS_BY_QUIZ_ID_SQL = "DELETE FROM Questions WHERE quizId = ?";

    public int addQuestion(Question question) {
        int generatedId = -1;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_QUESTION_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getQuestionType());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        question.setId(generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return generatedId;
    }

    public List<Question> getQuestionsByQuizId(int quizId) {
        List<Question> questions = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_QUESTIONS_BY_QUIZ_ID_SQL)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return questions;
    }

    // Xóa tất cả câu hỏi của một quiz (hữu ích khi cập nhật hoặc xóa quiz)
    public boolean deleteQuestionsByQuizId(int quizId) {
        boolean rowDeleted = false;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_QUESTIONS_BY_QUIZ_ID_SQL)) {
            ps.setInt(1, quizId);
            // executeUpdate trả về số dòng bị ảnh hưởng, có thể > 0
            ps.executeUpdate();
            rowDeleted = true; // Giả định thành công nếu không có exception
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowDeleted;
    }

    private Question extractQuestionFromResultSet(ResultSet rs) throws SQLException {
        return new Question(
                rs.getInt("id"),
                rs.getInt("quizId"),
                rs.getString("questionText"),
                rs.getString("questionType"),
                rs.getTimestamp("createdAt").toLocalDateTime(),
                rs.getTimestamp("updatedAt").toLocalDateTime()
        );
    }

    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                LOGGER.log(Level.SEVERE, "SQLState: {0}", ((SQLException) e).getSQLState());
                LOGGER.log(Level.SEVERE, "Error Code: {0}", ((SQLException) e).getErrorCode());
                LOGGER.log(Level.SEVERE, "Message: {0}", e.getMessage());
            }
        }
    }
}