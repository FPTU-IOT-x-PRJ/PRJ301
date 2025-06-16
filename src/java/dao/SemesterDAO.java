package dao;

import dal.DBContext;
import entity.Semester;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dung Ann
 */
public class SemesterDAO extends DBContext {

    private static final String SELECT_ALL_SEMESTERS_SQL = "SELECT id, name, startDate, endDate, status, createdAt,updatedAt FROM Semesters";

    /**
     * Lấy tất cả các kỳ học từ cơ sở dữ liệu. 
     *
     * @return Danh sách các dối tượng Semester.
     */
    public List<Semester> selectAllSemesters() {
        List<Semester> semesters = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SEMESTERS_SQL)) {
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
        return new Semester(id, name, startDate, endDate, status, createdAt, updatedAt);
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
    private static final String INSERT_SEMESTER_SQL = 
    "INSERT INTO Semesters (name, startDate, endDate, status, createdAt, updatedAt) VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";

public boolean insertSemester(Semester semester) {
    System.out.println(semester.toString());
    try (PreparedStatement ps = connection.prepareStatement(INSERT_SEMESTER_SQL)) {
        ps.setString(1, semester.getName());
        ps.setDate(2, (Date) semester.getStartDate());
        ps.setDate(3, (Date) semester.getEndDate());
        ps.setString(4, semester.getStatus());
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        printSQLException(e);
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
private static final String DELETE_SEMESTER_SQL = "DELETE FROM Semesters WHERE id = ?";

public boolean deleteSemester(int id) {
    try (PreparedStatement ps = connection.prepareStatement(DELETE_SEMESTER_SQL)) {
        ps.setInt(1, id);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        printSQLException(e);
    }
    return false;
}
private static final String SELECT_SEMESTER_BY_ID_SQL = 
    "SELECT id, name, startDate, endDate, status, createdAt, updatedAt FROM Semesters WHERE id = ?";

public Semester getSemesterById(int id) {
    try (PreparedStatement ps = connection.prepareStatement(SELECT_SEMESTER_BY_ID_SQL)) {
        ps.setInt(1, id);
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
}
