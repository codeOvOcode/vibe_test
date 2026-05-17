-- ============================================================
-- 高校选课管理系统 - SQL 编程题目
-- 表结构
-- ============================================================

-- 选课记录表
-- CREATE TABLE enrollments (
--     student_id  VARCHAR(20) NOT NULL COMMENT '学生ID（主键部分）',
--     course_id   VARCHAR(20) NOT NULL COMMENT '课程ID（主键部分）',
--     enroll_time DATETIME    NOT NULL COMMENT '选课时间',
--     PRIMARY KEY (student_id, course_id)
-- );

-- 课程表
-- CREATE TABLE courses (
--     course_id   VARCHAR(20) NOT NULL COMMENT '课程ID（主键）',
--     course_name VARCHAR(50) NOT NULL COMMENT '课程名称',
--     course_type VARCHAR(20) NOT NULL COMMENT '课程类型（公共课/专业课）',
--     capacity    INT         NOT NULL COMMENT '课程容量',
--     PRIMARY KEY (course_id)
-- );

-- ============================================================
-- 题目1：统计每门课程的选课人数
-- 返回：课程ID、课程名称、选课人数（别名：enroll_count）
-- 排序：按选课人数降序
-- ============================================================
SELECT
    c.course_id,
    c.course_name,
    COUNT(e.student_id) AS enroll_count
FROM
    courses c
LEFT JOIN
    enrollments e ON c.course_id = e.course_id
GROUP BY
    c.course_id,
    c.course_name
ORDER BY
    enroll_count DESC;

-- ============================================================
-- 题目2：统计选课人数超过50人的专业课
-- 返回：课程ID、课程名称、选课人数
-- 排序：按选课人数升序
-- ============================================================
SELECT
    c.course_id,
    c.course_name,
    COUNT(e.student_id) AS enroll_count
FROM
    courses c
INNER JOIN
    enrollments e ON c.course_id = e.course_id
WHERE
    c.course_type = '专业课'
GROUP BY
    c.course_id,
    c.course_name
HAVING
    COUNT(e.student_id) > 50
ORDER BY
    enroll_count ASC;
