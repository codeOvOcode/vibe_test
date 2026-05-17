package com.enrollment.service;

import com.enrollment.dto.CourseEnrollmentVO;
import com.enrollment.dto.ImportResult;

import java.util.List;
import java.util.Map;

public interface EnrollmentService {

    List<CourseEnrollmentVO> parseCsv(String csvData);

    List<CourseEnrollmentVO> deduplicate(List<CourseEnrollmentVO> records);

    List<CourseEnrollmentVO> sort(List<CourseEnrollmentVO> records);

    Map<String, List<CourseEnrollmentVO>> classify(List<CourseEnrollmentVO> records);

    List<CourseEnrollmentVO> search(String keyword, String type);

    ImportResult importCsv(String csvData);

    List<CourseEnrollmentVO> getSampleData();

    Map<String, List<CourseEnrollmentVO>> getByType(String courseType);

    List<CourseEnrollmentVO> getByTypeFlat(String courseType);

    List<CourseEnrollmentVO> getAllData();
}
