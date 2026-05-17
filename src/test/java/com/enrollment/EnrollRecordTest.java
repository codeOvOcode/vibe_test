package com.enrollment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class EnrollRecordTest {

    @Test
    @DisplayName("Constructor properly sets studentId, courseId, courseName")
    void constructorSetsAllFields() {
        EnrollRecord record = new EnrollRecord("S000001", "C000001", "高等数学");

        assertEquals("S000001", record.getStudentId());
        assertEquals("C000001", record.getCourseId());
        assertEquals("高等数学", record.getCourseName());
    }

    @Test
    @DisplayName("All getters return correct values")
    void gettersReturnCorrectValues() {
        EnrollRecord record = new EnrollRecord("S000042", "C000099", "线性代数");

        assertEquals("S000042", record.getStudentId());
        assertEquals("C000099", record.getCourseId());
        assertEquals("线性代数", record.getCourseName());
    }

    @Test
    @DisplayName("All setters update values correctly")
    void settersUpdateValuesCorrectly() {
        EnrollRecord record = new EnrollRecord("S000001", "C000001", "高等数学");

        record.setStudentId("S000999");
        record.setCourseId("C000888");
        record.setCourseName("数据结构");

        assertEquals("S000999", record.getStudentId());
        assertEquals("C000888", record.getCourseId());
        assertEquals("数据结构", record.getCourseName());
    }

    @Test
    @DisplayName("toString() returns EXACT format with Chinese colons")
    void toStringReturnsExactFormat() {
        EnrollRecord record = new EnrollRecord("S000001", "C000001", "高等数学");

        String expected = "学生ID：S000001，课程ID：C000001，课程名称：高等数学";
        assertEquals(expected, record.toString());
    }

    @Test
    @DisplayName("equals() returns true when studentId AND courseId are same (courseName irrelevant)")
    void equalsReturnsTrueForSameStudentIdAndCourseId() {
        EnrollRecord record1 = new EnrollRecord("S000001", "C000001", "高等数学");
        EnrollRecord record2 = new EnrollRecord("S000001", "C000001", "线性代数");

        assertTrue(record1.equals(record2));
        assertTrue(record2.equals(record1));
    }

    @Test
    @DisplayName("equals() returns false when studentId differs")
    void equalsReturnsFalseWhenStudentIdDiffers() {
        EnrollRecord record1 = new EnrollRecord("S000001", "C000001", "高等数学");
        EnrollRecord record2 = new EnrollRecord("S000002", "C000001", "高等数学");

        assertFalse(record1.equals(record2));
    }

    @Test
    @DisplayName("equals() returns false when courseId differs (even if courseName is same)")
    void equalsReturnsFalseWhenCourseIdDiffers() {
        EnrollRecord record1 = new EnrollRecord("S000001", "C000001", "高等数学");
        EnrollRecord record2 = new EnrollRecord("S000001", "C000002", "高等数学");

        assertFalse(record1.equals(record2));
    }

    @Test
    @DisplayName("hashCode() is consistent with equals() — same studentId+courseId = same hashCode")
    void hashCodeConsistentWithEquals() {
        EnrollRecord record1 = new EnrollRecord("S000001", "C000001", "高等数学");
        EnrollRecord record2 = new EnrollRecord("S000001", "C000001", "线性代数");

        assertEquals(record1.hashCode(), record2.hashCode());
    }

    @Test
    @DisplayName("Two records with same studentId+courseId but different courseName have same hashCode")
    void sameHashCodeForDifferentCourseName() {
        EnrollRecord record1 = new EnrollRecord("S000042", "C000099", "高等数学");
        EnrollRecord record2 = new EnrollRecord("S000042", "C000099", "大学英语");

        assertEquals(record1.hashCode(), record2.hashCode());
    }

    @Test
    @DisplayName("equals(null) returns false")
    void equalsNullReturnsFalse() {
        EnrollRecord record = new EnrollRecord("S000001", "C000001", "高等数学");

        assertFalse(record.equals(null));
    }

    @Test
    @DisplayName("equals(same object) returns true")
    void equalsSameObjectReturnsTrue() {
        EnrollRecord record = new EnrollRecord("S000001", "C000001", "高等数学");

        assertTrue(record.equals(record));
    }

    @Test
    @DisplayName("hashCode differs when studentId differs")
    void hashCodeDiffersWhenStudentIdDiffers() {
        EnrollRecord record1 = new EnrollRecord("S000001", "C000001", "高等数学");
        EnrollRecord record2 = new EnrollRecord("S000002", "C000001", "高等数学");

        assertNotEquals(record1.hashCode(), record2.hashCode());
    }

    @Test
    @DisplayName("hashCode differs when courseId differs")
    void hashCodeDiffersWhenCourseIdDiffers() {
        EnrollRecord record1 = new EnrollRecord("S000001", "C000001", "高等数学");
        EnrollRecord record2 = new EnrollRecord("S000001", "C000002", "高等数学");

        assertNotEquals(record1.hashCode(), record2.hashCode());
    }
}
