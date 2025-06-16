
package entity;

import java.time.LocalDateTime;
import java.sql.Date;

/**
 *
 * @author Dung Ann
 */
public class Semester {
    private int id;
    private String name;
    private Date startDate;
    private Date endDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    public Semester(){}
    public Semester( String name, Date startDate, Date endDate, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
       
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Semester(int id, String name, Date startDate, Date endDate, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Semester{" + "id=" + id + ", name=" + name + ", startDate=" + startDate + ", endDate=" + endDate + ", status=" + status + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    
    
    
}
