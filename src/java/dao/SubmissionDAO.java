// src/java/dao/SubmissionDAO.java
package dao;

import dal.DBContext;
import entity.Submission;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubmissionDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(SubmissionDAO.class.getName());

    private static final String INSERT_SUBMISSION_SQL = "INSERT INTO Submissions (quizId, userId, score, timeTakenMinutes, submissionTime) VALUES (?, ?, ?, ?, GETDATE())";
    private static final String SELECT_SUBMISSIONS_BY_QUIZ_ID_SQL = "SELECT * FROM Submissions WHERE quizId = ? ORDER BY submissionTime DESC";
    private static final String SELECT_SUBMISSIONS_BY_USER_AND_QUIZ_SQL = "SELECT * FROM Submissions WHERE userId = ? AND quizId = ? ORDER BY submissionTime DESC";
    private static final String SELECT_LAST_SUBMISSION_SCORE_BY_USER_AND_QUIZ_SQL = "SELECT TOP 1 score FROM Submissions WHERE userId = ? AND quizId = ? ORDER BY submissionTime DESC";


    public int addSubmission(Submission submission) {
        int generatedId = -1;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SUBMISSION_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, submission.getQuizId());
            ps.setInt(2, submission.getUserId());
            ps.setInt(3, submission.getScore());
            ps.setInt(4, submission.getTimeTakenMinutes());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        submission.setId(generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return generatedId;
    }

    public List<Submission> getSubmissionsByQuizId(int quizId) {
        List<Submission> submissions = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SUBMISSIONS_BY_QUIZ_ID_SQL)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    submissions.add(extractSubmissionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return submissions;
    }
    
    public List<Submission> getSubmissionsByUserAndQuizId(int userId, int quizId) {
        List<Submission> submissions = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SUBMISSIONS_BY_USER_AND_QUIZ_SQL)) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    submissions.add(extractSubmissionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return submissions;
    }
    
    public Integer getLastSubmissionScore(int userId, int quizId) {
        Integer score = null;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_LAST_SUBMISSION_SCORE_BY_USER_AND_QUIZ_SQL)) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    score = rs.getInt("score");
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return score;
    }

    private Submission extractSubmissionFromResultSet(ResultSet rs) throws SQLException {
        return new Submission(
                rs.getInt("id"),
                rs.getInt("quizId"),
                rs.getInt("userId"),
                rs.getInt("score"),
                rs.getInt("timeTakenMinutes"),
                rs.getTimestamp("submissionTime").toLocalDateTime()
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