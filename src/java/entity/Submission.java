// src/java/entity/Submission.java
package entity;

import java.time.LocalDateTime;

public class Submission {
    private int id;
    private int quizId;
    private int userId;
    private int score; // Điểm số của bài làm
    private int timeTakenMinutes; // Thời gian làm bài (tính bằng phút)
    private LocalDateTime submissionTime; // Thời điểm nộp bài

    // Constructors
    public Submission() {}

    public Submission(int quizId, int userId, int score, int timeTakenMinutes) {
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.timeTakenMinutes = timeTakenMinutes;
    }

    public Submission(int id, int quizId, int userId, int score, int timeTakenMinutes, LocalDateTime submissionTime) {
        this.id = id;
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.timeTakenMinutes = timeTakenMinutes;
        this.submissionTime = submissionTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTimeTakenMinutes() { return timeTakenMinutes; }
    public void setTimeTakenMinutes(int timeTakenMinutes) { this.timeTakenMinutes = timeTakenMinutes; }
    public LocalDateTime getSubmissionTime() { return submissionTime; }
    public void setSubmissionTime(LocalDateTime submissionTime) { this.submissionTime = submissionTime; }

    @Override
    public String toString() {
        return "Submission{" +
               "id=" + id +
               ", quizId=" + quizId +
               ", userId=" + userId +
               ", score=" + score +
               ", timeTakenMinutes=" + timeTakenMinutes +
               ", submissionTime=" + submissionTime +
               '}';
    }
}