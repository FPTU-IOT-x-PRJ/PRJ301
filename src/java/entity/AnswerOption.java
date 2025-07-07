// src/java/entity/AnswerOption.java
package entity;

public class AnswerOption {
    private int id;
    private int questionId;
    private String optionText;
    private boolean isCorrect;

    // Constructors
    public AnswerOption() {}

    public AnswerOption(int questionId, String optionText, boolean isCorrect) {
        this.questionId = questionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }
    
    public AnswerOption(int id, int questionId, String optionText, boolean isCorrect) {
        this.id = id;
        this.questionId = questionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public boolean isIsCorrect() { return isCorrect; }
    public void setIsCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
}