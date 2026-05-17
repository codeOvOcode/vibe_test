package com.enrollment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "student_name", length = 50, nullable = false)
    private String studentName;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "grade", length = 10)
    private String grade;

    @Column(name = "max_credits")
    private Integer maxCredits;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Student() {}

    public Student(String studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.createdAt = LocalDateTime.now();
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Integer getMaxCredits() { return maxCredits; }
    public void setMaxCredits(Integer maxCredits) { this.maxCredits = maxCredits; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
