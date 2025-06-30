package dao;

import dal.DBContext;
import entity.Document;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(DocumentDAO.class.getName());

    // Cập nhật INSERT_DOCUMENT_SQL để bao gồm subject_id và lesson_id
    private static final String INSERT_DOCUMENT_SQL = 
        "INSERT INTO Documents (fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, description, subject_id, lesson_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    // Cập nhật SELECT_ALL_DOCUMENTS_SQL để lấy thêm subject_id và lesson_id
    private static final String SELECT_ALL_DOCUMENTS_SQL = "SELECT id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subject_id, lesson_id FROM Documents WHERE uploadedBy = ? ORDER BY uploadDate DESC";
    
    // Cập nhật SELECT_DOCUMENT_BY_ID_SQL để lấy thêm subject_id và lesson_id
    private static final String SELECT_DOCUMENT_BY_ID_SQL = "SELECT id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subject_id, lesson_id FROM Documents WHERE id = ? AND uploadedBy = ?";
    
    // Cập nhật UPDATE_DOCUMENT_SQL để bao gồm subject_id và lesson_id
    private static final String UPDATE_DOCUMENT_SQL = 
        "UPDATE Documents SET fileName = ?, storedFileName = ?, filePath = ?, fileType = ?, fileSize = ?, description = ?, uploadDate = GETDATE(), subject_id = ?, lesson_id = ? " + 
        "WHERE id = ? AND uploadedBy = ?";
    
    private static final String DELETE_DOCUMENT_SQL = "DELETE FROM Documents WHERE id = ? AND uploadedBy = ?";

    public boolean addDocument(Document doc) {
        boolean rowInserted = false;
        try (PreparedStatement ps = connection.prepareStatement(INSERT_DOCUMENT_SQL)) {
            ps.setString(1, doc.getFileName());
            ps.setString(2, doc.getStoredFileName());
            ps.setString(3, doc.getFilePath());
            ps.setString(4, doc.getFileType());
            ps.setLong(5, doc.getFileSize());
            ps.setInt(6, doc.getUploadedBy());
            ps.setString(7, doc.getDescription());
            
            // Đặt subject_id. Sử dụng setObject để xử lý Integer có thể null
            if (doc.getSubjectId() != null) {
                ps.setInt(8, doc.getSubjectId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            
            // Đặt lesson_id. Sử dụng setObject để xử lý Integer có thể null
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

    public List<Document> getAllDocuments(int userId) {
        List<Document> documents = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_DOCUMENTS_SQL)) {
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

    public Document getDocumentById(int id, int userId) {
        Document document = null;
        try (PreparedStatement ps = connection.prepareStatement(SELECT_DOCUMENT_BY_ID_SQL)) {
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

    public boolean updateDocument(Document doc) {
        boolean rowUpdated = false;
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_DOCUMENT_SQL)) {
            ps.setString(1, doc.getFileName());
            ps.setString(2, doc.getStoredFileName());
            ps.setString(3, doc.getFilePath());
            ps.setString(4, doc.getFileType());
            ps.setLong(5, doc.getFileSize());
            ps.setString(6, doc.getDescription());
            
            // Đặt subject_id. Sử dụng setObject để xử lý Integer có thể null
            if (doc.getSubjectId() != null) {
                ps.setInt(7, doc.getSubjectId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            
            // Đặt lesson_id. Sử dụng setObject để xử lý Integer có thể null
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

    public boolean deleteDocument(int id, int userId) {
        boolean rowDeleted = false;
        try (PreparedStatement ps = connection.prepareStatement(DELETE_DOCUMENT_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            printSQLException(ex);
        }
        return rowDeleted;
    }

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
        
        // Lấy subject_id và lesson_id (có thể null từ DB)
        Integer subjectId = rs.getObject("subject_id", Integer.class);
        Integer lessonId = rs.getObject("lesson_id", Integer.class);
        
        return new Document(id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description, subjectId, lessonId);
    }
    
    /**
     * Hàm tiện ích để ghi chi tiết lỗi SQL vào logger, thay vì System.err.
     * Cải thiện việc quản lý log và gỡ lỗi trong ứng dụng thực tế.
     *
     * @param ex Ngoại lệ SQLException cần ghi.
     */
    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                // Sử dụng LOGGER thay vì System.err
                LOGGER.log(Level.SEVERE, "SQLState: " + ((SQLException) e).getSQLState());
                LOGGER.log(Level.SEVERE, "Error Code: " + ((SQLException) e).getErrorCode());
                LOGGER.log(Level.SEVERE, "Message: " + e.getMessage());
                
                // Cải thiện để in stack trace của nguyên nhân gây ra lỗi
                Throwable t = e.getCause(); // Lấy nguyên nhân của SQLException hiện tại
                while (t != null) {
                    LOGGER.log(Level.SEVERE, "Cause: " + t.getMessage(), t); // Ghi log nguyên nhân và stack trace của nó
                    t = t.getCause();
                }
            }
        }
    }
}