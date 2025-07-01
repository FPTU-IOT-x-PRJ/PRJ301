package dao;

import dal.DBContext;
import entity.Semester;
import java.sql.Date;
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
 * Lớp DAO quản lý các thao tác CRUD và các truy vấn liên quan đến đối tượng Semester trong cơ sở dữ liệu.
 * Author: Dung Ann
 */
public class SemesterDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(SemesterDAO.class.getName());

    // --- Hằng số SQL ---
    private static final String INSERT_SEMESTER_SQL = "INSERT INTO Semesters (name, startDate, endDate, description, status, createdAt, updatedAt, userId) VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE(), ?)";
    private static final String SELECT_SEMESTER_BY_ID_SQL = "SELECT id, name, startDate, endDate, description, status, createdAt, updatedAt, userId FROM Semesters WHERE id = ? AND userId = ?";
    private static final String UPDATE_SEMESTER_SQL = "UPDATE Semesters SET name = ?, startDate = ?, endDate = ?, description = ?, status = ?, updatedAt = GETDATE() WHERE id = ? AND userId = ?";
    private static final String DELETE_SEMESTER_SQL = "DELETE FROM Semesters WHERE id = ? AND userId = ?";
    private static final String SELECT_SEMESTER_BY_NAME_SQL = "SELECT id, name, startDate, endDate, description, status, createdAt, updatedAt, userId FROM Semesters WHERE name = ? AND userId = ?";
    private static final String SELECT_SEMESTER_BY_NAME_EXCEPT_ID_SQL = "SELECT id, name, startDate, endDate, description, status, createdAt, updatedAt, userId FROM Semesters WHERE name = ? AND id != ? AND userId = ?";
    private static final String SELECT_SEMESTER_LASTEST_SQL = "SELECT TOP 1 * FROM Semesters WHERE userId = ? ORDER BY startDate DESC, createdAt DESC";
    
    /**
     * Thêm một kỳ học mới vào cơ sở dữ liệu.
     *
     * @param semester Đối tượng Semester cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addSemester(Semester semester) {
        boolean rowInserted = false;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(INSERT_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, semester.getName());
            preparedStatement.setDate(2, semester.getStartDate());
            preparedStatement.setDate(3, semester.getEndDate());
            preparedStatement.setString(4, semester.getDescription());
            preparedStatement.setString(5, semester.getStatus());
            preparedStatement.setInt(6, semester.getUserId());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        semester.setId(generatedKeys.getInt(1)); // Cập nhật ID cho đối tượng Semester
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
     * Lấy một kỳ học dựa trên ID và userId.
     *
     * @param id ID của kỳ học.
     * @param userId ID của người dùng sở hữu kỳ học.
     * @return Đối tượng Semester nếu tìm thấy, ngược lại trả về null.
     */
    public Semester getSemesterById(int id, int userId) {
        Semester semester = null;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(SELECT_SEMESTER_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    semester = extractSemesterFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return semester;
    }

    /**
     * Lấy tất cả các kỳ học từ cơ sở dữ liệu cho một người dùng cụ thể, có hỗ trợ tìm kiếm, lọc và phân trang.
     *
     * @param search Từ khóa tìm kiếm (tên, mô tả). Có thể null hoặc rỗng.
     * @param statusFilter Trạng thái kỳ học để lọc. Có thể null hoặc rỗng.
     * @param startDate Ngày bắt đầu để lọc. Có thể null.
     * @param endDate Ngày kết thúc để lọc. Có thể null.
     * @param offset Vị trí bắt đầu của kết quả (cho phân trang).
     * @param limit Số lượng kết quả tối đa mỗi trang.
     * @param userId ID của người dùng sở hữu kỳ học.
     * @return Danh sách các đối tượng Semester.
     */
    public List<Semester> getAllSemesters(String search, String statusFilter, Date startDate, Date endDate, int offset, int limit, int userId) {
        List<Semester> semesters = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, name, startDate, endDate, description, status, createdAt, updatedAt, userId FROM Semesters WHERE userId = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR description LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (statusFilter != null && !statusFilter.isEmpty()) {
            sqlBuilder.append(" AND status = ?");
            params.add(statusFilter);
        }

        if (startDate != null) {
            sqlBuilder.append(" AND startDate >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            sqlBuilder.append(" AND endDate <= ?");
            params.add(endDate);
        }

        sqlBuilder.append(" ORDER BY startDate DESC"); // Mặc định sắp xếp theo ngày bắt đầu giảm dần

        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    semesters.add(extractSemesterFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return semesters;
    }

    /**
     * Cập nhật thông tin một kỳ học trong cơ sở dữ liệu.
     *
     * @param semester Đối tượng Semester chứa thông tin cần cập nhật (phải có ID).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean editSemester(Semester semester) {
        boolean rowUpdated = false;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(UPDATE_SEMESTER_SQL)) {
            preparedStatement.setString(1, semester.getName());
            preparedStatement.setDate(2, semester.getStartDate());
            preparedStatement.setDate(3, semester.getEndDate());
            preparedStatement.setString(4, semester.getDescription());
            preparedStatement.setString(5, semester.getStatus());
            preparedStatement.setInt(6, semester.getId());
            preparedStatement.setInt(7, semester.getUserId());

            rowUpdated = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowUpdated;
    }

    /**
     * Xóa một kỳ học khỏi cơ sở dữ liệu.
     *
     * @param id ID của kỳ học cần xóa.
     * @param userId ID của người dùng sở hữu kỳ học (để đảm bảo quyền).
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean deleteSemester(int id, int userId) {
        boolean rowDeleted = false;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(DELETE_SEMESTER_SQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, userId);
            rowDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowDeleted;
    }

    /**
     * Đếm tổng số kỳ học dựa trên các tiêu chí lọc.
     *
     * @param search Từ khóa tìm kiếm (tên, mô tả). Có thể null hoặc rỗng.
     * @param statusFilter Trạng thái kỳ học để lọc. Có thể null hoặc rỗng.
     * @param startDate Ngày bắt đầu để lọc. Có thể null.
     * @param endDate Ngày kết thúc để lọc. Có thể null.
     * @param userId ID của người dùng sở hữu kỳ học.
     * @return Tổng số kỳ học thỏa mãn điều kiện.
     */
    public int countSemesters(String search, String statusFilter, Date startDate, Date endDate, int userId) {
        int total = 0;
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM Semesters WHERE userId = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (name LIKE ? OR description LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (statusFilter != null && !statusFilter.isEmpty()) {
            sqlBuilder.append(" AND status = ?");
            params.add(statusFilter);
        }

        if (startDate != null) {
            sqlBuilder.append(" AND startDate >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            sqlBuilder.append(" AND endDate <= ?");
            params.add(endDate);
        }

        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return total;
    }
    
    /**
     * Lấy một kỳ học dựa trên tên và userId.
     *
     * @param name Tên của kỳ học.
     * @param userId ID của người dùng sở hữu kỳ học.
     * @return Đối tượng Semester nếu tìm thấy, ngược lại trả về null.
     */
    public Semester getSemesterByName(String name, int userId) {
        Semester semester = null;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(SELECT_SEMESTER_BY_NAME_SQL)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    semester = extractSemesterFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return semester;
    }

    /**
     * Lấy kỳ học mới nhất (dựa trên startDate DESC) của một người dùng cụ thể.
     *
     * @param userId ID của người dùng.
     * @return Đối tượng Semester mới nhất, hoặc null nếu không tìm thấy.
     */
    public Semester getLatestSemester(int userId) {
        Semester latestSemester = null;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(SELECT_SEMESTER_LASTEST_SQL)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    latestSemester = extractSemesterFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return latestSemester;
    }

    /**
     * Lấy một kỳ học dựa trên tên và userId, loại trừ một ID cụ thể. Dùng để
     * kiểm tra trùng tên khi chỉnh sửa (không trùng với chính nó).
     *
     * @param name Tên của kỳ học.
     * @param id ID của kỳ học cần loại trừ.
     * @param userId ID của người dùng sở hữu kỳ học.
     * @return Đối tượng Semester nếu tìm thấy (có trùng tên với kỳ học khác),
     * ngược lại trả về null.
     */
    public Semester getSemesterByNameExceptId(String name, int id, int userId) {
        Semester semester = null;
        try (Connection con = getConnection();             PreparedStatement preparedStatement = con.prepareStatement(SELECT_SEMESTER_BY_NAME_EXCEPT_ID_SQL)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id); // Loại trừ ID này
            preparedStatement.setInt(3, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    semester = extractSemesterFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return semester;
    }

    /**
     * Trích xuất thông tin Semester từ ResultSet.
     *
     * @param rs ResultSet chứa dữ liệu kỳ học.
     * @return Đối tượng Semester.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private Semester extractSemesterFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Date startDate = rs.getDate("startDate");
        Date endDate = rs.getDate("endDate");
        String description = rs.getString("description");
        String status = rs.getString("status");
        LocalDateTime createdAt = rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null;
        LocalDateTime updatedAt = rs.getTimestamp("updatedAt") != null ? rs.getTimestamp("updatedAt").toLocalDateTime() : null;
        int userId = rs.getInt("userId");
        return new Semester(id, name, startDate, endDate, status, createdAt, updatedAt, description, userId);
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
