package entity;

import java.time.LocalDateTime;

public class Document {
    private int id;
    private String fileName;        // Tên file gốc do người dùng upload
    private String storedFileName;  // public_id trên Cloudinary
    private String filePath;        // URL của file trên Cloudinary (secure_url)
    private String fileType;        // Kiểu MIME (image/jpeg, application/pdf, etc.)
    private long fileSize;          // Kích thước file theo byte
    private int uploadedBy;         // ID của người dùng upload (tham chiếu tới bảng Users)
    private LocalDateTime uploadDate; // Ngày giờ upload
    private String description;     // Mô tả tài liệu

    public Document() {
    }

    public Document(int id, String fileName, String storedFileName, String filePath, String fileType, long fileSize, int uploadedBy, LocalDateTime uploadDate, String description) {
        this.id = id;
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
        this.description = description;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}