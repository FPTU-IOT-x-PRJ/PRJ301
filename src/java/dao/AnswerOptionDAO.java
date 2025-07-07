// src/java/dao/AnswerOptionDAO.java
package dao;

import dal.DBContext;
import entity.AnswerOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnswerOptionDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(AnswerOptionDAO.class.getName());

    private static final String INSERT_OPTION_SQL = "INSERT INTO AnswerOptions (questionId, optionText, isCorrect) VALUES (?, ?, ?)";
    private static final String SELECT_OPTIONS_BY_QUESTION_ID_SQL = "SELECT * FROM AnswerOptions WHERE questionId = ?";
    private static final String DELETE_OPTIONS_BY_QUESTION_ID_SQL = "DELETE FROM AnswerOptions WHERE questionId = ?";


    public boolean addAnswerOption(AnswerOption option) {
        boolean rowInserted = false;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_OPTION_SQL)) {
            ps.setInt(1, option.getQuestionId());
            ps.setString(2, option.getOptionText());
            ps.setBoolean(3, option.isCorrect());
            rowInserted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowInserted;
    }

    public List<AnswerOption> getAnswerOptionsByQuestionId(int questionId) {
        List<AnswerOption> options = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_OPTIONS_BY_QUESTION_ID_SQL)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    options.add(extractAnswerOptionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return options;
    }

    public boolean deleteAnswerOptionsByQuestionId(int questionId) {
        boolean rowDeleted = false;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_OPTIONS_BY_QUESTION_ID_SQL)) {
            ps.setInt(1, questionId);
            ps.executeUpdate();
            rowDeleted = true;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowDeleted;
    }


    private AnswerOption extractAnswerOptionFromResultSet(ResultSet rs) throws SQLException {
        return new AnswerOption(
                rs.getInt("id"),
                rs.getInt("questionId"),
                rs.getString("optionText"),
                rs.getBoolean("isCorrect")
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