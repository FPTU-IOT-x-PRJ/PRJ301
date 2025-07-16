package dal;

import utils.ConfigManager;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBContext {

    private static final Logger LOGGER = Logger.getLogger(DBContext.class.getName());
    // Loại bỏ hoàn toàn trường protected Connection connection;
    private ConfigManager configManager;

    public DBContext() {
        configManager = ConfigManager.getInstance();
        LOGGER.log(Level.INFO, "DBContext instance created.");
    }

    // Phương thức này giờ sẽ CHỈ chịu trách nhiệm tạo và trả về một Connection mới.
    // Kết nối này sẽ được đóng bởi try-with-resources trong lớp gọi.
    public Connection getConnection() throws SQLException {
        Connection con = null; // Khởi tạo con là null
        try {
            String url = configManager.getProperty("db.url");
            String username = configManager.getProperty("db.username");
            String password = configManager.getProperty("db.password");

            if (url == null || username == null || password == null) {
                LOGGER.log(Level.SEVERE, "Missing database configuration information in .env file.");
                throw new RuntimeException("Missing database configuration information in .env file");
            }

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(url, username, password);
//            LOGGER.log(Level.INFO, "New database connection successfully acquired.");
            return con;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQL Server JDBC Driver not found", e);
            throw new SQLException("SQL Server JDBC Driver does not exist", e); // Ném SQLException để DAO bắt và ghi log
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to database: " + e.getMessage(), e);
            // Quan trọng: Đóng kết nối nếu nó được mở trước khi xảy ra ngoại lệ SQL khác
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Error closing connection after failed acquisition.", ex);
                }
            }
            throw e; // Ném lại SQLException
        }
    }

    public void closeConnection() {
        LOGGER.log(Level.INFO, "closeConnection() called in DBContext. No shared connection to close here.");
    }
}
