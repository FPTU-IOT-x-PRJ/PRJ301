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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Lớp DAO quản lý các thao tác CRUD và các truy vấn liên quan đến đối tượng User trong cơ sở dữ liệu.
 * Author: Dung Ann
 */
public class UserDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    // --- Hằng số SQL ---
    private static final String INSERT_USER_SQL = "INSERT INTO Users (username, email, password, firstName, lastName, role, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_USER_BY_ID_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users WHERE id = ?";
    private static final String SELECT_ALL_USERS_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users";
    private static final String DELETE_USER_SQL = "DELETE FROM Users WHERE id = ?";
    private static final String UPDATE_USER_PROFILE_SQL = "UPDATE Users SET firstName = ?, lastName = ?, role = ? WHERE id = ?";
    private static final String UPDATE_USER_PASSWORD_SQL = "UPDATE Users SET password = ? WHERE id = ?";
    private static final String SELECT_USER_BY_USERNAME_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users WHERE username = ?";
    private static final String CHECK_USERNAME_EXISTS_SQL = "SELECT COUNT(*) FROM Users WHERE username = ?";
    private static final String CHECK_EMAIL_EXISTS_SQL = "SELECT COUNT(*) FROM Users WHERE email = ?";
    private static final String SELECT_USER_BY_USERNAME_OR_EMAIL_SQL = "SELECT id, username, email, password, firstName, lastName, role, createdAt FROM Users WHERE username = ? OR email = ?";
    private static final String COUNT_NEW_USERS_THIS_MONTH_SQL = "SELECT COUNT(*) FROM Users WHERE createdAt >= ?";
    private static final String COUNT_ADMIN_USERS_SQL = "SELECT COUNT(*) FROM Users WHERE role = 'Admin'";
    private static final String COUNT_TOTAL_USERS_SQL = "SELECT COUNT(*) FROM Users";

    /**
     * Thêm một người dùng mới vào cơ sở dữ liệu.
     * Mật khẩu của người dùng NÊN được hash trước khi gọi phương thức này.
     *
     * @param user Đối tượng User chứa thông tin người dùng mới.
     * @return true nếu thêm thành công, ngược lại là false.
     */
    public boolean addUser(User user) {
        boolean rowInserted = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword()); // Mật khẩu đã được HASH
            preparedStatement.setString(4, user.getFirstName());
            preparedStatement.setString(5, user.getLastName());
            preparedStatement.setString(6, user.getRole());
            preparedStatement.setDate(7, new java.sql.Date(System.currentTimeMillis())); // Lưu thời gian tạo

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1)); // Cập nhật ID cho đối tượng User
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
     * Lấy thông tin người dùng từ cơ sở dữ liệu bằng ID.
     *
     * @param id ID của người dùng.
     * @return Đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy.
     */
    public User getUserById(int id) {
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
     * Lấy tất cả người dùng từ cơ sở dữ liệu.
     * Phương thức này có thể được lọc và phân trang.
     *
     * @param search Từ khóa tìm kiếm (firstName, lastName, email, username). Có thể null hoặc rỗng.
     * @param roleFilter Vai trò để lọc (ví dụ: "Admin", "User"). Có thể null hoặc rỗng.
     * @param sortOrder Thứ tự sắp xếp (ví dụ: "createdAt_desc", "firstName_asc").
     * @param offset Vị trí bắt đầu của kết quả (cho phân trang).
     * @param limit Số lượng kết quả tối đa mỗi trang.
     * @return Danh sách các đối tượng User.
     */
    public List<User> getAllUsers(String search, String roleFilter, String sortOrder, int offset, int limit) {
        List<User> users = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, firstName, lastName, username, email, role, createdAt FROM Users WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (firstName LIKE ? OR lastName LIKE ? OR email LIKE ? OR username LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if (roleFilter != null && !roleFilter.isEmpty()) {
            sqlBuilder.append(" AND role = ?");
            params.add(roleFilter);
        }

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
                sqlBuilder.append(" ORDER BY createdAt DESC"); // Mặc định
                break;
        }

        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString())) {
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

    /**
     * Cập nhật thông tin hồ sơ của người dùng (không bao gồm mật khẩu).
     *
     * @param user Đối tượng User chứa thông tin cần cập nhật (ID là bắt buộc).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public boolean editUser(User user) {
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
    private static final String UPDATE_USER_PASSWORD_BY_EMAIL_SQL = 
    "UPDATE Users SET password = ? WHERE email = ?";

public boolean updatePasswordByEmail(String email, String newHashedPassword) {
    boolean rowUpdated = false;

    try (PreparedStatement statement = connection.prepareStatement(UPDATE_USER_PASSWORD_BY_EMAIL_SQL)) {
        statement.setString(1, newHashedPassword);
        statement.setString(2, email);

        rowUpdated = statement.executeUpdate() > 0;
        System.out.println("email"+email+"newHashedPassword"+newHashedPassword+"rowUpdated"+rowUpdated);
    } catch (SQLException e) {
        printSQLException(e); // hoặc e.printStackTrace();
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
    public boolean updatePassword(int userId, String newHashedPassword) {
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
     * Kiểm tra xem tên đăng nhập đã tồn tại trong cơ sở dữ liệu chưa.
     *
     * @param username Tên đăng nhập cần kiểm tra.
     * @return true nếu tên đăng nhập tồn tại, ngược lại là false.
     */
    public boolean isUsernameExists(String username) {
        boolean exists = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(CHECK_USERNAME_EXISTS_SQL)) {
            preparedStatement.setString(1, username);
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
     * Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu chưa.
     *
     * @param email Email cần kiểm tra.
     * @return true nếu email tồn tại, ngược lại là false.
     */
    public boolean isEmailExists(String email) {
        boolean exists = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement(CHECK_EMAIL_EXISTS_SQL)) {
            preparedStatement.setString(1, email);
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
     * Lấy thông tin người dùng từ cơ sở dữ liệu bằng tên đăng nhập hoặc email.
     * Thường dùng cho quá trình đăng nhập để hỗ trợ cả tên đăng nhập và email.
     *
     * @param identifier Tên đăng nhập hoặc email của người dùng.
     * @return Đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy.
     */
    public User getUserByUsernameOrEmail(String identifier) {
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
     * Xác thực người dùng bằng tên đăng nhập/email và mật khẩu.
     *
     * @param identifier Tên đăng nhập hoặc email.
     * @param password Mật khẩu thô.
     * @return Đối tượng User nếu xác thực thành công, ngược lại null.
     */
    public User authenticateUser(String identifier, String password) {
        User user = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_USERNAME_OR_EMAIL_SQL)) {
            preparedStatement.setString(1, identifier);
            preparedStatement.setString(2, identifier);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    String hashedPasswordFromDb = rs.getString("password");
                    if (BCrypt.checkpw(password, hashedPasswordFromDb)) {
                        user = extractUserFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return user;
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
        int activeUsers = 0;
        int totalUsers = 0;

        LocalDate firstDayOfCurrentMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        java.sql.Date sqlDateFirstDayOfMonth = java.sql.Date.valueOf(firstDayOfCurrentMonth);

        try {
            try (PreparedStatement ps = connection.prepareStatement(COUNT_TOTAL_USERS_SQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalUsers = rs.getInt(1);
                }
            }
            activeUsers = totalUsers; // Giả sử activeUsers là tổng số người dùng nếu không có điều kiện khác

            try (PreparedStatement ps = connection.prepareStatement(COUNT_NEW_USERS_THIS_MONTH_SQL)) {
                ps.setDate(1, sqlDateFirstDayOfMonth);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newUsersThisMonth = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(COUNT_ADMIN_USERS_SQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    adminUsers = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            printSQLException(e);
        }
        return new UserStatistics(newUsersThisMonth, adminUsers, activeUsers, totalUsers);
    }
    
    /**
     * Đếm tổng số người dùng dựa trên các tiêu chí lọc.
     *
     * @param search Từ khóa tìm kiếm (firstName, lastName, email, username). Có thể null hoặc rỗng.
     * @param roleFilter Vai trò để lọc. Có thể null hoặc rỗng.
     * @return Tổng số người dùng thỏa mãn điều kiện.
     */
    public int countUsers(String search, String roleFilter) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(id) FROM Users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sqlBuilder.append(" AND (firstName LIKE ? OR lastName LIKE ? OR email LIKE ? OR username LIKE ?)");
            String searchTerm = "%" + search + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

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
    
    /**
     * Trích xuất thông tin người dùng từ một ResultSet và tạo đối tượng User.
     *
     * @param rs ResultSet chứa dữ liệu người dùng.
     * @return Đối tượng User đã được tạo.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        // Mật khẩu không được trích xuất trực tiếp để tránh lộ thông tin
        // String password = rs.getString("password");
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String role = rs.getString("role");
        Date createdAt = rs.getDate("createdAt");
        return new User(id, username, email, firstName, lastName, role, createdAt);
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
