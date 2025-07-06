package entity;

import java.util.Date;

public class Note {
    private int id;
    private String title;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Integer subjectId;
    private Integer lessonId;

    public Note() {
    }

    public Note(int id, String title, String content, Date createdAt, Date updatedAt, Integer subjectId, Integer lessonId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.subjectId = subjectId;
        this.lessonId = lessonId;
    }

    public Note(String title, String content, int userId, Integer subjectId, Integer lessonId) {
        this.title = title;
        this.content = content;
        this.subjectId = subjectId;
        this.lessonId = lessonId;
    }

    // Getters & Setters
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }

    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Integer getSubjectId() { return subjectId; }

    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public Integer getLessonId() { return lessonId; }

    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
}
