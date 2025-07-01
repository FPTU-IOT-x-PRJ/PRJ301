package dao;

import dal.DBContext;
import entity.Lesson;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp DAO quản lý các thao tác CRUD và các truy vấn liên quan đến đối tượng Lesson trong cơ sở dữ liệu.
 * Author: Dung Ann
 */
public class LessonDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(LessonDAO.class.getName());

    // --- Hằng số SQL ---
    private static final String INSERT_LESSON_SQL = "INSERT INTO Lessons (subjectId, name, lessonDate, description, status, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE())";
    private static final String SELECT_LESSON_BY_ID_SQL = "SELECT id, subjectId, name, lessonDate, description, status, createdAt, updatedAt FROM Lessons WHERE id = ?";
    private static final String UPDATE_LESSON_SQL = "UPDATE Lessons SET subjectId = ?, name = ?, lessonDate = ?, description = ?, status = ?, updatedAt = GETDATE() WHERE id = ?";
    private static final String DELETE_LESSON_SQL = "DELETE FROM Lessons WHERE id = ?";
    private static final String SELECT_LESSONS_BY_SUBJECT_ID_SQL = "SELECT id, subjectId, name, lessonDate, description, status, createdAt, updatedAt FROM Lessons WHERE subjectId = ? ORDER BY lessonDate ASC";
    private static final String SELECT_LESSONS_FOR_SEMESTER_SQL = 
            "SELECT l.id, l.name, l.lessonDate, l.description, l.status, l.subjectId, l.createdAt, l.updatedAt " +
            "FROM Lessons l " +
            "JOIN Subjects s ON l.subjectId = s.id " +
            "JOIN Semesters sem ON s.semesterId = sem.id " +
            "WHERE sem.id = ? AND sem.userId = ? " +
            "ORDER BY l.lessonDate, l.name";
    private static final String COUNT_LESSONS_BY_SUBJECT_ID_SQL = "SELECT COUNT(id) FROM Lessons WHERE subjectId = ?";

    /**
     * Thêm một buổi học mới vào cơ sở dữ liệu.
     *
     * @param lesson Đối tượng Lesson cần thêm.
     * @return true nếu thêm thành công, ngược lại là false.
     */
    public boolean addLesson(Lesson lesson) {
        boolean rowInserted = false;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(INSERT_LESSON_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, lesson.getSubjectId());
            preparedStatement.setString(2, lesson.getName());
            preparedStatement.setDate(3, lesson.getLessonDate());
            preparedStatement.setString(4, lesson.getDescription());
            preparedStatement.setString(5, lesson.getStatus());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lesson.setId(generatedKeys.getInt(1)); // Cập nhật ID cho đối tượng Lesson
                    }
                }
                rowInserted = true;
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowInserted;
    }

    /**
     * Lấy một buổi học bằng ID.
     *
     * @param lessonId ID của buổi học.
     * @return Đối tượng Lesson nếu tìm thấy, ngược lại trả về null.
     */
    public Lesson getLessonById(int lessonId) {
        Lesson lesson = null;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(SELECT_LESSON_BY_ID_SQL)) {
            preparedStatement.setInt(1, lessonId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    lesson = extractLessonFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return lesson;
    }

    /**
     * Lấy danh sách các buổi học theo ID môn học, có phân trang, tìm kiếm và lọc.
     *
     * @param subjectId ID của môn học (bắt buộc).
     * @param search Từ khóa tìm kiếm (tên, mô tả). Có thể null hoặc rỗng.
     * @param status Trạng thái buổi học để lọc. Có thể null hoặc rỗng.
     * @param page Trang hiện tại (bắt đầu từ 1).
     * @param pageSize Số lượng buổi học mỗi trang.
     * @return Danh sách các buổi học.
     */
    public List<Lesson> getAllLessonsBySubjectId(int subjectId, String search, String status, int page, int pageSize) {
        List<Lesson> lessons = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, subjectId, name, lessonDate, description, status, createdAt, updatedAt FROM Lessons WHERE subjectId = ?");
        List<Object> params = new ArrayList<>();
        params.add(subjectId);

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR description LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (status != null && !status.trim().isEmpty()) {
            sqlBuilder.append(" AND status = ?");
            params.add(status);
        }

        sqlBuilder.append(" ORDER BY lessonDate ASC, createdAt DESC");
        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        
        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);

        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    lessons.add(extractLessonFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return lessons;
    }
    
    /**
     * Lấy tất cả các buổi học cho một kỳ học và người dùng cụ thể.
     *
     * @param semesterId ID của kỳ học.
     * @param userId ID của người dùng.
     * @return Danh sách các đối tượng Lesson.
     * @throws SQLException Nếu có lỗi SQL xảy ra.
     */
    public List<Lesson> getAllLessonsForSemester(int semesterId, int userId) throws SQLException {
        List<Lesson> lessons = new ArrayList<>();
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(SELECT_LESSONS_FOR_SEMESTER_SQL)) {
            preparedStatement.setInt(1, semesterId);
            preparedStatement.setInt(2, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    lessons.add(extractLessonFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy buổi học cho kỳ học ID: " + semesterId + " và người dùng ID: " + userId, e);
            throw e;
        }
        return lessons;
    }

    /**
     * Cập nhật thông tin một buổi học.
     *
     * @param lesson Đối tượng Lesson chứa thông tin cần cập nhật (ID để xác định bản ghi).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public boolean editLesson(Lesson lesson) {
        boolean rowUpdated = false;
        try (Connection con = getConnection();
                PreparedStatement preparedStatement = con.prepareStatement(UPDATE_LESSON_SQL)) {
            preparedStatement.setInt(1, lesson.getSubjectId());
            preparedStatement.setString(2, lesson.getName());
            preparedStatement.setDate(3, lesson.getLessonDate());
            preparedStatement.setString(4, lesson.getDescription());
            preparedStatement.setString(5, lesson.getStatus());
            preparedStatement.setInt(6, lesson.getId());

            rowUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowUpdated;
    }

    /**
     * Xóa một buổi học bằng ID.
     *
     * @param lessonId ID của buổi học cần xóa.
     * @return true nếu xóa thành công, ngược lại là false.
     */
    public boolean deleteLesson(int lessonId) {
        boolean rowDeleted = false;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(DELETE_LESSON_SQL)) {
            preparedStatement.setInt(1, lessonId);
            rowDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
            if (e instanceof SQLException && e.getErrorCode() == 547) { // SQL Server Foreign Key Constraint error code
                 LOGGER.log(Level.WARNING, "Không thể xóa buổi học với ID {0} do ràng buộc khóa ngoại.", lessonId);
            }
        }
        return rowDeleted;
    }

    /**
     * Đếm tổng số buổi học dựa trên các tiêu chí lọc.
     *
     * @param subjectId ID của môn học (bắt buộc).
     * @param search Từ khóa tìm kiếm (tên, mô tả). Có thể null hoặc rỗng.
     * @param status Trạng thái buổi học để lọc. Có thể null hoặc rỗng.
     * @return Tổng số buổi học thỏa mãn điều kiện.
     */
    public int countLessons(int subjectId, String search, String status) {
        int totalLessons = 0;
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(id) FROM Lessons WHERE subjectId = ?");
        List<Object> params = new ArrayList<>();
        params.add(subjectId);

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR description LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (status != null && !status.trim().isEmpty()) {
            sqlBuilder.append(" AND status = ?");
            params.add(status);
        }
        
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    totalLessons = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return totalLessons;
    }

    /**
     * Phương thức trợ giúp để trích xuất dữ liệu từ ResultSet thành đối tượng Lesson.
     *
     * @param rs ResultSet chứa dữ liệu buổi học.
     * @return Đối tượng Lesson.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private Lesson extractLessonFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int subjectId = rs.getInt("subjectId");
        String name = rs.getString("name");
        java.sql.Date lessonDate = rs.getDate("lessonDate");
        String description = rs.getString("description");
        String status = rs.getString("status");
        LocalDateTime createdAt = rs.getTimestamp("createdAt").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updatedAt").toLocalDateTime();
        return new Lesson(id, subjectId, name, lessonDate, description, status, createdAt, updatedAt);
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
