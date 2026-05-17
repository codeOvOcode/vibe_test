package com.enrollment.dto;

import java.util.List;
import java.util.Map;

public class ImportResult {
    private int total;
    private int duplicated;
    private List<CourseEnrollmentVO> records;
    private Map<String, List<CourseEnrollmentVO>> classified;

    public ImportResult() {}

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getDuplicated() { return duplicated; }
    public void setDuplicated(int duplicated) { this.duplicated = duplicated; }
    public List<CourseEnrollmentVO> getRecords() { return records; }
    public void setRecords(List<CourseEnrollmentVO> records) { this.records = records; }
    public Map<String, List<CourseEnrollmentVO>> getClassified() { return classified; }
    public void setClassified(Map<String, List<CourseEnrollmentVO>> classified) { this.classified = classified; }
}
