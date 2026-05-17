-- ============================================================
-- 高校选课管理系统 - 数据库建表语句
-- 数据库: MySQL 8.0+
-- 账号: root / 密码: 123456
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS enrollment_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE enrollment_system;

-- ============================================================
-- 1. 学生表 (students)
-- ============================================================
DROP TABLE IF EXISTS students;
CREATE TABLE students (
    student_id   VARCHAR(20)  NOT NULL COMMENT '学生ID，格式：S+6位数字',
    student_name VARCHAR(50)  NOT NULL COMMENT '学生姓名',
    department   VARCHAR(50)  DEFAULT NULL COMMENT '所属院系',
    grade        VARCHAR(10)  DEFAULT NULL COMMENT '年级，如：2024级',
    max_credits  INT          DEFAULT 30 COMMENT '最大可选学分',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (student_id),
    INDEX idx_department (department),
    INDEX idx_grade (grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- ============================================================
-- 2. 教师表 (teachers)
-- ============================================================
DROP TABLE IF EXISTS teachers;
CREATE TABLE teachers (
    teacher_id   VARCHAR(20)  NOT NULL COMMENT '教师ID，格式：T+6位数字',
    teacher_name VARCHAR(50)  NOT NULL COMMENT '教师姓名',
    department   VARCHAR(50)  DEFAULT NULL COMMENT '所属院系',
    title        VARCHAR(20)  DEFAULT NULL COMMENT '职称：教授/副教授/讲师',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (teacher_id),
    INDEX idx_teacher_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师表';

-- ============================================================
-- 3. 课程表 (courses)
-- ============================================================
DROP TABLE IF EXISTS courses;
CREATE TABLE courses (
    course_id    VARCHAR(20)  NOT NULL COMMENT '课程ID，格式：C+6位数字',
    course_name  VARCHAR(50)  NOT NULL COMMENT '课程名称',
    course_type  VARCHAR(20)  NOT NULL COMMENT '课程类型：公共课/专业课/选修课',
    capacity     INT          NOT NULL DEFAULT 30 COMMENT '课程容量（选课上限人数）',
    teacher_id   VARCHAR(20)  DEFAULT NULL COMMENT '授课教师ID',
    credits      INT          DEFAULT 2 COMMENT '学分',
    semester     VARCHAR(20)  DEFAULT NULL COMMENT '开课学期，如：2024-2025-1',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (course_id),
    INDEX idx_course_type (course_type),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_course_type_capacity (course_type, capacity),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ============================================================
-- 4. 选课记录表 (enrollments)
-- ============================================================
DROP TABLE IF EXISTS enrollments;
CREATE TABLE enrollments (
    student_id  VARCHAR(20) NOT NULL COMMENT '学生ID',
    course_id   VARCHAR(20) NOT NULL COMMENT '课程ID',
    enroll_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    status      VARCHAR(10) DEFAULT '正常' COMMENT '选课状态：正常/退选/待补选',
    score       DECIMAL(5,2) DEFAULT NULL COMMENT '成绩',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (student_id, course_id),
    INDEX idx_course_id (course_id),
    INDEX idx_enroll_time (enroll_time),
    INDEX idx_status (status),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES students(student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_enroll_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课记录表';

-- ============================================================
-- 5. 样例数据 (Sample Data)
-- ============================================================

-- 教师样例数据
INSERT INTO teachers (teacher_id, teacher_name, department, title) VALUES
('T000001', '张教授', '计算机学院', '教授'),
('T000002', '李副教授', '计算机学院', '副教授'),
('T000003', '王讲师', '数学学院', '讲师'),
('T000004', '赵教授', '外语学院', '教授'),
('T000005', '陈副教授', '管理学院', '副教授');

-- 课程样例数据
INSERT INTO courses (course_id, course_name, course_type, capacity, teacher_id, credits, semester) VALUES
('C000001', 'Java程序设计',     '专业课', 60, 'T000001', 4, '2024-2025-2'),
('C000002', '数据结构与算法',   '专业课', 50, 'T000002', 3, '2024-2025-2'),
('C000003', '计算机网络',       '公共课', 80, 'T000002', 3, '2024-2025-2'),
('C000004', '高等数学',         '公共课', 100, 'T000003', 5, '2024-2025-2'),
('C000005', '大学英语',         '公共课', 120, 'T000004', 4, '2024-2025-2'),
('C000006', '数据库原理',       '专业课', 50, 'T000001', 3, '2024-2025-2'),
('C000007', '管理学概论',       '选修课', 60, 'T000005', 2, '2024-2025-2'),
('C000008', 'Python数据分析',   '选修课', 40, 'T000003', 2, '2024-2025-2'),
('C000009', '软件工程',         '专业课', 45, 'T000001', 3, '2024-2025-2'),
('C000010', '线性代数',         '公共课', 100, 'T000003', 4, '2024-2025-2');

-- 学生样例数据
INSERT INTO students (student_id, student_name, department, grade, max_credits) VALUES
('S000001', '张三', '计算机学院', '2024级', 25),
('S000002', '李四', '计算机学院', '2024级', 25),
('S000003', '王五', '数学学院',   '2023级', 28),
('S000004', '赵六', '外语学院',   '2024级', 22),
('S000005', '孙七', '管理学院',   '2024级', 24),
('S000006', '周八', '计算机学院', '2023级', 28),
('S000007', '吴九', '计算机学院', '2024级', 25),
('S000008', '郑十', '数学学院',   '2024级', 25);

-- 选课记录样例数据
INSERT INTO enrollments (student_id, course_id, enroll_time, status) VALUES
('S000001', 'C000001', '2025-02-20 09:00:00', '正常'),
('S000001', 'C000002', '2025-02-20 09:01:00', '正常'),
('S000001', 'C000006', '2025-02-20 09:02:00', '正常'),
('S000002', 'C000001', '2025-02-20 09:10:00', '正常'),
('S000002', 'C000003', '2025-02-20 09:11:00', '正常'),
('S000002', 'C000004', '2025-02-20 09:12:00', '正常'),
('S000003', 'C000004', '2025-02-20 09:20:00', '正常'),
('S000003', 'C000008', '2025-02-20 09:21:00', '正常'),
('S000004', 'C000005', '2025-02-20 09:30:00', '正常'),
('S000004', 'C000007', '2025-02-20 09:31:00', '正常'),
('S000005', 'C000007', '2025-02-20 09:40:00', '正常'),
('S000005', 'C000003', '2025-02-20 09:41:00', '正常'),
('S000006', 'C000002', '2025-02-20 09:50:00', '正常'),
('S000006', 'C000009', '2025-02-20 09:51:00', '正常'),
('S000007', 'C000003', '2025-02-20 10:00:00', '正常'),
('S000007', 'C000008', '2025-02-20 10:01:00', '正常'),
('S000007', 'C000010', '2025-02-20 10:02:00', '正常'),
('S000008', 'C000004', '2025-02-20 10:10:00', '正常'),
('S000008', 'C000010', '2025-02-20 10:11:00', '正常');
