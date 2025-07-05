package entity;

public class Quizzes {

    private int id;
    private int quizId;
    private String question;
    private String answers;
    private String options;

    public Quizzes(int id, int quizId, String question, String answers, String options) {
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

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    

    @Override
    public String toString() {
        return "Quizzes{"
                + "id=" + id
                + ", quizId=" + quizId
                + ", question='" + question + '\''
                + ", answers=" + answers
                + ", options=" + options
                + '}';
    }
}
