package com.enrollment.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "enrollments")
@IdClass(Enrollment.EnrollmentId.class)
public class Enrollment {

    @Id
    @Column(name = "student_id", length = 20, nullable = false)
    private String studentId;

    @Id
    @Column(name = "course_id", length = 20, nullable = false)
    private String courseId;

    @Column(name = "enroll_time", nullable = false)
    private LocalDateTime enrollTime;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "score", precision = 5, scale = 2)
    private java.math.BigDecimal score;

    public Enrollment() {}

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public LocalDateTime getEnrollTime() { return enrollTime; }
    public void setEnrollTime(LocalDateTime enrollTime) { this.enrollTime = enrollTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.math.BigDecimal getScore() { return score; }
    public void setScore(java.math.BigDecimal score) { this.score = score; }

    public static class EnrollmentId implements Serializable {
        private String studentId;
        private String courseId;

        public EnrollmentId() {}
        public EnrollmentId(String studentId, String courseId) {
            this.studentId = studentId;
            this.courseId = courseId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EnrollmentId that)) return false;
            return Objects.equals(studentId, that.studentId) && Objects.equals(courseId, that.courseId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(studentId, courseId);
        }
    }
}
