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

    private static final String INSERT_DOCUMENT_SQL = 
        "INSERT INTO Documents (fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, description) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_DOCUMENTS_SQL = "SELECT * FROM Documents WHERE uploadedBy = ? ORDER BY uploadDate DESC";
    private static final String SELECT_DOCUMENT_BY_ID_SQL = "SELECT * FROM Documents WHERE id = ? AND uploadedBy = ?";
    private static final String UPDATE_DOCUMENT_SQL = 
        "UPDATE Documents SET fileName = ?, storedFileName = ?, filePath = ?, fileType = ?, fileSize = ?, description = ?, uploadDate = GETDATE() " + // Cập nhật cả uploadDate khi sửa metadata
        "WHERE id = ? AND uploadedBy = ?";
    private static final String DELETE_DOCUMENT_SQL = "DELETE FROM Documents WHERE id = ? AND uploadedBy = ?";

    public boolean addDocument(Document doc) {
        boolean rowInserted = false;
        try (PreparedStatement ps = connection.prepareStatement(INSERT_DOCUMENT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doc.getFileName());
            ps.setString(2, doc.getStoredFileName());
            ps.setString(3, doc.getFilePath());
            ps.setString(4, doc.getFileType());
            ps.setLong(5, doc.getFileSize());
            ps.setInt(6, doc.getUploadedBy());
            ps.setString(7, doc.getDescription());
            
            rowInserted = ps.executeUpdate() > 0;
            
            if (rowInserted) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        doc.setId(rs.getInt(1)); 
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding document: " + doc.getFileName(), e);
            printSQLException(e);
        }
        return rowInserted;
    }

    public List<Document> getAllDocumentsByUser(int userId) {
        List<Document> documents = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_DOCUMENTS_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documents.add(extractDocumentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all documents for user ID: " + userId, e);
            printSQLException(e);
        }
        return documents;
    }

    public Document getDocumentById(int id, int userId) {
        Document doc = null;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DOCUMENT_BY_ID_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doc = extractDocumentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving document by ID: " + id + " for user ID: " + userId, e);
            printSQLException(e);
        }
        return doc;
    }
    
    // NEW: Update Document Metadata
    public boolean updateDocument(Document doc) {
        boolean rowUpdated = false;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_DOCUMENT_SQL)) {
            ps.setString(1, doc.getFileName());
            ps.setString(2, doc.getStoredFileName());
            ps.setString(3, doc.getFilePath());
            ps.setString(4, doc.getFileType());
            ps.setLong(5, doc.getFileSize());
            ps.setString(6, doc.getDescription());
            ps.setInt(7, doc.getId());
            ps.setInt(8, doc.getUploadedBy());
            
            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating document: " + doc.getId(), e);
            printSQLException(e);
        }
        return rowUpdated;
    }
    
    public boolean deleteDocument(int id, int userId) {
        boolean rowDeleted = false;
        try (PreparedStatement ps = connection.prepareStatement(DELETE_DOCUMENT_SQL)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting document with ID: " + id + " for user ID: " + userId, e);
            printSQLException(e);
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
        
        return new Document(id, fileName, storedFileName, filePath, fileType, fileSize, uploadedBy, uploadDate, description);
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