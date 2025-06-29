/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import entity.Subject;

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
 * DAO class quản lý các thao tác với bảng Subjects.
 *
 * Author: Dung Ann
 */
public class SubjectDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(SubjectDAO.class.getName());

    private static final String SELECT_ALL_SUBJECTS_SQL = "SELECT * FROM Subjects WHERE semester_id = ?";
    private static final String INSERT_SUBJECT_SQL = "INSERT INTO Subjects (semester_id, name, code, description, credits, teacher_name, is_active, prerequisites, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";
    private static final String UPDATE_SUBJECT_SQL = "UPDATE Subjects SET semester_id = ?, name = ?, code = ?, description = ?, credits = ?, teacher_name = ?, is_active = ?, prerequisites = ?, updated_at = GETDATE() WHERE id = ?";
    private static final String DELETE_SUBJECT_SQL = "DELETE FROM Subjects WHERE id = ?";
    private static final String SELECT_SUBJECT_BY_ID_SQL = "SELECT * FROM Subjects WHERE id = ?";

    /**
     * Lấy danh sách tất cả môn học theo semesterId.
     */
    public List<Subject> selectAllSubjects(int semesterId) {
        List<Subject> subjects = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_SUBJECTS_SQL)) {
            ps.setInt(1, semesterId);
            try (ResultSet rs = ps.executeQuery()) {
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
     * Lấy tổng số môn học theo điều kiện tìm kiếm (ví dụ: theo tên, code). Bạn
     * có thể mở rộng thêm các tham số lọc nếu cần.
     */
    public int getTotalSubjectCount(String search, int semesterId) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(id) FROM Subjects WHERE semester_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(semesterId);

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR code LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }

        try (PreparedStatement ps = connection.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
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
     * Thêm mới một môn học.
     */
    public boolean insertSubject(Subject subject) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SUBJECT_SQL)) {
            ps.setInt(1, subject.getSemesterId());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getCode());
            ps.setString(4, subject.getDescription());
            ps.setInt(5, subject.getCredits());
            ps.setString(6, subject.getTeacherName());
            ps.setBoolean(7, subject.isActive());
            ps.setString(8, subject.getPrerequisites());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    /**
     * Cập nhật thông tin môn học.
     */
    public boolean updateSubject(Subject subject) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SUBJECT_SQL)) {
            ps.setInt(1, subject.getSemesterId());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getCode());
            ps.setString(4, subject.getDescription());
            ps.setInt(5, subject.getCredits());
            ps.setString(6, subject.getTeacherName());
            ps.setBoolean(7, subject.isActive());
            ps.setString(8, subject.getPrerequisites());
            ps.setInt(9, subject.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

     /**
     * Xóa một môn học khỏi cơ sở dữ liệu.
     *
     * @param subjectId ID của môn học cần xóa.
     * @return true nếu xóa thành công, false nếu không thành công.
     */
    public boolean deleteSubject(int subjectId) {
        boolean rowDeleted = false;
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SUBJECT_SQL)) {
            statement.setInt(1, subjectId);
            rowDeleted = statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting subject with ID: " + subjectId, ex);
            printSQLException(ex);
        }
        return rowDeleted;
    }
    /**
     * Lấy môn học theo id.
     */
    public Subject getSubjectById(int id) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_SUBJECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractSubjectFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return null;
    }

    /**
     * Trích xuất dữ liệu từ ResultSet thành đối tượng Subject.
     */
    private Subject extractSubjectFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int semesterId = rs.getInt("semester_id");
        String name = rs.getString("name");
        String code = rs.getString("code");
        String description = rs.getString("description");
        int credits = rs.getInt("credits");
        String teacherName = rs.getString("teacher_name");
        boolean isActive = rs.getBoolean("is_active");
        String prerequisites = rs.getString("prerequisites");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();

        return new Subject(id, semesterId, name, code, description, credits, teacherName, isActive, prerequisites, createdAt, updatedAt);
    }

    public boolean isCodeExists(String code) {
        String sql = "SELECT COUNT(*) FROM Subjects WHERE code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    public boolean isCodeExistsExceptId(String code, int id) {
        String sql = "SELECT COUNT(*) FROM Subjects WHERE code = ? AND id <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    /**
     * Hàm tiện ích in lỗi SQLException.
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

    public List<Subject> getFilteredAndPaginatedSubjects(
            String search, Integer semesterId, Boolean isActive,
            int offset, int limit, Integer userId
    ) {
        List<Subject> subjects = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Subjects WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Nếu có userId (nếu bạn lưu userId ở bảng Subjects)
//        if (userId != null) {
//            sqlBuilder.append(" AND userId = ?");
//            params.add(userId);
//        }

        if (semesterId != null) {
            sqlBuilder.append(" AND semester_id = ?");
            params.add(semesterId);
        }

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR code LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (isActive != null) {
            sqlBuilder.append(" AND is_active = ?");
            params.add(isActive);
        }

        sqlBuilder.append(" ORDER BY created_at DESC");
        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
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

}