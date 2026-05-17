# 高校选课管理系统

er图.xml使用draw.io查看[https://www.drawio.com/]

基于 **SpringBoot 3.x** + **MySQL 8.0** + **TypeScript** 开发的选课数据批量导入、检索与展示系统。适配高校选课管理场景，支持 CSV 批量导入、多维检索、课程分类展示。

---

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.3.5 |
| 持久层 | Spring Data JPA + Hibernate | 6.5.3 |
| 数据库 | MySQL | 8.0 |
| 连接池 | HikariCP | (内置) |
| 构建工具 | Maven | 3.x |
| Java | JDK | 17 |
| 前端语言 | TypeScript | 5.4+ |
| 前端样式 | 原生 CSS | — |
| 测试框架 | JUnit 5 | (通过 spring-boot-starter-test) |

---

## 项目结构

```
├── pom.xml                          # Maven 配置
├── package.json                     # 前端 TypeScript 依赖
├── tsconfig.json                    # TypeScript 编译配置
├── 需求文档.md                      # 需求分析文档
├── 接口文档.md                      # REST API 接口契约
├── 分析及设计.md                    # 数据模型、并发分析、索引设计
├── 选课统计查询.sql                 # SQL 统计查询
├── frontend/
│   └── src/
│       ├── types.ts                 # 前端类型定义
│       └── main.ts                  # 前端主逻辑
└── src/
    ├── main/java/com/enrollment/
    │   ├── EnrollmentApplication.java      # 启动类
    │   ├── entity/
    │   │   ├── Course.java                 # 课程实体
    │   │   └── Enrollment.java             # 选课实体
    │   ├── repository/
    │   │   ├── CourseRepository.java       # 课程数据访问
    │   │   └── EnrollmentRepository.java   # 选课数据访问
    │   ├── service/
    │   │   ├── EnrollmentService.java      # 业务接口
    │   │   └── impl/EnrollmentServiceImpl.java
    │   ├── controller/
    │   │   └── EnrollmentController.java   # REST 控制器
    │   └── dto/
    │       ├── ApiResponse.java
    │       ├── CourseEnrollmentVO.java
    │       └── ImportResult.java
    ├── main/resources/
    │   ├── application.yml                 # 应用配置
    │   ├── schema.sql                      # 建表语句 + 样例数据
    │   └── static/
    │       ├── index.html                  # 前端页面
    │       ├── css/style.css               # 样式
    │       └── js/main.js                  # 编译后的前端脚本
    └── test/
        └── java/com/enrollment/            # 单元测试
```

---

## 快速开始

### 环境要求

- **JDK 17** 或更高版本
- **Maven 3.6** 或更高版本
- **MySQL 8.0**（默认账号 `root`，密码 `123456`）
- **Node.js**（TypeScript 编译需要）

### 首次启动

```bash
# 1. 初始化数据库（建库、建表、插入样例数据）
# PowerShell:
Get-Content src/main/resources/schema.sql | mysql -u root -p123456

# 或 CMD:
# mysql -u root -p123456 < src\main\resources\schema.sql

# 2. 安装前端依赖并编译 TypeScript
npm install
npx tsc

# 3. 启动后端
mvn spring-boot:run

# 4. 浏览器打开
# http://localhost:8080
```

### 后续启动

数据库已初始化后，只需：

```bash
# 编译前端（如 TypeScript 有修改）
npx tsc

# 启动后端
mvn spring-boot:run
```

如果前端代码未修改，直接：

```bash
mvn spring-boot:run
```

### 运行测试

```bash
mvn test
```

---

## 功能概览

| 功能 | 说明 |
|------|------|
| **CSV 批量导入** | 文本框输入 CSV 格式选课数据，后端自动去重、排序、分类，**持久化到数据库** |
| **多维检索** | 按学生ID、课程ID、课程名称（模糊）、课程类型四种维度检索 |
| **分类展示** | 按专业课/公共课/选修课分组展示，清晰直观 |
| **Excel 导出** | 一键导出数据库中所有选课记录为 .xlsx 文件 |
| **样例数据** | 页面加载时自动展示数据库中的预置样例数据 |
| **性能保障** | 1000+ 条记录检索/排序 ≤ 1秒，支持单次 ≥ 500 条导入 |

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/enrollment/import` | CSV 批量导入 |
| `GET` | `/api/enrollment/export` | Excel 导出 |
| `GET` | `/api/enrollment/search?keyword=X&type=Y` | 多维检索 |
| `GET` | `/api/enrollment/sample` | 获取样例数据 |
| `GET` | `/api/enrollment/by-type?courseType=X` | 按类型分类获取 |
| `GET` | `/api/courses` | 获取课程列表 |

> 详见 [`接口文档.md`](./接口文档.md)

---

## 数据库配置

默认连接信息（`src/main/resources/application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/enrollment_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

如需修改，编辑 `application.yml` 中的 `spring.datasource` 配置即可。

---

## 文档

| 文档 | 说明 |
|------|------|
| [`需求文档.md`](./需求文档.md) | 功能需求详细分析 |
| [`接口文档.md`](./接口文档.md) | REST API 接口契约 |
| [`分析及设计.md`](./分析及设计.md) | 数据模型、ER图、并发分析、索引设计 |
| [`选课统计查询.sql`](./选课统计查询.sql) | SQL 统计查询题目答案 |
