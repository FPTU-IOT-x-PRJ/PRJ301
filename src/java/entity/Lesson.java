package entity;

import java.time.LocalDateTime;
import java.sql.Date;

/**
 * Entity class đại diện cho một buổi học (Lesson).
 * Liên kết với một môn học (Subject).
 *
 * @author Dung Ann
 */
public class Lesson {
    private int id;
    private int subjectId;
    private String name;
    private Date lessonDate; // Ngày diễn ra buổi học
    private String description;
    private String status; // Ví dụ: "Planned", "Completed", "Cancelled"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Lesson() {
    }

    // Constructor khi thêm mới (ID, createdAt, updatedAt sẽ được DB tự sinh hoặc gán)
    public Lesson(int subjectId, String name, Date lessonDate, String description, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.subjectId = subjectId;
        this.name = name;
        this.lessonDate = lessonDate;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor đầy đủ
    public Lesson(int id, int subjectId, String name, Date lessonDate, String description, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.subjectId = subjectId;
        this.name = name;
        this.lessonDate = lessonDate;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    public Date getLessonDate() {
        return lessonDate;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLessonDate(Date lessonDate) {
        this.lessonDate = lessonDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Lesson{" + "id=" + id + ", subjectId=" + subjectId + ", name=" + name + ", lessonDate=" + lessonDate + ", description=" + description + ", status=" + status + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
}