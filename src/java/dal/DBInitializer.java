package dal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Date; // Import Date cho createdAt, startDate, endDate
import java.time.LocalDate; // Dùng LocalDate để tạo ngày tháng dễ hơn
import java.time.format.DateTimeFormatter; // Để format ngày tháng nếu cần
import org.mindrot.jbcrypt.BCrypt;

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
        try (var rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            // Lấy schema của cơ sở dữ liệu hiện tại để kiểm tra chính xác hơn.
            // Trong SQL Server, schema mặc định thường là 'dbo'.
            // Nếu bạn không dùng schema cụ thể, có thể để null.
            // rs = meta.getTables(null, "dbo", tableName, new String[]{"TABLE"}) // Ví dụ cho SQL Server
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

// Trong lớp DBContext hoặc lớp tương tự nơi bạn định nghĩa các hàm tạo bảng

    /**
     * Tạo bảng Users nếu nó chưa tồn tại.
     * @param conn Đối tượng Connection.
     */
    private void createUsersTable(Connection conn) { 
        String sql = "CREATE TABLE Users (\n" + 
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
            LOGGER.log(Level.INFO, "Table 'Users' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Users table", e);
        }
    }
    
    /**
     * Tạo bảng Semesters nếu nó chưa tồn tại.
     * Khi một Users bị xóa, các Semesters thuộc về User đó cũng sẽ bị xóa.
     * @param conn Đối tượng Connection.
     */
    private void createSemestersTable(Connection conn) { 
        String sql = "CREATE TABLE Semesters (\n" + 
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    name NVARCHAR(100) NOT NULL,\n" +
                     "    startDate DATE NOT NULL,\n" +
                     "    endDate DATE NOT NULL,\n" +
                     "    description NVARCHAR(MAX),\n" +
                     "    status VARCHAR(50) NOT NULL,\n" +
                     "    createdAt DATETIME DEFAULT GETDATE(),\n" +
                     "    updatedAt DATETIME DEFAULT GETDATE(),\n" +
                     "    userId INT,\n" +
                     "    FOREIGN KEY (userId) REFERENCES Users(id) ON DELETE CASCADE\n" + 
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'Semesters' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Semesters table", e);
        }
    }

    /**
     * Tạo bảng Subjects nếu nó chưa tồn tại.
     * Khi một Semesters bị xóa, các Subjects thuộc về Semester đó cũng sẽ bị xóa.
     * @param conn Đối tượng Connection.
     */
    private void createSubjectsTable(Connection conn) { 
        String sql = "CREATE TABLE Subjects (\n" + 
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    semesterId INT NOT NULL,\n" +
                     "    name NVARCHAR(255) NOT NULL,\n" +
                     "    code VARCHAR(50) NOT NULL,\n" +
                     "    description NVARCHAR(MAX),\n" +
                     "    credits INT NOT NULL,\n" +
                     "    teacherName NVARCHAR(100),\n" +
                     "    isActive BIT DEFAULT 1,\n" +
                     "    prerequisites NVARCHAR(MAX),\n" +
                     "    createdAt DATETIME DEFAULT GETDATE(),\n" +
                     "    updatedAt DATETIME DEFAULT GETDATE(),\n" +
                     "    FOREIGN KEY (semesterId) REFERENCES Semesters(id) ON DELETE CASCADE\n" + 
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'Subjects' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Subjects table", e);
        }
    }

    /**
     * Tạo bảng Lessons nếu nó chưa tồn tại.
     * Khi một Subjects bị xóa, các Lessons thuộc về Subject đó cũng sẽ bị xóa.
     * @param conn Đối tượng Connection.
     */
    private void createLessonsTable(Connection conn) { 
        String sql = "CREATE TABLE Lessons (\n" + 
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    subjectId INT NOT NULL,\n" +
                     "    name NVARCHAR(255) NOT NULL,\n" +
                     "    lessonDate DATE NOT NULL,\n" +
                     "    description NVARCHAR(MAX),\n" +
                     "    status VARCHAR(50) NOT NULL,\n" +
                     "    createdAt DATETIME DEFAULT GETDATE(),\n" +
                     "    updatedAt DATETIME DEFAULT GETDATE(),\n" +
                     "    FOREIGN KEY (subjectId) REFERENCES Subjects(id) ON DELETE CASCADE\n" + 
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'Lessons' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Lessons table", e);
        }
    }

    /**
     * Tạo bảng Documents nếu nó chưa tồn tại.
     * Các bản ghi Documents sẽ bị xóa nếu User, Subject hoặc Lesson liên quan bị xóa.
     * @param conn Đối tượng Connection.
     */
    private void createDocumentsTable(Connection conn) {
        String sql = "CREATE TABLE Documents (\n" +
                     "    id INT PRIMARY KEY IDENTITY(1,1),\n" +
                     "    fileName NVARCHAR(255) NOT NULL,    \n" +
                     "    storedFileName VARCHAR(255) NOT NULL UNIQUE,\n" +
                     "    filePath NVARCHAR(MAX) NOT NULL,     \n" +
                     "    fileType VARCHAR(100),             \n" +
                     "    fileSize BIGINT,                     \n" +
                     "    uploadedBy INT,                      \n" +
                     "    uploadDate DATETIME DEFAULT GETDATE(), \n" +
                     "    description NVARCHAR(MAX), \n" +
                     "    subjectId INT,\n" +
                     "    lessonId INT,\n" +
                     ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.log(Level.INFO, "Table 'Documents' created successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating Documents table", e);
        }
    }

    /**
     * Thêm dữ liệu giả vào các bảng.
     * @param conn Đối tượng Connection.
     */
    private void insertFakeData(Connection conn) {
        // --- 1. Insert Admin User ---
        int adminUserId = -1;
        String adminUsername = "admin";
        String adminPasswordPlain = "123456"; 
        String hashedPassword = BCrypt.hashpw(adminPasswordPlain, BCrypt.gensalt(12));

        String insertUserSql = "INSERT INTO Users (username, email, password, firstName, lastName, role, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String selectUserIdSql = "SELECT id FROM Users WHERE username = ?"; // Để lấy ID của user vừa tạo

        try {
            // Kiểm tra và chèn Admin User
            if (!userExists(conn, adminUsername)) {
                try (PreparedStatement ps = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, adminUsername);
                    ps.setString(2, "admin@example.com");
                    ps.setString(3, hashedPassword);
                    ps.setString(4, "Admin");
                    ps.setString(5, "User");
                    ps.setString(6, "Admin");
                    ps.setDate(7, Date.valueOf(LocalDate.now())); 
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            adminUserId = rs.getInt(1);
                        }
                    }
                    LOGGER.log(Level.INFO, "Admin user '{0}' inserted successfully with ID: {1}", new Object[]{adminUsername, adminUserId});
                }
            } else {
                LOGGER.log(Level.INFO, "Admin user '{0}' already exists. Attempting to retrieve ID.", adminUsername);
                // Nếu admin đã tồn tại, lấy ID của nó
                try (PreparedStatement ps = conn.prepareStatement(selectUserIdSql)) {
                    ps.setString(1, adminUsername);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            adminUserId = rs.getInt("id");
                            LOGGER.log(Level.INFO, "Retrieved Admin user ID: {0}", adminUserId);
                        }
                    }
                }
            }

            if (adminUserId == -1) {
                LOGGER.log(Level.SEVERE, "Failed to get Admin user ID. Cannot insert dependent data.");
                return; // Không thể tiếp tục nếu không có Admin ID
            }

            // --- 2. Insert Semesters for Admin User ---
            String insertSemesterSql = "INSERT INTO Semesters (name, startDate, endDate, description, status, userId) VALUES (?, ?, ?, ?, ?, ?)";
            
            // Chỉ thêm nếu bảng Semesters rỗng để tránh trùng lặp khi initializeDatabase(false)
            if (countTableRows(conn, "Semesters") == 0) { 
                LOGGER.log(Level.INFO, "Inserting fake Semesters data...");
                try (PreparedStatement ps = conn.prepareStatement(insertSemesterSql, Statement.RETURN_GENERATED_KEYS)) {
                    // Semester 1: Fall 2024
                    ps.setString(1, "Fall 2024");
                    ps.setDate(2, Date.valueOf(LocalDate.of(2024, 9, 1)));
                    ps.setDate(3, Date.valueOf(LocalDate.of(2024, 12, 31)));
                    ps.setString(4, "Kỳ học mùa thu 2024.");
                    ps.setString(5, "Active");
                    ps.setInt(6, adminUserId);
                    ps.executeUpdate();
                    int fall2024Id = getGeneratedId(ps); // Lấy ID của Semester vừa tạo

                    // Semester 2: Spring 2025
                    ps.setString(1, "Spring 2025");
                    ps.setDate(2, Date.valueOf(LocalDate.of(2025, 1, 15)));
                    ps.setDate(3, Date.valueOf(LocalDate.of(2025, 5, 30)));
                    ps.setString(4, "Kỳ học mùa xuân 2025.");
                    ps.setString(5, "Completed");
                    ps.setInt(6, adminUserId);
                    ps.executeUpdate();
                    int spring2025Id = getGeneratedId(ps);

                    // Semester 3: Summer 2025 (Upcoming)
                    ps.setString(1, "Summer 2025");
                    ps.setDate(2, Date.valueOf(LocalDate.of(2025, 6, 15)));
                    ps.setDate(3, Date.valueOf(LocalDate.of(2025, 8, 30)));
                    ps.setString(4, "Kỳ học mùa hè 2025. Sắp diễn ra.");
                    ps.setString(5, "Inactive");
                    ps.setInt(6, adminUserId);
                    ps.executeUpdate();
                    int summer2025Id = getGeneratedId(ps);

                    LOGGER.log(Level.INFO, "Fake Semesters inserted successfully.");

                    // --- 3. Insert Subjects for Semesters ---
                    String insertSubjectSql = "INSERT INTO Subjects (semesterId, name, code, description, credits, teacherName, isActive, prerequisites) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    
                    // Chỉ thêm nếu bảng Subjects rỗng
                    if (countTableRows(conn, "Subjects") == 0) {
                        LOGGER.log(Level.INFO, "Inserting fake Subjects data...");
                        try (PreparedStatement psSubject = conn.prepareStatement(insertSubjectSql, Statement.RETURN_GENERATED_KEYS)) {
                            // Subjects for Fall 2024
                            psSubject.setInt(1, fall2024Id);
                            psSubject.setString(2, "Lập trình Web với Java");
                            psSubject.setString(3, "PRJ301");
                            psSubject.setString(4, "Môn học nền tảng về phát triển ứng dụng web sử dụng Java Servlet và JSP.");
                            psSubject.setInt(5, 3);
                            psSubject.setString(6, "Nguyen Van A");
                            psSubject.setBoolean(7, true);
                            psSubject.setString(8, "C# Programming");
                            psSubject.executeUpdate();
                            int prj301Id = getGeneratedId(psSubject);

                            psSubject.setInt(1, fall2024Id);
                            psSubject.setString(2, "Cơ sở dữ liệu nâng cao");
                            psSubject.setString(3, "CSD201");
                            psSubject.setString(4, "Nghiên cứu sâu về thiết kế và quản lý cơ sở dữ liệu.");
                            psSubject.setInt(5, 3);
                            psSubject.setString(6, "Le Thi B");
                            psSubject.setBoolean(7, true);
                            psSubject.setString(8, "Database Fundamentals");
                            psSubject.executeUpdate();
                            int csd201Id = getGeneratedId(psSubject);

                            // Subjects for Spring 2025
                            psSubject.setInt(1, spring2025Id);
                            psSubject.setString(2, "Phân tích và Thiết kế Hệ thống");
                            psSubject.setString(3, "SWP391");
                            psSubject.setString(4, "Tìm hiểu về quy trình phân tích và thiết kế phần mềm.");
                            psSubject.setInt(5, 4);
                            psSubject.setString(6, "Tran Van C");
                            psSubject.setBoolean(7, true);
                            psSubject.setString(8, "Software Engineering");
                            psSubject.executeUpdate();
                            int swp391Id = getGeneratedId(psSubject);

                            psSubject.setInt(1, spring2025Id);
                            psSubject.setString(2, "Trí tuệ Nhân tạo");
                            psSubject.setString(3, "AI201");
                            psSubject.setString(4, "Giới thiệu về các khái niệm và thuật toán AI cơ bản.");
                            psSubject.setInt(5, 3);
                            psSubject.setString(6, "Pham Thi D");
                            psSubject.setBoolean(7, false);
                            psSubject.setString(8, "Mathematics, Algorithms");
                            psSubject.executeUpdate();
                            int ai201Id = getGeneratedId(psSubject);
                            
                            // Subjects for Summer 2025
                            psSubject.setInt(1, summer2025Id);
                            psSubject.setString(2, "Phát triển Ứng dụng Di động");
                            psSubject.setString(3, "MOB401");
                            psSubject.setString(4, "Xây dựng ứng dụng cho thiết bị di động (Android/iOS).");
                            psSubject.setInt(5, 4);
                            psSubject.setString(6, "Nguyen Thi E");
                            psSubject.setBoolean(7, false);
                            psSubject.setString(8, "Java Programming");
                            psSubject.executeUpdate();
                            int mob401Id = getGeneratedId(psSubject);

                            LOGGER.log(Level.INFO, "Fake Subjects inserted successfully.");

                            // --- 4. Insert Lessons for Subjects ---
                            String insertLessonSql = "INSERT INTO Lessons (subjectId, name, lessonDate, description, status) VALUES (?, ?, ?, ?, ?)";
                            
                            // Chỉ thêm nếu bảng Lessons rỗng
                            if (countTableRows(conn, "Lessons") == 0) {
                                LOGGER.log(Level.INFO, "Inserting fake Lessons data...");
                                try (PreparedStatement psLesson = conn.prepareStatement(insertLessonSql)) {
                                    // Lessons for PRJ301 (fall2024Id)
                                    psLesson.setInt(1, prj301Id);
                                    psLesson.setString(2, "Giới thiệu Servlet và JSP");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2024, 9, 5)));
                                    psLesson.setString(4, "Buổi học đầu tiên về kiến trúc Web Java.");
                                    psLesson.setString(5, "Completed");
                                    psLesson.executeUpdate();

                                    psLesson.setInt(1, prj301Id);
                                    psLesson.setString(2, "Request-Response Model");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2024, 9, 12)));
                                    psLesson.setString(4, "Tìm hiểu cách Servlet xử lý request và response.");
                                    psLesson.setString(5, "Completed");
                                    psLesson.executeUpdate();
                                    
                                    psLesson.setInt(1, prj301Id);
                                    psLesson.setString(2, "Filters and Listeners");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2024, 9, 19)));
                                    psLesson.setString(4, "Khám phá Filters và Listeners trong Java Web.");
                                    psLesson.setString(5, "Active");
                                    psLesson.executeUpdate();

                                    // Lessons for CSD201 (fall2024Id)
                                    psLesson.setInt(1, csd201Id);
                                    psLesson.setString(2, "SQL Joins");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2024, 9, 10)));
                                    psLesson.setString(4, "Học về các loại JOIN trong SQL.");
                                    psLesson.setString(5, "Completed");
                                    psLesson.executeUpdate();

                                    psLesson.setInt(1, csd201Id);
                                    psLesson.setString(2, "Indexing and Optimization");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2024, 9, 17)));
                                    psLesson.setString(4, "Tối ưu hóa hiệu suất truy vấn CSDL.");
                                    psLesson.setString(5, "Active");
                                    psLesson.executeUpdate();

                                    // Lessons for SWP391 (spring2025Id)
                                    psLesson.setInt(1, swp391Id);
                                    psLesson.setString(2, "Yêu cầu phần mềm");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2025, 1, 20)));
                                    psLesson.setString(4, "Cách thu thập và phân tích yêu cầu từ khách hàng.");
                                    psLesson.setString(5, "Active");
                                    psLesson.executeUpdate();
                                    
                                    // Lessons for AI201 (spring2025Id)
                                    psLesson.setInt(1, ai201Id);
                                    psLesson.setString(2, "Giới thiệu học máy");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2025, 2, 10)));
                                    psLesson.setString(4, "Khái niệm cơ bản về Machine Learning.");
                                    psLesson.setString(5, "Active");
                                    psLesson.executeUpdate();
                                    
                                    // Lessons for MOB401 (summer2025Id)
                                    psLesson.setInt(1, mob401Id);
                                    psLesson.setString(2, "Giới thiệu Android Studio");
                                    psLesson.setDate(3, Date.valueOf(LocalDate.of(2025, 6, 20)));
                                    psLesson.setString(4, "Cài đặt và làm quen môi trường phát triển Android.");
                                    psLesson.setString(5, "Inactive");
                                    psLesson.executeUpdate();

                                    LOGGER.log(Level.INFO, "Fake Lessons inserted successfully.");
                                }
                            } else {
                                LOGGER.log(Level.INFO, "Lessons table is not empty. Skipping fake Lessons insertion.");
                            }

                        }
                    } else {
                        LOGGER.log(Level.INFO, "Subjects table is not empty. Skipping fake Subjects insertion.");
                    }

                }
            } else {
                LOGGER.log(Level.INFO, "Semesters table is not empty. Skipping fake Semesters insertion.");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting fake data into database.", e);
        }
    }

    /**
     * Helper method to get the ID of the last inserted row.
     * Works for IDENTITY columns in SQL Server.
     * @param ps PreparedStatement used for insertion with RETURN_GENERATED_KEYS.
     * @return The generated ID, or -1 if not found.
     * @throws SQLException 
     */
    private int getGeneratedId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Kiểm tra xem một người dùng có tồn tại dựa trên username hay không.
     * @param conn Đối tượng Connection.
     * @param username Tên người dùng cần kiểm tra.
     * @return true nếu người dùng tồn tại, ngược lại là false.
     */
    private boolean userExists(Connection conn, String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user exists: " + username, e);
        }
        return false;
    }
    
    /**
     * Đếm số hàng trong một bảng.
     * @param conn Đối tượng Connection.
     * @param tableName Tên bảng.
     * @return Số hàng trong bảng.
     */
    private int countTableRows(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting rows in table " + tableName, e);
        }
        return 0;
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

            // Đảm bảo thứ tự drop và create chính xác theo phụ thuộc khóa ngoại
            String[] tableNames = {"Documents", "Lessons", "Subjects", "Semesters", "Users"}; 

            if (enforceReset) {
                LOGGER.log(Level.INFO, "Enforce reset is true. Dropping all tables...");
                for (int i = tableNames.length - 1; i >= 0; i--) {
                    dropTable(conn, tableNames[i]);
                }
                
                // Sau khi drop, tạo lại bảng theo đúng thứ tự phụ thuộc
                LOGGER.log(Level.INFO, "Creating tables after reset...");
                createUsersTable(conn); 
                createSemestersTable(conn); 
                createSubjectsTable(conn); 
                createLessonsTable(conn);
                createDocumentsTable(conn); // Thêm tạo bảng Documents ở đây

            } else {
                // Nếu không reset, chỉ tạo các bảng nếu chúng chưa tồn tại
                LOGGER.log(Level.INFO, "Enforce reset is false. Creating missing tables...");
                if (!tableExists(conn, "Users")) createUsersTable(conn);
                if (!tableExists(conn, "Semesters")) createSemestersTable(conn);
                if (!tableExists(conn, "Subjects")) createSubjectsTable(conn);
                if (!tableExists(conn, "Lessons")) createLessonsTable(conn);
                if (!tableExists(conn, "Documents")) createDocumentsTable(conn); // Thêm tạo bảng Documents ở đây
            }
            
            LOGGER.log(Level.INFO, "Inserting fake data...");
            insertFakeData(conn); // Luôn chạy insertFakeData để đảm bảo dữ liệu giả
                                     // (với kiểm tra trùng lặp bên trong)

            LOGGER.log(Level.INFO, "Database initialization completed.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed.", e);
        }
    }
    
    // Phương thức main để kiểm tra
    public static void main(String[] args) {
        DBInitializer initializer = new DBInitializer();
        // Để reset hoàn toàn DB (xóa và tạo lại tất cả bảng), truyền true
        // Để chỉ tạo các bảng thiếu và thêm dữ liệu giả nếu chưa có, truyền false
        initializer.initializeDatabase(true); 
    }
}