package dao;

import dal.DBContext;
import entity.Subject;

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
 * Lớp DAO quản lý các thao tác CRUD và các truy vấn liên quan đến đối tượng
 * Subject trong cơ sở dữ liệu. Author: Dung Ann
 */
public class SubjectDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(SubjectDAO.class.getName());

    // --- Hằng số SQL ---
    private static final String INSERT_SUBJECT_SQL = "INSERT INTO Subjects (semesterId, name, code, description, credits, teacherName, isActive, prerequisites, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
    private static final String SELECT_SUBJECT_BY_ID_SQL = "SELECT * FROM Subjects WHERE id = ?";
    private static final String UPDATE_SUBJECT_SQL = "UPDATE Subjects SET semesterId = ?, name = ?, code = ?, description = ?, credits = ?, teacherName = ?, isActive = ?, prerequisites = ?, updatedAt = GETDATE() WHERE id = ?";
    private static final String DELETE_SUBJECT_SQL = "DELETE FROM Subjects WHERE id = ?";
    private static final String CHECK_CODE_EXISTS_SQL = "SELECT COUNT(*) FROM Subjects WHERE code = ? AND semesterId = ?";
    private static final String CHECK_CODE_EXISTS_EXCEPT_ID_SQL = "SELECT COUNT(*) FROM Subjects WHERE code = ? AND semesterId = ? AND id <> ?";

    /**
     * Thêm một môn học mới vào cơ sở dữ liệu.
     *
     * @param subject Đối tượng Subject cần thêm.
     * @return true nếu thêm thành công, ngược lại là false.
     */
    public boolean addSubject(Subject subject) {
        boolean rowInserted = false;
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(INSERT_SUBJECT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, subject.getSemesterId());
            preparedStatement.setString(2, subject.getName());
            preparedStatement.setString(3, subject.getCode());
            preparedStatement.setString(4, subject.getDescription());
            preparedStatement.setInt(5, subject.getCredits());
            preparedStatement.setString(6, subject.getTeacherName());
            preparedStatement.setBoolean(7, subject.isActive());
            preparedStatement.setString(8, subject.getPrerequisites());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        subject.setId(generatedKeys.getInt(1)); // Cập nhật ID cho đối tượng Subject
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
     * Lấy một môn học bằng ID.
     *
     * @param id ID của môn học.
     * @return Đối tượng Subject nếu tìm thấy, ngược lại trả về null.
     */
    public Subject getSubjectById(int id) {
        Subject subject = null;
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(SELECT_SUBJECT_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    subject = extractSubjectFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return subject;
    }

    /**
     * Lấy danh sách tất cả môn học theo semesterId, có hỗ trợ tìm kiếm, lọc và
     * phân trang.
     *
     * @param search Từ khóa tìm kiếm (tên, mã). Có thể null hoặc rỗng.
     * @param semesterId ID của kỳ học để lọc. Có thể null.
     * @param isActive Trạng thái hoạt động của môn học để lọc. Có thể null.
     * @param offset Vị trí bắt đầu của kết quả (cho phân trang).
     * @param limit Số lượng kết quả tối đa mỗi trang.
     * @param teacherName Tên giáo viên để lọc. Có thể null hoặc rỗng.
     * @return Danh sách các đối tượng Subject.
     */
    public List<Subject> getAllSubjects(
            String search, Integer semesterId, Boolean isActive,
            int offset, int limit, String teacherName) {
        List<Subject> subjects = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT s.id, s.name, s.code, s.credits, s.description, s.prerequisites, s.isActive, s.semesterId, s.createdAt, s.updatedAt, s.teacherName ");
        sqlBuilder.append("FROM Subjects s ");
        sqlBuilder.append("WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (semesterId != null) {
            sqlBuilder.append(" AND s.semesterId = ?");
            params.add(semesterId);
        }

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (s.name LIKE ? OR s.code LIKE ?) ");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (teacherName != null && !teacherName.trim().isEmpty()) {
            sqlBuilder.append(" AND s.teacherName LIKE ? ");
            params.add("%" + teacherName + "%");
        }

        if (isActive != null) {
            sqlBuilder.append(" AND s.isActive = ?");
            params.add(isActive);
        }

        sqlBuilder.append(" ORDER BY s.createdAt DESC");
        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    subjects.add(extractSubjectFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return subjects;
    }

    /**
     * Cập nhật thông tin môn học.
     *
     * @param subject Đối tượng Subject chứa thông tin cần cập nhật (ID để xác
     * định bản ghi).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public boolean editSubject(Subject subject) {
        boolean rowUpdated = false;
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(UPDATE_SUBJECT_SQL)) {
            preparedStatement.setInt(1, subject.getSemesterId());
            preparedStatement.setString(2, subject.getName());
            preparedStatement.setString(3, subject.getCode());
            preparedStatement.setString(4, subject.getDescription());
            preparedStatement.setInt(5, subject.getCredits());
            preparedStatement.setString(6, subject.getTeacherName());
            preparedStatement.setBoolean(7, subject.isActive());
            preparedStatement.setString(8, subject.getPrerequisites());
            preparedStatement.setInt(9, subject.getId());
            rowUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowUpdated;
    }

    /**
     * Xóa một môn học khỏi cơ sở dữ liệu.
     *
     * @param subjectId ID của môn học cần xóa.
     * @return true nếu xóa thành công, false nếu không thành công.
     */
    public boolean deleteSubject(int subjectId) {
        boolean rowDeleted = false;
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(DELETE_SUBJECT_SQL)) {
            preparedStatement.setInt(1, subjectId);
            rowDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa môn học với ID: " + subjectId, ex);
            printSQLException(ex);
        }
        return rowDeleted;
    }

    /**
     * Đếm tổng số môn học theo điều kiện tìm kiếm.
     *
     * @param search Từ khóa tìm kiếm (tên, mã). Có thể null hoặc rỗng.
     * @param semesterId ID của kỳ học để lọc. Có thể null.
     * @param teacherName Tên giáo viên để lọc. Có thể null hoặc rỗng.
     * @return Tổng số môn học thỏa mãn điều kiện.
     */
    public int countSubjects(String search, Integer semesterId, String teacherName) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(s.id) FROM Subjects s WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (semesterId != null) {
            sqlBuilder.append(" AND s.semesterId = ?");
            params.add(semesterId);
        }
        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (s.name LIKE ? OR s.code LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }
        if (teacherName != null && !teacherName.trim().isEmpty()) {
            sqlBuilder.append(" AND s.teacherName LIKE ?");
            params.add("%" + teacherName + "%");
        }

        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return 0;
    }

    /**
     * Kiểm tra xem mã môn học đã tồn tại trong một kỳ học cụ thể chưa.
     *
     * @param code Mã môn học cần kiểm tra.
     * @param semesterId ID của kỳ học.
     * @return true nếu mã tồn tại, ngược lại là false.
     */
    public boolean isCodeExists(String code, int semesterId) {
        boolean exists = false;
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(CHECK_CODE_EXISTS_SQL)) {
            preparedStatement.setString(1, code);
            preparedStatement.setInt(2, semesterId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return exists;
    }

    /**
     * Kiểm tra xem mã môn học đã tồn tại trong một kỳ học cụ thể chưa, loại trừ
     * một ID môn học. Hữu ích khi cập nhật để kiểm tra trùng lặp không phải với
     * chính bản ghi đó.
     *
     * @param code Mã môn học cần kiểm tra.
     * @param semesterId ID của kỳ học.
     * @param id ID của môn học cần loại trừ.
     * @return true nếu mã tồn tại (cho một môn học khác), ngược lại là false.
     */
    public boolean isCodeExistsExceptId(String code, int semesterId, int id) {
        boolean exists = false;
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(CHECK_CODE_EXISTS_EXCEPT_ID_SQL)) {
            preparedStatement.setString(1, code);
            preparedStatement.setInt(2, semesterId);
            preparedStatement.setInt(3, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return exists;
    }

    /**
     * Trích xuất dữ liệu từ ResultSet thành đối tượng Subject.
     *
     * @param rs ResultSet chứa dữ liệu môn học.
     * @return Đối tượng Subject.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private Subject extractSubjectFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int semesterId = rs.getInt("semesterId");
        String name = rs.getString("name");
        String code = rs.getString("code");
        String description = rs.getString("description");
        int credits = rs.getInt("credits");
        String teacherName = rs.getString("teacherName");
        boolean isActive = rs.getBoolean("isActive");
        String prerequisites = rs.getString("prerequisites");
        LocalDateTime createdAt = rs.getTimestamp("createdAt").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updatedAt").toLocalDateTime();

        return new Subject(id, semesterId, name, code, description, credits, teacherName, isActive, prerequisites, createdAt, updatedAt);
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
