package entity;

public class Quizz {
    private int id;
    private int subjectId;
    private String title;
    private String description;

    public Quizz(int id, int subjectId, String title, String description) {
        this.id = id;
        this.subjectId = subjectId;
        this.title = title;
        this.description = description;
    }

    public Quizz() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Quizz{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
