/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.time.LocalDateTime;

/**
 *
 * @author Dung Ann
 */
public class Subject {

    private int id;
    private int semesterId;
    private String name;
    private String code;
    private String description;
    private int credits;
    private String teacherName;
    private boolean isActive;
    private String prerequisites;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Subject() {
    }

    public Subject(int id, int semesterId, String name, String code, String description, int credits, String teacherName, boolean isActive, String prerequisites, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.semesterId = semesterId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.credits = credits;
        this.teacherName = teacherName;
        this.isActive = isActive;
        this.prerequisites = prerequisites;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Subject(int semesterId, String name, String code, String description, int credits, String teacherName, boolean isActive, String prerequisites, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.semesterId = semesterId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.credits = credits;
        this.teacherName = teacherName;
        this.isActive = isActive;
        this.prerequisites = prerequisites;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getSemesterId() {
        return semesterId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getCredits() {
        return credits;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return this.isActive;
    }

}