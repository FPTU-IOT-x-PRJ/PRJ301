package dao;

import dal.DBContext;
import entity.Note;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp DAO quản lý các thao tác CRUD và các truy vấn liên quan đến đối tượng
 * Note trong cơ sở dữ liệu. KHÔNG CÓ userId TRỰC TIẾP TRONG BẢNG NOTES. Quyền
 * truy cập được quản lý ở tầng Controller thông qua Subject/Lesson ID. Author:
 * Dung Ann
 */
public class NoteDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(NoteDAO.class.getName());

    // --- Hằng số SQL ---
    // INSERT không có userId trực tiếp vào bảng Notes
    private static final String INSERT_NOTE_SQL
            = "INSERT INTO Notes (title, content, subjectId, lessonId, createdAt, updatedAt) "
            + "VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";

    // Lấy ghi chú bằng ID và kiểm tra quyền sở hữu thông qua userId của Semester
    private static final String SELECT_NOTE_BY_ID_AND_USER_SQL
            = "SELECT n.id, n.title, n.content, n.createdAt, n.updatedAt, n.subjectId, n.lessonId "
            + "FROM Notes n "
            + "LEFT JOIN Lessons l ON n.lessonId = l.id "
            + "LEFT JOIN Subjects s ON n.subjectId = s.id OR l.subjectId = s.id " // Nối Subject qua Note hoặc Lesson
            + "INNER JOIN Semesters sem ON s.semesterId = sem.id " // Nối đến Semester
            + "WHERE n.id = ? AND sem.userId = ?"; // Thêm điều kiện userId

    // Cập nhật ghi chú và kiểm tra quyền sở hữu thông qua userId của Semester
    private static final String UPDATE_NOTE_SQL
            = "UPDATE Notes SET title = ?, content = ?, subjectId = ?, lessonId = ?, updatedAt = GETDATE() "
            + "WHERE id = ? AND EXISTS (SELECT 1 FROM Notes n2 "
            + "LEFT JOIN Lessons l ON n2.lessonId = l.id "
            + "LEFT JOIN Subjects s ON n2.subjectId = s.id OR l.subjectId = s.id "
            + "INNER JOIN Semesters sem ON s.semesterId = sem.id "
            + "WHERE n2.id = Notes.id AND sem.userId = ?)"; // Subquery kiểm tra quyền sở hữu

    // Xóa ghi chú và kiểm tra quyền sở hữu thông qua userId của Semester
    private static final String DELETE_NOTE_SQL
            = "DELETE FROM Notes WHERE id = ? AND EXISTS (SELECT 1 FROM Notes n2 "
            + "LEFT JOIN Lessons l ON n2.lessonId = l.id "
            + "LEFT JOIN Subjects s ON n2.subjectId = s.id OR l.subjectId = s.id "
            + "INNER JOIN Semesters sem ON s.semesterId = sem.id "
            + "WHERE n2.id = Notes.id AND sem.userId = ?)"; // Subquery kiểm tra quyền sở hữu

    private static final String BASE_SELECT_FILTERED_NOTES_SQL
            = "SELECT n.id, n.title, n.content, n.createdAt, n.updatedAt, n.subjectId, n.lessonId "
            + "FROM Notes n "
            + "LEFT JOIN Lessons l ON n.lessonId = l.id "
            + "LEFT JOIN Subjects s ON n.subjectId = s.id OR l.subjectId = s.id " // Nối Subject qua Note hoặc Lesson
            + "INNER JOIN Semesters sem ON s.semesterId = sem.id "; // Luôn luôn nối đến Semester để kiểm tra userId

    // Phương thức thêm ghi chú
    public boolean addNote(Note note) throws SQLException {
        boolean rowAffected = false;
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_NOTE_SQL)) {
            preparedStatement.setString(1, note.getTitle());
            preparedStatement.setString(2, note.getContent());
            if (note.getSubjectId() != null) {
                preparedStatement.setInt(3, note.getSubjectId());
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            if (note.getLessonId() != null) {
                preparedStatement.setInt(4, note.getLessonId());
            } else {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            }
            rowAffected = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
            throw e;
        }
        return rowAffected;
    }

    // Phương thức lấy ghi chú theo ID và UserID
    public Note getNoteById(int id, int userId) throws SQLException {
        Note note = null;
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_NOTE_BY_ID_AND_USER_SQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, userId); // Thêm userId vào điều kiện WHERE
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                note = extractNoteFromResultSet(rs);
            }
        } catch (SQLException e) {
            printSQLException(e);
            throw e;
        }
        return note;
    }

    // Phương thức mới: Lấy ghi chú dựa trên lessonId, subjectId và userId (từ semester)
    // Phương thức này đã có sẵn trong đoạn code bạn cung cấp và phù hợp
    public List<Note> getNotesByLessonOrSubjectId(Integer lessonId, Integer subjectId) throws SQLException {
        List<Note> notes = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT n.id, n.title, n.content, n.createdAt, n.updatedAt, n.subjectId, n.lessonId ");
        sqlBuilder.append("FROM Notes n ");

        List<Object> params = new ArrayList<>();
        boolean hasCondition = false;

        
        sqlBuilder.append("WHERE ");
        if (lessonId != null) {
            sqlBuilder.append("n.lessonId = ? ");
            params.add(lessonId);
        }
        else if (subjectId != null) {
            sqlBuilder.append("n.subjectId = ? ");
            params.add(subjectId);
        }


        sqlBuilder.append("ORDER BY n.createdAt DESC");

        // Logging
        LOGGER.log(Level.INFO, "SQL Query for getNotesByLessonOrSubjectId: {0}", sqlBuilder.toString());
        LOGGER.log(Level.INFO, "Parameters: {0}", params);

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    notes.add(extractNoteFromResultSet(rs));
                }
                LOGGER.log(Level.INFO, "Number of notes fetched: {0}", count);
            }
        } catch (SQLException e) {
            printSQLException(e);
            throw e;
        }

        return notes;
    }

    // Đổi tên từ updateNote thành editNote để phù hợp với Controller
    public boolean editNote(Note note, int userId) throws SQLException {
        boolean rowAffected = false;
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_NOTE_SQL)) {
            preparedStatement.setString(1, note.getTitle());
            preparedStatement.setString(2, note.getContent());
            if (note.getSubjectId() != null) {
                preparedStatement.setInt(3, note.getSubjectId());
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            if (note.getLessonId() != null) {
                preparedStatement.setInt(4, note.getLessonId());
            } else {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            }
            preparedStatement.setInt(5, note.getId());
            preparedStatement.setInt(6, userId); // userId cho subquery kiểm tra quyền sở hữu
            rowAffected = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
            throw e;
        }
        return rowAffected;
    }

    // Phương thức xóa ghi chú theo ID và UserID
    public boolean deleteNote(int id, int userId) throws SQLException {
        boolean rowAffected = false;
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(DELETE_NOTE_SQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, userId); // userId cho subquery kiểm tra quyền sở hữu
            rowAffected = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
            throw e;
        }
        return rowAffected;
    }

    /**
     * Phương thức lấy danh sách ghi chú của một người dùng, có thể lọc theo môn
     * học và buổi học.
     *
     * @param userId ID của người dùng.
     * @param subjectId ID môn học để lọc (có thể là null).
     * @param lessonId ID buổi học để lọc (có thể là null).
     * @return Danh sách các ghi chú phù hợp.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public List<Note> getFilteredNotes(int userId, Integer subjectId, Integer lessonId) throws SQLException {
        List<Note> notes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT_FILTERED_NOTES_SQL);
        sql.append("WHERE sem.userId = ? "); // Luôn lọc theo userId

        if (subjectId != null) {
            sql.append("AND (n.subjectId = ? OR l.subjectId = ?) "); // Kiểm tra subjectId từ Note hoặc Lesson
        }
        if (lessonId != null) {
            sql.append("AND n.lessonId = ? ");
        }
        sql.append("ORDER BY n.createdAt DESC");

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            preparedStatement.setInt(paramIndex++, userId);

            if (subjectId != null) {
                preparedStatement.setInt(paramIndex++, subjectId);
                preparedStatement.setInt(paramIndex++, subjectId); // Cho l.subjectId
            }
            if (lessonId != null) {
                preparedStatement.setInt(paramIndex++, lessonId);
            }

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                notes.add(extractNoteFromResultSet(rs));
            }
        } catch (SQLException e) {
            printSQLException(e);
            throw e;
        }
        return notes;
    }

    /**
     * Trích xuất một đối tượng Note từ ResultSet.
     *
     * @param rs ResultSet chứa dữ liệu Note.
     * @return Đối tượng Note.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private Note extractNoteFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        java.util.Date createdAt = rs.getTimestamp("createdAt");
        java.util.Date updatedAt = rs.getTimestamp("updatedAt");
        Integer subjectId = rs.getObject("subjectId") != null ? rs.getInt("subjectId") : null;
        Integer lessonId = rs.getObject("lessonId") != null ? rs.getInt("lessonId") : null;

        Note note = new Note(id, title, content, createdAt, updatedAt, subjectId, lessonId);
        return note;
    }

    /**
     * Hàm tiện ích để ghi chi tiết lỗi SQL vào logger.
     *
     * @param ex Ngoại lệ SQLException cần ghi.
     */
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
