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
    
    // Phương thức để lấy tổng số kỳ học (để tính tổng số trang)
    public int getTotalSemesterCount(String search, String statusFilter, Date startDate, Date endDate, int userId) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(id) FROM Semesters WHERE userId = ?");
        List<Object> params = new ArrayList<>();

        params.add(userId);
        // Thêm điều kiện tìm kiếm theo tên kỳ học
        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND name LIKE ?");
            String searchTerm = "%" + search + "%";
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
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
    
    
    // --- Helper Methods ---

    /**
     * Trích xuất thông tin kỳ học từ một ResultSet và tạo đối tượng Semester.
     * Giúp tránh trùng lặp code khi đọc dữ liệu từ ResultSet.
     *
     * @param rs ResultSet chứa dữ liệu kỳ học.
     * @return Đối tượng Semester đã được tạo.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private Semester extractSemesterFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Date startDate = rs.getDate("startDate");
        Date endDate = rs.getDate("endDate");
        String status = rs.getString("status");
        LocalDateTime updatedAt = rs.getTimestamp("updatedAt").toLocalDateTime();
        LocalDateTime createdAt = rs.getTimestamp("createdAt").toLocalDateTime();
        String description = rs.getString("description");
        int userId = rs.getInt("userId");
        return new Semester(id, name, startDate, endDate, status, createdAt, updatedAt, description, userId);
    }
    /**
     * Hàm tiện ích để in chi tiết lỗi SQL ra System.err.
     * Trong ứng dụng thực tế, nên sử dụng một logging framework (ví dụ: Log4j, SLF4J).
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
    
    private static final String GET_SEMESTER = 
    "SELECT * FROM Semesters WHERE name = ? AND userId = ?";
    
    public Semester getSemester(String name, int userId) {
        try (PreparedStatement ps = connection.prepareStatement(GET_SEMESTER)) {
            ps.setString(1, name);
            ps.setInt(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return extractSemesterFromResultSet(rs);
                }
            }            
        }
        catch (SQLException ex) {
            LOGGER.log(Level.INFO, "Lỗi khi thực hiện truy vấn");
        }
        
        return null;
    }
    
    private static final String INSERT_SEMESTER_SQL = 
    "INSERT INTO Semesters (name, startDate, endDate, status, createdAt, updatedAt, userId) VALUES (?, ?, ?, ?, GETDATE(), GETDATE(), ?)";

    public boolean insertSemester(Semester semester) {
        //System.out.println(semester.toString());
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SEMESTER_SQL)) {
            ps.setString(1, semester.getName());
            ps.setDate(2, (Date) semester.getStartDate());
            ps.setDate(3, (Date) semester.getEndDate());
            ps.setString(4, semester.getStatus());
            ps.setInt(5, semester.getUserId());
            return ps.executeUpdate() > 0;
        }
        catch (SQLException ex) {
            LOGGER.log(Level.INFO, "Lỗi khi thực hiện truy vấn");            
        }
        
        return false;
    }
    
    private static final String UPDATE_SEMESTER_SQL = 
        "UPDATE Semesters SET name = ?, startDate = ?, endDate = ?, status = ?, updatedAt = GETDATE() WHERE id = ?";

    public boolean updateSemester(Semester semester) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SEMESTER_SQL)) {
            ps.setString(1, semester.getName());
            ps.setDate(2, (Date) semester.getStartDate());
            ps.setDate(3, (Date) semester.getEndDate());
            ps.setString(4, semester.getStatus());
            ps.setInt(5, semester.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }
    
    private static final String DELETE_SEMESTER_SQL = "DELETE FROM Semesters WHERE id = ? AND userId = ?";

    public boolean deleteSemester(int id, int userId) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_SEMESTER_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }
    
    private static final String SELECT_SEMESTER_BY_ID_SQL = 
        "SELECT * FROM Semesters WHERE id = ? AND userId = ?";

    public Semester getSemesterById(int id, int userId) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_SEMESTER_BY_ID_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractSemesterFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return null;
    }
    
    public List<Semester> getFilteredAndPaginatedSemesters(String search, String statusFilter, 
                                                          Date startDate, Date endDate, 
                                                          int offset, int limit, int userId) {
        List<Semester> semesters = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM Semesters WHERE userId = ?");

        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        // Thêm điều kiện tìm kiếm theo tên
        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND name LIKE ?");
            String searchTerm = "%" + search + "%";
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
    
}
