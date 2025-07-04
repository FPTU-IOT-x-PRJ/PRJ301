package entity;

import java.util.List;

public class Quizzes {
    private int id;
    private int quizId;
    private String question;
    private List<String> answers;
    private List<String> options;

    public Quizzes(int id, int quizId, String question, List<String> answers, List<String> options) {
        this.id = id;
        this.quizId = quizId;
        this.question = question;
        this.answers = answers;
        this.options = options;
    }

    public Quizzes() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "Quizzes{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", question='" + question + '\'' +
                ", answers=" + answers +
                ", options=" + options +
                '}';
    }
}
