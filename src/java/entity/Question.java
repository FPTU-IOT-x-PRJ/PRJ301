// src/java/entity/Question.java
package entity;

import java.time.LocalDateTime;
import java.util.List;

public class Question {
    private int id;
    private int quizId;
    private String questionText;
    private String questionType; // Ví dụ: "MULTIPLE_CHOICE", "TRUE_FALSE"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Dùng để chứa các lựa chọn trả lời khi lấy dữ liệu
    private List<AnswerOption> answerOptions;

    // Constructors
    public Question() {}

    public Question(int quizId, String questionText, String questionType) {
        this.quizId = quizId;
        this.questionText = questionText;
        this.questionType = questionType;
    }
    
    public Question(int id, int quizId, String questionText, String questionType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<AnswerOption> getAnswerOptions() { return answerOptions; }
    public void setAnswerOptions(List<AnswerOption> answerOptions) { this.answerOptions = answerOptions; }
}