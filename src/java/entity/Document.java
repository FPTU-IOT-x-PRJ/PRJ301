package entity;

import java.time.LocalDateTime;
import java.sql.Date;

/**
 * Entity class đại diện cho một tài liệu (Document). Có thể liên kết với một
 * môn học (Subject) hoặc một buổi học (Lesson).
 *
 * @author Dung Ann
 */
public class Document {

    private int id;
    private String fileName;
    private String storedFileName;
    private String filePath;
    private String fileType;
    private long fileSize;
    private int uploadedBy;
    private LocalDateTime uploadDate;
    private String description;
    private Integer subjectId; // Mới: ID môn học, có thể null
    private Integer lessonId;  // Mới: ID buổi học, có thể null

    // Constructors
    public Document() {
    }

    // Constructor khi thêm mới (ID, uploadDate sẽ được DB tự sinh hoặc gán)
    // Cập nhật constructor để bao gồm subjectId và lessonId
    public Document(String fileName, String storedFileName, String filePath, String fileType, long fileSize, int uploadedBy, String description, Integer subjectId, Integer lessonId) {
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.description = description;
        this.subjectId = subjectId;
        this.lessonId = lessonId;
    }

    // Constructor đầy đủ
    // Cập nhật constructor để bao gồm subjectId và lessonId
    public Document(int id, String fileName, String storedFileName, String filePath, String fileType, long fileSize, int uploadedBy, LocalDateTime uploadDate, String description, Integer subjectId, Integer lessonId) {
        this.id = id;
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
        this.description = description;
        this.subjectId = subjectId;
        this.lessonId = lessonId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    @Override
    public String toString() {
        return "Document{" + "id=" + id + ", fileName=" + fileName + ", storedFileName=" + storedFileName + ", filePath=" + filePath + ", fileType=" + fileType + ", fileSize=" + fileSize + ", uploadedBy=" + uploadedBy + ", uploadDate=" + uploadDate + ", description=" + description + ", subjectId=" + subjectId + ", lessonId=" + lessonId + '}';
    }
}
