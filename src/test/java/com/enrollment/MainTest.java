package com.enrollment;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private static final Pattern RECORD_PATTERN = Pattern.compile(
            "学生ID：(S\\d+)，课程ID：(C\\d+)，课程名称：.+"
    );

    private String captureMainOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        System.setOut(ps);
        try {
            Main.main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString();
    }

    private List<String> extractStudentIds(String output) {
        List<String> ids = new ArrayList<>();
        Matcher m = RECORD_PATTERN.matcher(output);
        while (m.find()) {
            ids.add(m.group(1));
        }
        return ids;
    }

    @Test
    void mainProcessesDemoData() {
        String output = captureMainOutput();

        // Output is not empty
        assertFalse(output.isEmpty(), "Output should not be empty");

        // Output contains Chinese-formatted lines matching toString() format
        assertTrue(output.contains("学生ID："), "Output should contain Chinese-formatted record lines");

        // Duplicates are removed: output has fewer records than input had duplicates
        List<String> studentIds = extractStudentIds(output);
        assertTrue(studentIds.size() < 10,
                "Output should have fewer records than 10 input records due to dedup, got: " + studentIds.size());

        // Records are sorted: student IDs appear in ascending order
        for (int i = 1; i < studentIds.size(); i++) {
            assertTrue(studentIds.get(i - 1).compareTo(studentIds.get(i)) <= 0,
                    "Student IDs should be in ascending order, but "
                            + studentIds.get(i - 1) + " > " + studentIds.get(i));
        }
    }

    @Test
    void mainHasCorrectNumberOfOutputRecords() {
        String output = captureMainOutput();
        List<String> studentIds = extractStudentIds(output);
        // 10 input records, 2 duplicate pairs → 8 unique records
        assertEquals(8, studentIds.size(),
                "Expected 8 output records after dedup of 2 duplicate pairs, got: " + studentIds.size());
    }

    @Test
    void mainOutputContainsExpectedStudentIds() {
        String output = captureMainOutput();

        assertTrue(output.contains("S000001"), "Output should contain S000001");
        assertTrue(output.contains("S000003"), "Output should contain S000003");
        assertTrue(output.contains("S000005"), "Output should contain S000005");
    }

    @Test
    void mainDoesNotCrashOnEmpty() {
        assertDoesNotThrow(() -> Main.main(new String[]{}),
                "Main.main should not throw any exception");
    }
}
