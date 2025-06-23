package dal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBInitializer {
    private static final Logger LOGGER = Logger.getLogger(DBInitializer.class.getName());
    private DBContext dbContext;

    public DBInitializer() {
        this.dbContext = new DBContext();
    }

    /**
     * Kiểm tra xem một bảng có tồn tại trong cơ sở dữ liệu hay không.
     * @param tableName Tên của bảng cần kiểm tra.
     * @return true nếu bảng tồn tại, ngược lại là false.
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        // Lấy kết quả từ getTables và kiểm tra tên bảng
        // Các đối số: catalog, schemaPattern, tableNamePattern, types
        try (var rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * Thực hiện xóa một bảng.
     * @param conn Đối tượng Connection.
     * @param tableName Tên của bảng cần xóa.
     */
    private void dropTable(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement()) {
            String sql = "DROP TABLE IF EXISTS " + tableName;
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table {0} dropped successfully.", tableName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error dropping table " + tableName, e);
        }
    }

    /**
     * Tạo bảng User nếu nó chưa tồn tại.
     * @param conn Đối tượng Connection.
     */
    private void createUserTable(Connection conn) {
        String sql = "CREATE TABLE [User] (\n" +
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    username VARCHAR(50) NOT NULL UNIQUE,\n" +
                     "    email VARCHAR(100) NOT NULL UNIQUE,\n" +
                     "    password VARCHAR(255) NOT NULL,\n" +
                     "    firstName NVARCHAR(50),\n" +
                     "    lastName NVARCHAR(50),\n" +
                     "    role VARCHAR(20) NOT NULL DEFAULT 'user',\n" +
                     "    createdAt DATE DEFAULT GETDATE()\n" +
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'User' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating User table", e);
        }
    }
    
    /**
     * Tạo bảng Semester nếu nó chưa tồn tại.
     * @param conn Đối tượng Connection.
     */
    private void createSemesterTable(Connection conn) {
        String sql = "CREATE TABLE Semester (\n" +
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    name NVARCHAR(100) NOT NULL,\n" +
                     "    startDate DATE NOT NULL,\n" +
                     "    endDate DATE NOT NULL,\n" +
                     "    description NVARCHAR(MAX),\n" +
                     "    status VARCHAR(50) NOT NULL,\n" +
                     "    createdAt DATETIME DEFAULT GETDATE(),\n" +
                     "    updatedAt DATETIME DEFAULT GETDATE(),\n" +
                     "    userId INT,\n" +
                     "    FOREIGN KEY (userId) REFERENCES [User](id)\n" +
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'Semester' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Semester table", e);
        }
    }

    /**
     * Tạo bảng Subject nếu nó chưa tồn tại.
     * @param conn Đối tượng Connection.
     */
    private void createSubjectTable(Connection conn) {
        String sql = "CREATE TABLE Subject (\n" +
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    semesterId INT NOT NULL,\n" +
                     "    name NVARCHAR(255) NOT NULL,\n" +
                     "    code VARCHAR(50) NOT NULL UNIQUE,\n" +
                     "    description NVARCHAR(MAX),\n" +
                     "    credits INT NOT NULL,\n" +
                     "    teacherName NVARCHAR(100),\n" +
                     "    isActive BIT DEFAULT 1,\n" +
                     "    prerequisites NVARCHAR(MAX),\n" +
                     "    createdAt DATETIME DEFAULT GETDATE(),\n" +
                     "    updatedAt DATETIME DEFAULT GETDATE(),\n" +
                     "    FOREIGN KEY (semesterId) REFERENCES Semester(id)\n" +
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'Subject' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Subject table", e);
        }
    }

    /**
     * Tạo bảng Lesson nếu nó chưa tồn tại.
     * @param conn Đối tượng Connection.
     */
    private void createLessonTable(Connection conn) {
        String sql = "CREATE TABLE Lesson (\n" +
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    subjectId INT NOT NULL,\n" +
                     "    name NVARCHAR(255) NOT NULL,\n" +
                     "    lessonDate DATE NOT NULL,\n" +
                     "    description NVARCHAR(MAX),\n" +
                     "    status VARCHAR(50) NOT NULL,\n" +
                     "    createdAt DATETIME DEFAULT GETDATE(),\n" +
                     "    updatedAt DATETIME DEFAULT GETDATE(),\n" +
                     "    FOREIGN KEY (subjectId) REFERENCES Subject(id)\n" +
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'Lesson' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Lesson table", e);
        }
    }

    /**
     * Khởi tạo cơ sở dữ liệu: kiểm tra và tạo/xóa/tạo lại các bảng.
     * Nếu enforceReset là true, tất cả các bảng sẽ bị xóa và tạo lại.
     * Nếu không, chỉ những bảng thiếu mới được tạo.
     * @param enforceReset Nếu true, sẽ xóa tất cả các bảng hiện có và tạo lại.
     */
    public void initializeDatabase(boolean enforceReset) {
        try (Connection conn = dbContext.getConnection()) {
            if (conn == null) {
                LOGGER.log(Level.SEVERE, "Database connection is null. Cannot initialize database.");
                return;
            }

            // Danh sách các bảng theo thứ tự tạo (để đảm bảo FOREIGN KEY)
            String[] tableNames = {"Lesson", "Subject", "Semester", "[User]"};

            if (enforceReset) {
                LOGGER.log(Level.INFO, "Enforce reset is true. Dropping all tables...");
                // Xóa bảng theo thứ tự ngược lại để tránh lỗi FOREIGN KEY
                for (int i = tableNames.length - 1; i >= 0; i--) {
                    dropTable(conn, tableNames[i]);
                }
            }

            // Tạo các bảng
            LOGGER.log(Level.INFO, "Creating tables...");
            createUserTable(conn);
            createSemesterTable(conn);
            createSubjectTable(conn);
            createLessonTable(conn);

            LOGGER.log(Level.INFO, "Database initialization completed.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed.", e);
        }
    }
    
    // Phương thức main để kiểm tra
    public static void main(String[] args) {
        DBInitializer initializer = new DBInitializer();
        // Để reset hoàn toàn DB (xóa và tạo lại tất cả bảng), truyền true
        // Để chỉ tạo các bảng thiếu, truyền false
        initializer.initializeDatabase(true); 
    }
}