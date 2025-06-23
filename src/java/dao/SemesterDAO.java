package dao;

import dal.DBContext;
import entity.Semester;
import java.sql.Date;
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
 *
 * @author Dung Ann
 */
public class SemesterDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(SemesterDAO.class.getName());
    private static final String SELECT_ALL_SEMESTERS_SQL = "SELECT * FROM Semesters WHERE userId = ?";
    private static final String SELECT_SEMESTER_BY_ID_SQL = "SELECT id, name, startDate, endDate, description, status, createdAt, updatedAt, userId FROM Semesters WHERE id = ? AND userId = ?";
    private static final String INSERT_SEMESTER_SQL = "INSERT INTO Semesters (name, startDate, endDate, description, status, createdAt, updatedAt, userId) VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE(), ?)";
    private static final String UPDATE_SEMESTER_SQL = "UPDATE Semesters SET name = ?, startDate = ?, endDate = ?, description = ?, status = ?, updatedAt = GETDATE() WHERE id = ? AND userId = ?";
    private static final String DELETE_SEMESTER_SQL = "DELETE FROM Semesters WHERE id = ? AND userId = ?";
    private static final String SELECT_SEMESTER_BY_NAME_SQL = "SELECT id, name, startDate, endDate, description, status, createdAt, updatedAt, userId FROM Semesters WHERE name = ? AND userId = ?";
    private static final String SELECT_SEMESTER_BY_NAME_EXCEPT_ID_SQL = "SELECT id, name, startDate, endDate, description, status, createdAt, updatedAt, userId FROM Semesters WHERE name = ? AND id != ? AND userId = ?";
    private static final String SELECT_SEMESTER_LASTEST = "SELECT TOP 1 * FROM Semesters WHERE userId = ? ORDER BY startDate DESC, createdAt DESC";
    
    /**
     * Lấy tất cả các kỳ học từ cơ sở dữ liệu.
     *
     * @return Danh sách các dối tượng Semester.
     */
    public List<Semester> selectAllSemesters(int userId) {
        List<Semester> semesters = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_SEMESTERS_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
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
     * Lấy một kỳ học dựa trên ID và userId.
     *
     * @param id ID của kỳ học.
     * @param userId ID của người dùng sở hữu kỳ học.
     * @return Đối tượng Semester nếu tìm thấy, ngược lại trả về null.
     */
    public Semester getSemesterById(int id, int userId) {
        Semester semester = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SEMESTER_BY_ID_SQL)) {
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
     * Lấy một kỳ học dựa trên tên và userId.
     *
     * @param name Tên của kỳ học.
     * @param userId ID của người dùng sở hữu kỳ học.
     * @return Đối tượng Semester nếu tìm thấy, ngược lại trả về null.
     */
    public Semester getSemester(String name, int userId) {
        Semester semester = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SEMESTER_BY_NAME_SQL)) {
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
        // Sử dụng ORDER BY startDate DESC, created_at DESC để đảm bảo lấy đúng kỳ học mới nhất
        // LIMIT 1 (hoặc TOP 1 cho SQL Server) để chỉ lấy 1 bản ghi
        try (PreparedStatement ps = connection.prepareStatement(SELECT_SEMESTER_LASTEST)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SEMESTER_BY_NAME_EXCEPT_ID_SQL)) {
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
     * Thêm một kỳ học mới vào cơ sở dữ liệu.
     *
     * @param semester Đối tượng Semester cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean insertSemester(Semester semester) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, semester.getName());
            preparedStatement.setDate(2, semester.getStartDate());
            preparedStatement.setDate(3, semester.getEndDate());
            preparedStatement.setString(4, semester.getDescription());
            preparedStatement.setString(5, semester.getStatus());
            preparedStatement.setInt(6, semester.getUserId());

            int affectedRows = preparedStatement.executeUpdate();

            // Lấy ID tự động tăng nếu cần
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        semester.setId(generatedKeys.getInt(1)); // Cập nhật ID cho đối tượng Semester
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
     * Cập nhật thông tin một kỳ học trong cơ sở dữ liệu.
     *
     * @param semester Đối tượng Semester chứa thông tin cần cập nhật (phải có
     * ID).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateSemester(Semester semester) {
        boolean rowUpdated = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SEMESTER_SQL)) {
            preparedStatement.setString(1, semester.getName());
            preparedStatement.setDate(2, semester.getStartDate());
            preparedStatement.setDate(3, semester.getEndDate());
            preparedStatement.setString(4, semester.getDescription());
            preparedStatement.setString(5, semester.getStatus());
            preparedStatement.setInt(6, semester.getId());
            preparedStatement.setInt(7, semester.getUserId()); // Đảm bảo chỉ cập nhật kỳ học của người dùng đó

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
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SEMESTER_SQL)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, userId);
            rowDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowDeleted;
    }

    // Phương thức để lấy tổng số kỳ học (để tính tổng số trang)
    public int getTotalSemesterCount(String search, String statusFilter, Date startDate, Date endDate, int userId) {
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
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

    // Phương thức để lấy danh sách kỳ học đã lọc và phân trang
    public List<Semester> getFilteredAndPaginatedSemesters(String search, String statusFilter, Date startDate, Date endDate, int offset, int limit, int userId) {
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

        // Thêm điều kiện lọc theo trạng thái
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sqlBuilder.append(" AND status = ?");
            params.add(statusFilter);
        }

        // Thêm điều kiện lọc theo khoảng thời gian bắt đầu
        if (startDate != null) {
            sqlBuilder.append(" AND startDate >= ?");
            params.add(startDate);
        }

        // Thêm điều kiện lọc theo khoảng thời gian kết thúc
        if (endDate != null) {
            sqlBuilder.append(" AND endDate <= ?");
            params.add(endDate);
        }

        sqlBuilder.append(" ORDER BY startDate DESC"); // Mặc định sắp xếp theo ngày tạo giảm dần

        // Thêm phân trang (cho SQL Server, sử dụng OFFSET-FETCH)
        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
            // Gán các tham số
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
        // Convert SQL Timestamp to LocalDateTime
        LocalDateTime createdAt = rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null;
        LocalDateTime updatedAt = rs.getTimestamp("updatedAt") != null ? rs.getTimestamp("updatedAt").toLocalDateTime() : null;
        int userId = rs.getInt("userId");
        return new Semester(id, name, startDate, endDate, status, createdAt, updatedAt, description, userId);
    }

    /**
     * Hàm tiện ích để in chi tiết lỗi SQL ra System.err. Trong ứng dụng thực
     * tế, nên sử dụng một logging framework (ví dụ: Log4j, SLF4J).
     *
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
