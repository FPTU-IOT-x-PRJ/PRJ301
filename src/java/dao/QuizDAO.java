package dao;

import dal.DBContext;
import entity.Quizz;
import entity.Quizzes;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class QuizDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(QuizDAO.class.getName());

    // --- SQL ---
    private static final String INSERT_QUIZ_SQL = 
        "INSERT INTO Quizzes (subject_id, title, description) VALUES (?, ?, ?)";

    private static final String INSERT_QUESTION_SQL = 
        "INSERT INTO Quiz_Questions (quiz_id, question, answer, options) VALUES (?, ?, ?, ?)";

    private static final String SELECT_QUIZ_BY_ID_SQL = 
        "SELECT id, subject_id, title, description FROM Quizzes WHERE id = ?";

    private static final String SELECT_QUESTIONS_BY_QUIZ_ID_SQL = 
        "SELECT id, quiz_id, question, answer, options FROM Quiz_Questions WHERE quiz_id = ?";

    /**
     * Thêm quiz mới vào CSDL.
     */
    public boolean addQuiz(Quizz quiz) {
        boolean inserted = false;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_QUIZ_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, quiz.getSubjectId());
            ps.setString(2, quiz.getTitle());
            ps.setString(3, quiz.getDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        quiz.setId(rs.getInt(1));
                        inserted = true;
                    }
                }
            }

        } catch (SQLException e) {
            printSQLException(e);
        }

        return inserted;
    }

    /**
     * Thêm danh sách câu hỏi vào bảng Quiz_Questions.
     */
    public boolean addQuestions(int quizId, List<Quizzes> questions) {
        boolean success = false;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_QUESTION_SQL)) {

            for (Quizzes q : questions) {
                ps.setInt(1, quizId);
                ps.setString(2, q.getQuestion());
                ps.setString(3, String.join("|", q.getAnswers())); // join string if needed
                ps.setString(4, String.join("|", q.getOptions()));
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            success = Arrays.stream(results).allMatch(i -> i >= 0);

        } catch (SQLException e) {
            printSQLException(e);
        }

        return success;
    }

    /**
     * Truy vấn quiz theo ID.
     */
    public Quizz getQuizById(int id) {
        Quizz quiz = null;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_QUIZ_BY_ID_SQL)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quiz = new Quizz();
                    quiz.setId(rs.getInt("id"));
                    quiz.setSubjectId(rs.getInt("subject_id"));
                    quiz.setTitle(rs.getString("title"));
                    quiz.setDescription(rs.getString("description"));
                }
            }

        } catch (SQLException e) {
            printSQLException(e);
        }

        return quiz;
    }

    /**
     * Lấy danh sách câu hỏi theo quiz_id.
     */
    public List<Quizzes> getQuestionsByQuizId(int quizId) {
        List<Quizzes> list = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_QUESTIONS_BY_QUIZ_ID_SQL)) {

            ps.setInt(1, quizId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quizzes q = new Quizzes();
                    q.setId(rs.getInt("id"));
                    q.setQuizId(rs.getInt("quiz_id"));
                    q.setQuestion(rs.getString("question"));
                    q.setAnswers(rs.getString("answer"));
                    q.setOptions(rs.getString("options"));

                    list.add(q);
                }
            }

        } catch (SQLException e) {
            printSQLException(e);
        }

        return list;
    }

    /**
     * In lỗi SQL chi tiết.
     */
    private void printSQLException(SQLException ex) {
        while (ex != null) {
            LOGGER.severe("SQLState: " + ex.getSQLState());
            LOGGER.severe("Error Code: " + ex.getErrorCode());
            LOGGER.severe("Message: " + ex.getMessage());
            ex = ex.getNextException();
        }
    }
}
