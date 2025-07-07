// src/java/entity/Quiz.java
package entity;

import java.time.LocalDateTime;

public class Quiz {
    private int id;
    private int subjectId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Quiz() {}

    public Quiz(int subjectId, String title, String description) {
        this.subjectId = subjectId;
        this.title = title;
        this.description = description;
    }

    public Quiz(int id, int subjectId, String title, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.subjectId = subjectId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Quiz{" + "id=" + id + ", subjectId=" + subjectId + ", title=" + title + '}';
    }
}