package dao;

import dal.DBContext;
import entity.Document;
import java.sql.Connection; // Import Connection
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp DAO quản lý các thao tác CRUD và các truy vấn liên quan đến đối tượng
 * Document trong cơ sở dữ liệu. Author: Dung Ann
 */
public class DocumentDAO extends DBContext { // Kế thừa DBContext

    private static final Logger LOGGER = Logger.getLogger(DocumentDAO.class.getName());

    // --- Hằng số SQL ---
    private static final String INSERT_DOCUMENT_SQL
            = "INSERT INTO Documents (fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, description, subjectId, lessonId) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_ALL_DOCUMENTS_BY_USER_SQL
            = "SELECT id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subjectId, lessonId FROM Documents WHERE uploadedBy = ? ORDER BY uploadDate DESC";

    private static final String SELECT_DOCUMENT_BY_ID_AND_USER_SQL
            = "SELECT id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subjectId, lessonId FROM Documents WHERE id = ? AND uploadedBy = ?";

    private static final String UPDATE_DOCUMENT_SQL
            = "UPDATE Documents SET fileName = ?, storedFileName = ?, filePath = ?, fileType = ?, fileSize = ?, description = ?, uploadDate = GETDATE(), subjectId = ?, lessonId = ? "
            + "WHERE id = ? AND uploadedBy = ?";

    private static final String DELETE_DOCUMENT_SQL = "DELETE FROM Documents WHERE id = ? AND uploadedBy = ?";

    private static final String SELECT_DOCUMENTS_BY_SUBJECT_ID_SQL
            = "SELECT id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subjectId, lessonId FROM Documents WHERE subjectId = ? AND uploadedBy = ? ORDER BY uploadDate DESC";

    private static final String SELECT_DOCUMENTS_BY_LESSON_ID_SQL
            = "SELECT id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subjectId, lessonId FROM Documents WHERE lessonId = ? AND uploadedBy = ? ORDER BY uploadDate DESC";

    /**
     * Thêm một tài liệu mới vào cơ sở dữ liệu.
     *
     * @param doc Đối tượng Document cần thêm.
     * @return true nếu thêm thành công, ngược lại là false.
     */
    public boolean addDocument(Document doc) {
        boolean rowInserted = false;
        // Lấy kết nối mới cho mỗi thao tác
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(INSERT_DOCUMENT_SQL)) {
            ps.setString(1, doc.getFileName());
            ps.setString(2, doc.getStoredFileName());
            ps.setString(3, doc.getFilePath());
            ps.setString(4, doc.getFileType());
            ps.setLong(5, doc.getFileSize());
            ps.setInt(6, doc.getUploadedBy());
            ps.setString(7, doc.getDescription());

            if (doc.getSubjectId() != null) {
                ps.setInt(8, doc.getSubjectId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            if (doc.getLessonId() != null) {
                ps.setInt(9, doc.getLessonId());
            } else {
                ps.setNull(9, java.sql.Types.INTEGER);
            }

            rowInserted = ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return rowInserted;
    }

    /**
     * Lấy một tài liệu bằng ID và userId của người tải lên.
     *
     * @param id ID của tài liệu.
     * @param userId ID của người dùng đã tải tài liệu lên.
     * @return Đối tượng Document nếu tìm thấy, ngược lại trả về null.
     */
    public Document getDocumentById(int id, int userId) {
        Document document = null;
        try (Connection con = getConnection(); // Lấy kết nối từ DBContext
                 PreparedStatement ps = con.prepareStatement(SELECT_DOCUMENT_BY_ID_AND_USER_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    document = extractDocumentFromResultSet(rs);
                }
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return document;
    }

    /**
     * Lấy tất cả tài liệu được tải lên bởi một người dùng cụ thể.
     *
     * @param userId ID của người dùng.
     * @return Danh sách các đối tượng Document.
     */
    public List<Document> getAllDocumentsByUserId(int userId) {
        List<Document> documents = new ArrayList<>();
        try (Connection con = getConnection(); // Lấy kết nối từ DBContext
                 PreparedStatement ps = con.prepareStatement(SELECT_ALL_DOCUMENTS_BY_USER_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documents.add(extractDocumentFromResultSet(rs));
                }
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return documents;
    }

    /**
     * Lấy danh sách tài liệu dựa trên các tiêu chí lọc. Có thể lọc theo
     * subjectId và/hoặc lessonId.
     *
     * @param userId ID của người dùng.
     * @param subjectId ID của môn học (có thể null nếu không lọc theo môn học).
     * @param lessonId ID của buổi học (có thể null nếu không lọc theo buổi
     * học).
     * @return Danh sách các đối tượng Document thỏa mãn điều kiện.
     */
    public List<Document> getFilteredDocuments(int userId, Integer subjectId, Integer lessonId) {
        List<Document> documents = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subjectId, lessonId FROM Documents WHERE uploadedBy = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (subjectId != null) {
            sqlBuilder.append(" AND subjectId = ?");
            params.add(subjectId);
        }

        if (lessonId != null) {
            sqlBuilder.append(" AND lessonId = ?");
            params.add(lessonId);
        }

        sqlBuilder.append(" ORDER BY uploadDate DESC");

        // --- Bắt đầu phần logging SQL ---
        LOGGER.log(Level.INFO, "SQL Query for getFilteredDocuments: {0}", sqlBuilder.toString());
        LOGGER.log(Level.INFO, "Parameters for getFilteredDocuments: {0}", params.toString());
        // --- Kết thúc phần logging SQL ---

        try (Connection con = getConnection(); // Lấy kết nối từ DBContext
                 PreparedStatement ps = con.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    documents.add(extractDocumentFromResultSet(rs));
                }
                LOGGER.log(Level.INFO, "Number of documents fetched: {0}", count);

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            printSQLException(ex);
        }
        return documents;
    }

    /**
     * Lấy tất cả tài liệu liên quan đến một môn học cụ thể, được tải lên bởi
     * một người dùng.
     *
     * @param subjectId ID của môn học.
     * @param userId ID của người dùng đã tải tài liệu lên.
     * @return Danh sách các đối tượng Document.
     */
    public List<Document> getDocumentsBySubjectId(int subjectId, int userId) {
        List<Document> documents = new ArrayList<>();
        try (Connection con = getConnection(); // Lấy kết nối từ DBContext
                 PreparedStatement ps = con.prepareStatement(SELECT_DOCUMENTS_BY_SUBJECT_ID_SQL)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documents.add(extractDocumentFromResultSet(rs));
                }
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return documents;
    }

    /**
     * Lấy tất cả tài liệu liên quan đến một buổi học cụ thể, được tải lên bởi
     * một người dùng.
     *
     * @param lessonId ID của buổi học.
     * @param userId ID của người dùng đã tải tài liệu lên.
     * @return Danh sách các đối tượng Document.
     */
    public List<Document> getDocumentsByLessonId(int lessonId, int userId) {
        List<Document> documents = new ArrayList<>();
        try (Connection con = getConnection(); // Lấy kết nối từ DBContext
                 PreparedStatement ps = con.prepareStatement(SELECT_DOCUMENTS_BY_LESSON_ID_SQL)) {
            ps.setInt(1, lessonId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documents.add(extractDocumentFromResultSet(rs));
                }
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return documents;
    }

    /**
     * Cập nhật thông tin một tài liệu trong cơ sở dữ liệu.
     *
     * @param doc Đối tượng Document chứa thông tin cần cập nhật (ID và
     * uploadedBy để xác định bản ghi).
     * @return true nếu cập nhật thành công, ngược lại là false.
     */
    public boolean editDocument(Document doc) {
        boolean rowUpdated = false;
        try (Connection con = getConnection(); // Lấy kết nối từ DBContext
                 PreparedStatement ps = con.prepareStatement(UPDATE_DOCUMENT_SQL)) {
            ps.setString(1, doc.getFileName());
            ps.setString(2, doc.getStoredFileName());
            ps.setString(3, doc.getFilePath());
            ps.setString(4, doc.getFileType());
            ps.setLong(5, doc.getFileSize());
            ps.setString(6, doc.getDescription());

            if (doc.getSubjectId() != null) {
                ps.setInt(7, doc.getSubjectId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }

            if (doc.getLessonId() != null) {
                ps.setInt(8, doc.getLessonId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.setInt(9, doc.getId());
            ps.setInt(10, doc.getUploadedBy());

            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return rowUpdated;
    }

    /**
     * Xóa một tài liệu khỏi cơ sở dữ liệu.
     *
     * @param id ID của tài liệu cần xóa.
     * @param userId ID của người dùng đã tải tài liệu lên (để đảm bảo quyền).
     * @return true nếu xóa thành công, ngược lại là false.
     */
    public boolean deleteDocument(int id, int userId) {
        boolean rowDeleted = false;
        try (Connection con = getConnection(); // Lấy kết nối từ DBContext
                 PreparedStatement ps = con.prepareStatement(DELETE_DOCUMENT_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return rowDeleted;
    }

    /**
     * Phương thức trợ giúp để trích xuất dữ liệu từ ResultSet thành đối tượng
     * Document.
     *
     * @param rs ResultSet chứa dữ liệu tài liệu.
     * @return Đối tượng Document.
     * @throws SQLException Nếu có lỗi khi truy cập dữ liệu từ ResultSet.
     */
    private Document extractDocumentFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String fileName = rs.getString("fileName");
        String storedFileName = rs.getString("storedFileName");
        String filePath = rs.getString("filePath");
        String fileType = rs.getString("fileType");
        long fileSize = rs.getLong("fileSize");
        int uploadedBy = rs.getInt("uploadedBy");
        LocalDateTime uploadDate = rs.getTimestamp("uploadDate") != null ? rs.getTimestamp("uploadDate").toLocalDateTime() : null;
        String description = rs.getString("description");
        int subjectId = rs.getInt("subjectId");
        int lessonId = rs.getInt("lessonId");
        return new Document(id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subjectId, lessonId);
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
