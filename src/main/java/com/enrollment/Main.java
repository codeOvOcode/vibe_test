package com.enrollment;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<EnrollRecord> records = new ArrayList<>();

        // 10 records with duplicates and mixed ordering
        records.add(new EnrollRecord("S000003", "C000001", "高等数学"));
        records.add(new EnrollRecord("S000001", "C000002", "大学英语"));
        records.add(new EnrollRecord("S000002", "C000003", "计算机网络"));
        records.add(new EnrollRecord("S000001", "C000001", "Java程序设计"));
        records.add(new EnrollRecord("S000005", "C000001", "高等数学"));
        records.add(new EnrollRecord("S000002", "C000003", "计算机网络进阶"));
        records.add(new EnrollRecord("S000004", "C000002", "大学英语"));
        records.add(new EnrollRecord("S000002", "C000001", "数据结构"));
        records.add(new EnrollRecord("S000001", "C000003", "操作系统"));
        records.add(new EnrollRecord("S000001", "C000003", "操作系统进阶"));

        EnrollmentProcessor processor = new EnrollmentProcessor();
        System.out.println("=== 选课记录处理结果 ===");
        System.out.println("原始记录数: " + records.size());
        List<EnrollRecord> processed = processor.process(records);
        System.out.println("处理后记录数: " + processed.size());
    }
}
