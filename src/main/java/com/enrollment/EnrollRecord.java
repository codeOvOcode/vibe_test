/*
 * ============================================================
 * 代码来源标注
 * ============================================================
 * AI工具: Sisyphus (DeepSeek-V4-Pro)
 * AI生成比例: 100%
 * AI生成部分: 类结构、字段定义、全参构造器、getter/setter、
 *             toString()格式化输出、equals()/hashCode()逻辑
 * 手动修改: 无（AI输出完全符合需求规格）
 * 详见: AI提示词与代码标注.md → 提示词一
 * ============================================================
 */
package com.enrollment;

import java.util.Objects;

public class EnrollRecord {
    private String studentId;  // Format: S+6 digits, e.g. "S000001"
    private String courseId;   // Format: C+6 digits, e.g. "C000001"
    private String courseName;

    public EnrollRecord(String studentId, String courseId, String courseName) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    @Override
    public String toString() {
        return String.format("学生ID：%s，课程ID：%s，课程名称：%s", studentId, courseId, courseName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnrollRecord that)) return false;
        return Objects.equals(studentId, that.studentId) && Objects.equals(courseId, that.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseId);
    }
}
