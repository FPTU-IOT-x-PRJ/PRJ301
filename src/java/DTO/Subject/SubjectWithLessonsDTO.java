package DTO.Subject;

import entity.Lesson;
import entity.Subject;
import java.util.List;

public class SubjectWithLessonsDTO {
    private Subject subject;
    private List<Lesson> lessons;

    public SubjectWithLessonsDTO() {
    }

    public SubjectWithLessonsDTO(Subject subject, List<Lesson> lessons) {
        this.subject = subject;
        this.lessons = lessons;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }
    
    
}