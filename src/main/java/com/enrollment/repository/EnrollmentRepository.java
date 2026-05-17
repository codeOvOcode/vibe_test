package com.enrollment.repository;

import com.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Enrollment.EnrollmentId> {

    @Query("SELECT new com.enrollment.dto.CourseEnrollmentVO(e.studentId, e.courseId, c.courseName, c.courseType) " +
           "FROM Enrollment e JOIN Course c ON e.courseId = c.courseId " +
           "ORDER BY e.studentId ASC, e.courseId ASC")
    List<com.enrollment.dto.CourseEnrollmentVO> findAllEnrollments();

    @Query("SELECT new com.enrollment.dto.CourseEnrollmentVO(e.studentId, e.courseId, c.courseName, c.courseType) " +
           "FROM Enrollment e JOIN Course c ON e.courseId = c.courseId " +
           "WHERE e.studentId = :keyword " +
           "ORDER BY e.studentId ASC, e.courseId ASC")
    List<com.enrollment.dto.CourseEnrollmentVO> searchByStudentId(@Param("keyword") String keyword);

    @Query("SELECT new com.enrollment.dto.CourseEnrollmentVO(e.studentId, e.courseId, c.courseName, c.courseType) " +
           "FROM Enrollment e JOIN Course c ON e.courseId = c.courseId " +
           "WHERE e.courseId = :keyword " +
           "ORDER BY e.studentId ASC, e.courseId ASC")
    List<com.enrollment.dto.CourseEnrollmentVO> searchByCourseId(@Param("keyword") String keyword);

    @Query("SELECT new com.enrollment.dto.CourseEnrollmentVO(e.studentId, e.courseId, c.courseName, c.courseType) " +
           "FROM Enrollment e JOIN Course c ON e.courseId = c.courseId " +
           "WHERE LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY e.studentId ASC, e.courseId ASC")
    List<com.enrollment.dto.CourseEnrollmentVO> searchByCourseName(@Param("keyword") String keyword);

    @Query("SELECT new com.enrollment.dto.CourseEnrollmentVO(e.studentId, e.courseId, c.courseName, c.courseType) " +
           "FROM Enrollment e JOIN Course c ON e.courseId = c.courseId " +
           "WHERE c.courseType = :keyword " +
           "ORDER BY e.studentId ASC, e.courseId ASC")
    List<com.enrollment.dto.CourseEnrollmentVO> searchByCourseType(@Param("keyword") String keyword);
}
