package dao;

import dal.DBContext;
import entity.Lesson;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO class quản lý các thao tác với bảng Lessons.
 *
 * Author: Dung Ann
 */
public class LessonDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(LessonDAO.class.getName());

    private static final String SELECT_LESSON_BY_ID_SQL = "SELECT id, subjectId, name, lessonDate, description, status, createdAt, updatedAt FROM Lessons WHERE id = ?";
    private static final String SELECT_LESSONS_BY_SUBJECT_ID_SQL = "SELECT id, subjectId, name, lessonDate, description, status, createdAt, updatedAt FROM Lessons WHERE subjectId = ? ORDER BY lessonDate ASC";
    private static final String INSERT_LESSON_SQL = "INSERT INTO Lessons (subjectId, name, lessonDate, description, status, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE())";
    private static final String UPDATE_LESSON_SQL = "UPDATE Lessons SET subjectId = ?, name = ?, lessonDate = ?, description = ?, status = ?, updatedAt = GETDATE() WHERE id = ?";
    private static final String DELETE_LESSON_SQL = "DELETE FROM Lessons WHERE id = ?";
    private static final String COUNT_LESSONS_BY_SUBJECT_ID_SQL = "SELECT COUNT(id) FROM Lessons WHERE subjectId = ?";

    /**
     * Lấy một buổi học bằng ID.
     * @param lessonId ID của buổi học.
     * @return Đối tượng Lesson nếu tìm thấy, ngược lại trả về null.
     */
    public Lesson getLessonById(int lessonId) {
        Lesson lesson = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LESSON_BY_ID_SQL)) {
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
     * Lấy danh sách các buổi học theo ID môn học.
     * @param subjectId ID của môn học.
     * @return Danh sách các buổi học.
     */
    public List<Lesson> getLessonsBySubjectId(int subjectId) {
        List<Lesson> lessons = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LESSONS_BY_SUBJECT_ID_SQL)) {
            preparedStatement.setInt(1, subjectId);
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
     * Lấy danh sách các buổi học theo ID môn học, có phân trang, tìm kiếm và lọc.
     * @param subjectId ID của môn học (bắt buộc).
     * @param search Từ khóa tìm kiếm (tên, mô tả). Có thể null hoặc rỗng.
     * @param status Trạng thái buổi học để lọc. Có thể null hoặc rỗng.
     * @param page Trang hiện tại (bắt đầu từ 1).
     * @param pageSize Số lượng buổi học mỗi trang.
     * @return Danh sách các buổi học.
     */
    public List<Lesson> getFilteredLessonsBySubjectId(int subjectId, String search, String status, int page, int pageSize) {
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
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

    // --- SQL Constants ---
    private static final String SELECT_LESSONS_FOR_SEMESTER_SQL = 
            "SELECT l.id, l.name, l.lessonDate, l.description, l.status, l.subjectId, l.createdAt, l.updatedAt " +
            "FROM Lessons l " +
            "JOIN Subjects s ON l.subjectId = s.id " +
            "JOIN Semesters sem ON s.semesterId = sem.id " +
            "WHERE sem.id = ? AND sem.userId = ? " + // Đảm bảo đúng user sở hữu semester
            "ORDER BY l.lessonDate, l.name"; // Sắp xếp để dễ hiển thị
    
    public List<Lesson> getAllLessonsForSemester(int semesterId, int userId) throws SQLException {
        List<Lesson> lessons = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_LESSONS_FOR_SEMESTER_SQL)) {
            ps.setInt(1, semesterId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lessons.add(extractLessonFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching lessons for semester ID: " + semesterId + " and user ID: " + userId, e);
            throw e; // Ném lại ngoại lệ để lớp gọi xử lý
        }
        return lessons;
    }
    
    /**
     * Đếm tổng số buổi học dựa trên các tiêu chí lọc.
     * @param subjectId ID của môn học (bắt buộc).
     * @param search Từ khóa tìm kiếm (tên, mô tả). Có thể null hoặc rỗng.
     * @param status Trạng thái buổi học để lọc. Có thể null hoặc rỗng.
     * @return Tổng số buổi học thỏa mãn điều kiện.
     */
    public int countLessonsBySubjectId(int subjectId, String search, String status) {
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
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
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
     * Thêm một buổi học mới vào cơ sở dữ liệu.
     * @param lesson Đối tượng Lesson cần thêm.
     * @return true nếu thêm thành công, ngược lại false.
     */
    public boolean addLesson(Lesson lesson) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_LESSON_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
                return true;
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    /**
     * Cập nhật thông tin một buổi học.
     * @param lesson Đối tượng Lesson chứa thông tin cần cập nhật (ID để xác định bản ghi).
     * @return true nếu cập nhật thành công, ngược lại false.
     */
    public boolean updateLesson(Lesson lesson) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LESSON_SQL)) {
            preparedStatement.setInt(1, lesson.getSubjectId());
            preparedStatement.setString(2, lesson.getName());
            preparedStatement.setDate(3, lesson.getLessonDate());
            preparedStatement.setString(4, lesson.getDescription());
            preparedStatement.setString(5, lesson.getStatus());
            // updatedAt được DB tự động cập nhật GETDATE()
            preparedStatement.setInt(6, lesson.getId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    /**
     * Xóa một buổi học bằng ID.
     * @param lessonId ID của buổi học cần xóa.
     * @return true nếu xóa thành công, ngược lại false.
     */
    public boolean deleteLesson(int lessonId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_LESSON_SQL)) {
            preparedStatement.setInt(1, lessonId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            printSQLException(e);
            // Log lỗi cụ thể hơn, ví dụ có ràng buộc khóa ngoại
            if (e instanceof SQLException && e.getErrorCode() == 547) { // SQL Server Foreign Key Constraint error code
                 LOGGER.log(Level.WARNING, "Cannot delete lesson with ID {0} due to foreign key constraint.", lessonId);
            }
        }
        return false;
    }

    /**
     * Phương thức trợ giúp để trích xuất dữ liệu từ ResultSet thành đối tượng Lesson.
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
     * Hàm tiện ích để in chi tiết lỗi SQL ra System.err.
     * Trong ứng dụng thực tế, nên sử dụng một logging framework (ví dụ: Log4j, SLF4J).
     * @param ex Ngoại lệ SQLException cần in.
     */
    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.err.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}