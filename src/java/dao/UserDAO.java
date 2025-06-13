package dao;

import DTO.User.UserStatistics;
import dal.DBContext;
import entity.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // Sử dụng java.sql.Date cho cột Date trong DB
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends DBContext {

    // --- SQL Constants ---
    // Sử dụng hằng số cho tất cả các câu lệnh SQL để dễ quản lý và tránh lỗi
    private static final String INSERT_USER_SQL = "INSERT INTO Users (username, email, password, firstName, lastName, role, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_USER_BY_ID_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users WHERE id = ?";
    private static final String SELECT_ALL_USERS_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users";
    private static final String DELETE_USER_SQL = "DELETE FROM Users WHERE id = ?";
    private static final String UPDATE_USER_PROFILE_SQL = "UPDATE Users SET firstName = ?, lastName = ?, role = ? WHERE id = ?";
    private static final String UPDATE_USER_PASSWORD_SQL = "UPDATE Users SET password = ? WHERE id = ?"; // Tách riêng cập nhật mật khẩu
    private static final String SELECT_USER_BY_USERNAME_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users WHERE username = ?";
    private static final String CHECK_USERNAME_EXISTS_SQL = "SELECT COUNT(*) FROM Users WHERE username = ?";
    private static final String CHECK_EMAIL_EXISTS_SQL = "SELECT COUNT(*) FROM Users WHERE email = ?";
    private static final String SELECT_USER_BY_USERNAME_OR_EMAIL_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users WHERE username = ? OR email = ?";
    private static final String COUNT_NEW_USERS_THIS_MONTH_SQL = "SELECT COUNT(*) FROM Users WHERE createdAt >= ?";
    private static final String COUNT_ADMIN_USERS_SQL = "SELECT COUNT(*) FROM Users WHERE role = 'Admin'";
    private static final String COUNT_TOTAL_USERS_SQL = "SELECT COUNT(*) FROM Users";

    /**
     * Kiểm tra xem tên đăng nhập đã tồn tại trong cơ sở dữ liệu chưa.
     *
     * @param username Tên đăng nhập cần kiểm tra.
     * @return true nếu tên đăng nhập tồn tại, ngược lại là false.
     */
    public boolean usernameExists(String username) {
        boolean exists = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(CHECK_USERNAME_EXISTS_SQL)) {
            preparedStatement.setString(1, username);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // In lỗi ra console, có thể thay bằng logging framework
        }
        return exists;
    }

    /**
     * Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu chưa.
     *
     * @param email Email cần kiểm tra.
     * @return true nếu email tồn tại, ngược lại là false.
     */
    public boolean emailExists(String email) {
        boolean exists = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(CHECK_EMAIL_EXISTS_SQL)) {
            preparedStatement.setString(1, email);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // In lỗi ra console, có thể thay bằng logging framework
        }
        return exists;
    }

    /**
     * Thêm một người dùng mới vào cơ sở dữ liệu.
     * Mật khẩu của người dùng NÊN được hash trước khi gọi phương thức này.
     *
     * @param user Đối tượng User chứa thông tin người dùng mới.
     * @return Số lượng hàng bị ảnh hưởng (thường là 1 nếu thành công), hoặc 0 nếu không thành công.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình truy vấn cơ sở dữ liệu.
     */
    public int insertUser(User user) throws SQLException {
        int result = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword()); // Mật khẩu đã được HASH
            preparedStatement.setString(4, user.getFirstName());
            preparedStatement.setString(5, user.getLastName());
            preparedStatement.setString(6, user.getRole());
            preparedStatement.setDate(7, new java.sql.Date(System.currentTimeMillis())); // Lưu thời gian tạo

            result = preparedStatement.executeUpdate();

            // Lấy ID tự động tạo (nếu có)
            if (result > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } // try-with-resources sẽ tự động đóng preparedStatement
        return result;
    }

    /**
     * Lấy thông tin người dùng từ cơ sở dữ liệu bằng ID.
     *
     * @param id ID của người dùng.
     * @return Đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy.
     */
    public User selectUserById(int id) {
        User user = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    user = extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return user;
    }

    /**
     * Lấy thông tin người dùng từ cơ sở dữ liệu bằng tên đăng nhập.
     * Thường dùng cho quá trình đăng nhập hoặc kiểm tra trùng lặp.
     *
     * @param username Tên đăng nhập của người dùng.
     * @return Đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy.
     */
    public User selectUserByUsername(String username) {
        User user = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_USERNAME_SQL)) {
            preparedStatement.setString(1, username);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    user = extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return user;
    }
    
    /**
     * Lấy thông tin người dùng từ cơ sở dữ liệu bằng tên đăng nhập hoặc email.
     * Thường dùng cho quá trình đăng nhập để hỗ trợ cả tên đăng nhập và email.
     *
     * @param identifier Tên đăng nhập hoặc email của người dùng.
     * @return Đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy.
     */
    public User selectUserByUsernameOrEmail(String identifier) {
        User user = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_USERNAME_OR_EMAIL_SQL)) {
            preparedStatement.setString(1, identifier);
            preparedStatement.setString(2, identifier); // Dùng cùng một giá trị cho cả username và email
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    user = extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return user;
    }

    /**
     * Lấy tất cả người dùng từ cơ sở dữ liệu.
     * Cần cẩn thận khi sử dụng phương thức này vì nó có thể trả về mật khẩu (đã hash).
     *
     * @return Danh sách các đối tượng User.
     */
    public List<User> selectAllUsers() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS_SQL)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return users;
    }

    /**
     * Cập nhật thông tin hồ sơ của người dùng (không bao gồm mật khẩu).
     *
     * @param user Đối tượng User chứa thông tin cần cập nhật (ID là bắt buộc).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public boolean updateUserProfile(User user) {
        boolean rowUpdated = false;
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_USER_PROFILE_SQL)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getRole());
            statement.setInt(4, user.getId());

            rowUpdated = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowUpdated;
    }

    /**
     * Cập nhật mật khẩu của người dùng.
     * Mật khẩu MỚI NÊN được hash trước khi gọi phương thức này.
     *
     * @param userId ID của người dùng cần cập nhật mật khẩu.
     * @param newHashedPassword Mật khẩu mới (đã được hash).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public boolean updateUserPassword(int userId, String newHashedPassword) {
        boolean rowUpdated = false;
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_USER_PASSWORD_SQL)) {
            statement.setString(1, newHashedPassword);
            statement.setInt(2, userId);

            rowUpdated = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowUpdated;
    }

    /**
     * Xóa một người dùng khỏi cơ sở dữ liệu bằng ID.
     *
     * @param id ID của người dùng cần xóa.
     * @return true nếu xóa thành công, ngược lại là false.
     */
    public boolean deleteUser(int id) {
        boolean rowDeleted = false;
        try (PreparedStatement statement = connection.prepareStatement(DELETE_USER_SQL)) {
            statement.setInt(1, id);
            rowDeleted = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e);
        }
        return rowDeleted;
    }

    /**
     * Lấy các số liệu thống kê người dùng từ cơ sở dữ liệu.
     * Bao gồm: số người dùng mới trong tháng này, số người dùng admin,
     * tổng số người dùng đang hoạt động và tổng số người dùng.
     *
     * @return Đối tượng UserStatistics chứa các số liệu thống kê.
     * Trả về một đối tượng UserStatistics với tất cả giá trị là 0 nếu có lỗi.
     */
    public UserStatistics getUserStatistics() {
        int newUsersThisMonth = 0;
        int adminUsers = 0;
        int activeUsers = 0; // Giả sử activeUsers = totalUsers nếu không có cột trạng thái
        int totalUsers = 0;

        // Lấy ngày đầu tiên của tháng hiện tại
        LocalDate firstDayOfCurrentMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        java.sql.Date sqlDateFirstDayOfMonth = java.sql.Date.valueOf(firstDayOfCurrentMonth);

        try {
            // 1. Get total users
            try (PreparedStatement ps = connection.prepareStatement(COUNT_TOTAL_USERS_SQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalUsers = rs.getInt(1);
                }
            }
            activeUsers = totalUsers; // Giả sử activeUsers là tổng số người dùng nếu không có điều kiện khác

            // 2. Get new users this month
            try (PreparedStatement ps = connection.prepareStatement(COUNT_NEW_USERS_THIS_MONTH_SQL)) {
                ps.setDate(1, sqlDateFirstDayOfMonth);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newUsersThisMonth = rs.getInt(1);
                    }
                }
            }

            // 3. Get admin users
            try (PreparedStatement ps = connection.prepareStatement(COUNT_ADMIN_USERS_SQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    adminUsers = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            printSQLException(e);
            // Có thể log lỗi và trả về UserStatistics mặc định hoặc ném ngoại lệ tùy logic ứng dụng
        }
        return new UserStatistics(newUsersThisMonth, adminUsers, activeUsers, totalUsers);
    }
    
        public List<User> getFilteredAndPaginatedUsers(String search, String roleFilter, String sortOrder, int offset, int limit) {
        List<User> users = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, firstName, lastName, username, email, role, createdAt FROM Users WHERE 1=1");

        List<Object> params = new ArrayList<>();

        // Thêm điều kiện tìm kiếm
        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (firstName LIKE ? OR lastName LIKE ? OR email LIKE ? OR username LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

        // Thêm điều kiện lọc theo vai trò
        if (roleFilter != null && !roleFilter.isEmpty()) {
            sqlBuilder.append(" AND role = ?");
            params.add(roleFilter);
        }

        // Thêm sắp xếp
        switch (sortOrder) {
            case "createdAt_desc":
                sqlBuilder.append(" ORDER BY createdAt DESC");
                break;
            case "createdAt_asc":
                sqlBuilder.append(" ORDER BY createdAt ASC");
                break;
            case "firstName_asc":
                sqlBuilder.append(" ORDER BY firstName ASC");
                break;
            case "firstName_desc":
                sqlBuilder.append(" ORDER BY firstName DESC");
                break;
            default:
                sqlBuilder.append(" ORDER BY createdAt DESC"); // Mặc định sắp xếp theo ngày tạo giảm dần
                break;
        }

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
                    users.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return users;
    }

    // Phương thức để lấy tổng số người dùng (để tính tổng số trang)
    public int getTotalUserCount(String search, String roleFilter) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(id) FROM Users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Thêm điều kiện tìm kiếm
        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (firstName LIKE ? OR lastName LIKE ? OR email LIKE ? OR username LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

        // Thêm điều kiện lọc theo vai trò
        if (roleFilter != null && !roleFilter.isEmpty()) {
            sqlBuilder.append(" AND role = ?");
            params.add(roleFilter);
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
     * Trích xuất thông tin người dùng từ một ResultSet và tạo đối tượng User.
     * Giúp tránh trùng lặp code khi đọc dữ liệu từ ResultSet.
     *
     * @param rs ResultSet chứa dữ liệu người dùng.
     * @return Đối tượng User đã được tạo.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        //String password = rs.getString("password");
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String role = rs.getString("role");
        Date createdAt = rs.getDate("createdAt");
        return new User(id, username, email, firstName, lastName, role, createdAt);
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
}