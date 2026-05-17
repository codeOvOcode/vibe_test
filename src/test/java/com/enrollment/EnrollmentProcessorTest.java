package com.enrollment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentProcessorTest {

    private EnrollmentProcessor processor;

    private EnrollRecord r1;
    private EnrollRecord r2;
    private EnrollRecord r3;
    private EnrollRecord r4;
    private EnrollRecord r5;

    @BeforeEach
    void setUp() {
        processor = new EnrollmentProcessor();

        r1 = new EnrollRecord("S000001", "C000001", "Math");
        r2 = new EnrollRecord("S000001", "C000001", "Mathematics");
        r3 = new EnrollRecord("S000002", "C000001", "Math");
        r4 = new EnrollRecord("S000001", "C000002", "Physics");
        r5 = new EnrollRecord("S000003", "C000003", "Chemistry");
    }

    // ==================== deduplicate() tests ====================

    @Test
    @DisplayName("deduplicate: should remove exact duplicates (same studentId+courseId)")
    void deduplicateWithMixedDuplicates() {
        // r1 and r2 have same studentId+courseId => duplicates
        // r3 is different (different studentId)
        List<EnrollRecord> input = Arrays.asList(r1, r2, r3);
        List<EnrollRecord> result = processor.deduplicate(input);

        assertEquals(2, result.size());
        assertSame(r1, result.get(0)); // first occurrence kept by reference
        assertSame(r3, result.get(1)); // r3 kept
    }

    @Test
    @DisplayName("deduplicate: all records same key => output single record")
    void deduplicateAllDuplicates() {
        // All three have same studentId+courseId, different courseNames
        EnrollRecord a = new EnrollRecord("S000001", "C000001", "Math");
        EnrollRecord b = new EnrollRecord("S000001", "C000001", "Mathematics");
        EnrollRecord c = new EnrollRecord("S000001", "C000001", "Advanced Math");
        List<EnrollRecord> input = Arrays.asList(a, b, c);
        List<EnrollRecord> result = processor.deduplicate(input);

        assertEquals(1, result.size());
        assertSame(a, result.get(0)); // first occurrence preserved
    }

    @Test
    @DisplayName("deduplicate: no duplicates => output same size, all records present")
    void deduplicateNoDuplicates() {
        List<EnrollRecord> input = Arrays.asList(r1, r3, r4);
        List<EnrollRecord> result = processor.deduplicate(input);

        assertEquals(3, result.size());
        assertTrue(result.contains(r1));
        assertTrue(result.contains(r3));
        assertTrue(result.contains(r4));
    }

    @Test
    @DisplayName("deduplicate: first occurrence should be kept when duplicates exist")
    void deduplicatePreservesFirst() {
        // r1 = first, r2 = duplicate (same key), r5 = different
        List<EnrollRecord> input = Arrays.asList(r1, r2, r5);
        List<EnrollRecord> result = processor.deduplicate(input);

        assertEquals(2, result.size());
        // r1 (first) kept by reference, r2 excluded, r5 kept
        assertSame(r1, result.get(0));
        assertSame(r5, result.get(1));
    }

    @Test
    @DisplayName("deduplicate: empty list => empty list (not null)")
    void deduplicateEmptyList() {
        List<EnrollRecord> result = processor.deduplicate(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("deduplicate: null input => IllegalArgumentException")
    void deduplicateNullInput() {
        assertThrows(IllegalArgumentException.class, () -> processor.deduplicate(null));
    }

    @Test
    @DisplayName("deduplicate: single record => same single record")
    void deduplicateSingleRecord() {
        List<EnrollRecord> input = Collections.singletonList(r1);
        List<EnrollRecord> result = processor.deduplicate(input);

        assertEquals(1, result.size());
        assertSame(r1, result.get(0));
    }

    // ==================== sort() tests ====================

    @Test
    @DisplayName("sort: by studentId asc, then courseId asc")
    void sortByStudentIdThenCourseId() {
        // Input in reverse order
        List<EnrollRecord> input = Arrays.asList(
                new EnrollRecord("S000003", "C000001", "Chem"),
                new EnrollRecord("S000001", "C000002", "Physics"),
                new EnrollRecord("S000001", "C000001", "Math")
        );
        List<EnrollRecord> result = processor.sort(input);

        assertEquals(3, result.size());
        // studentId "S000001" < "S000003"
        assertEquals("S000001", result.get(0).getStudentId());
        assertEquals("C000001", result.get(0).getCourseId());
        assertEquals("S000001", result.get(1).getStudentId());
        assertEquals("C000002", result.get(1).getCourseId());
        assertEquals("S000003", result.get(2).getStudentId());
        assertEquals("C000001", result.get(2).getCourseId());
    }

    @Test
    @DisplayName("sort: same studentId => sorted by courseId asc")
    void sortSameStudentIdDifferentCourseId() {
        List<EnrollRecord> input = Arrays.asList(
                new EnrollRecord("S000001", "C000003", "Chem"),
                new EnrollRecord("S000001", "C000001", "Math"),
                new EnrollRecord("S000001", "C000002", "Physics")
        );
        List<EnrollRecord> result = processor.sort(input);

        assertEquals(3, result.size());
        assertEquals("C000001", result.get(0).getCourseId());
        assertEquals("C000002", result.get(1).getCourseId());
        assertEquals("C000003", result.get(2).getCourseId());
    }

    @Test
    @DisplayName("sort: empty list => empty list")
    void sortEmptyList() {
        List<EnrollRecord> result = processor.sort(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sort: null input => IllegalArgumentException")
    void sortNullInput() {
        assertThrows(IllegalArgumentException.class, () -> processor.sort(null));
    }

    @Test
    @DisplayName("sort: single record => same single record")
    void sortSingleRecord() {
        List<EnrollRecord> input = Collections.singletonList(r1);
        List<EnrollRecord> result = processor.sort(input);

        assertEquals(1, result.size());
        assertSame(r1, result.get(0));
    }

    // ==================== process() tests ====================

    @Test
    @DisplayName("process: full pipeline — deduplicate, sort, and print each record")
    void processFullPipeline() {
        // Input: duplicates + unsorted order
        // r1=S000001/C000001/Math, r2=S000001/C000001/Mathematics (dupe of r1)
        // r5=S000003/C000003/Chemistry, r3=S000002/C000001/Math
        List<EnrollRecord> input = Arrays.asList(r5, r1, r2, r3);

        // Capture System.out
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));

        List<EnrollRecord> result;
        try {
            result = processor.process(input);
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }

        // After dedup: r5, r1, r3 (r2 removed)
        // After sort by studentId then courseId: r1 (S000001/C000001), r3 (S000002/C000001), r5 (S000003/C000003)
        assertEquals(3, result.size());
        assertSame(r1, result.get(0));
        assertSame(r3, result.get(1));
        assertSame(r5, result.get(2));

        // Verify printed output matches toString() format
        String[] lines = baos.toString().split(System.lineSeparator());
        assertEquals(3, lines.length);
        assertEquals(r1.toString(), lines[0]);
        assertEquals(r3.toString(), lines[1]);
        assertEquals(r5.toString(), lines[2]);
    }

    @Test
    @DisplayName("process: empty list => empty list, no output")
    void processEmptyList() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));

        List<EnrollRecord> result;
        try {
            result = processor.process(Collections.emptyList());
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals("", baos.toString());
    }

    @Test
    @DisplayName("process: null input => IllegalArgumentException")
    void processNullInput() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(null));
    }
}
