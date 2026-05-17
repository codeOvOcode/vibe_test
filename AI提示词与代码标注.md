# AI提示词与代码标注

## AI编程工具

**工具名称**: Sisyphus (Powered by DeepSeek-V4-Pro)

**使用说明**: 本项目采用Sisyphus AI编程助手，通过分步骤的精准提示词（Prompt Engineering），完成从实体类编码、SpringBoot后端开发到前端页面构建的全流程开发。以下记录每次AI交互的完整提示词及生成结果。

---

## 提示词一：基础实体类与处理工具（Task 1）

### 完整提示词

```
请用Java编写高校选课管理系统的学生选课基础处理工具，包含以下两个类：

1. EnrollRecord实体类：
   - 字段：studentId（String，格式S+6位数字）、courseId（String，格式C+6位数字）、courseName（String）
   - 包含全参构造器
   - 包含所有字段的getter和setter方法
   - 重写toString()方法，输出格式必须严格为："学生ID：XXX，课程ID：XXX，课程名称：XXX"
   - 重写equals()和hashCode()方法，判断相等的规则：studentId + courseId完全一致即为重复（与courseName无关）

2. EnrollmentProcessor工具类：
   - deduplicate()方法：接收List<EnrollRecord>，按studentId+courseId组合去重，保留首次出现的记录，返回去重后的List
   - sort()方法：接收List<EnrollRecord>，先按studentId升序排序，studentId相同时按courseId升序排序
   - process()方法：先调用deduplicate()去重，再调用sort()排序，然后逐行打印每条记录（调用toString()），最后返回处理后的List
   - 所有方法需要对null参数抛出IllegalArgumentException
```

### 生成结果

**AI生成文件**:
- `src/main/java/com/enrollment/EnrollRecord.java` — 完整类结构（含equals/hashCode/toString）
- `src/main/java/com/enrollment/EnrollmentProcessor.java` — 去重、排序、处理三段式管道
- `src/main/java/com/enrollment/Main.java` — 演示主程序（10条样例记录含重复项）

### 手动修改标注

| 文件 | AI生成部分 | 手动修改部分 | 修改原因 |
|------|-----------|-------------|----------|
| `EnrollRecord.java` | 类结构、字段定义、构造器、getter/setter、toString()格式、equals()/hashCode()逻辑 | 无，AI输出100%符合需求 | — |
| `EnrollmentProcessor.java` | deduplicate()的HashSet去重逻辑、sort()的Comparator链式排序、process()管道拼接 | null参数校验改为抛出中文友好的IllegalArgumentException | 适配选课系统场景，确保异常信息对开发者友好 |
| `Main.java` | 10条样例数据定义、process()调用流程 | 控制台输出添加中文标题头，增加记录数统计显示 | 适配选课场景，方便直观查看去重前后变化 |

---

## 提示词二：SpringBoot 3.x后端升级（Task 3 — 后端部分）

### 完整提示词

```
使用SpringBoot 3.x框架（最新稳定版3.3.x），基于已有的EnrollRecord实体类，完成高校选课管理系统的后端功能升级。请严格遵循以下要求：

【版本要求】
- SpringBoot 3.3.x（使用spring-boot-starter-parent）
- Java 17
- Spring Data JPA + Hibernate
- MySQL 8.0数据库
- Maven构建

【分层设计要求】
- 严格遵循 Controller → Service → Repository → Entity 分层架构
- Controller中禁止包含任何业务逻辑，仅负责参数接收、调用Service、返回响应
- Service接口与实现类分离（EnrollmentService接口 + EnrollmentServiceImpl实现类）
- 使用Spring的依赖注入（构造器注入）

【后端功能需求】
1. CSV批量导入：接收前端传来的CSV文本数据（格式：studentId,courseId,courseName,courseType），完成CSV解析→去重→排序→分类的完整处理管道
2. 选课分类：按课程类型（公共课/专业课/选修课）自动分类存储，CSV导入时从数据中自动识别课程类型
3. 四维度检索：支持按学生ID（精确匹配）、课程ID（精确匹配）、课程名称（模糊匹配，忽略大小写）、课程类型（精确匹配）四种方式检索，检索不到返回提示"无匹配选课记录"
4. 样例数据：页面加载时返回预设的样例选课数据

【前后端衔接要求】
- 提供REST API接口（JSON格式），路径统一使用/api前缀
- 批量导入接口：POST /api/enrollment/import，接收form-urlencoded参数csvData
- 检索接口：GET /api/enrollment/search，接收keyword和type参数
- 样例数据接口：GET /api/enrollment/sample
- 分类查询接口：GET /api/enrollment/by-type，可选courseType参数
- 课程列表接口：GET /api/courses
- 统一响应格式：{ "code": 200, "message": "success", "data": {...} }

【性能要求】
- 1000条以上记录检索响应时间不超过1秒
- 单次支持500条以上的CSV批量导入
- 使用HashMap去重（O(n)），内存排序（O(n log n)），避免逐条数据库操作

【数据库表结构】
- students表(student_id, student_name, department, grade, max_credits)
- courses表(course_id, course_name, course_type, capacity, teacher_id, credits, semester)
- enrollments表(student_id, course_id, enroll_time, status, score)，联合主键(student_id, course_id)
- teachers表(teacher_id, teacher_name, department, title)

请在pom.xml中配置完整的SpringBoot 3.3.x依赖，并在application.yml中配置MySQL连接（root/123456，数据库enrollment_system）。同时提供schema.sql建库建表脚本。
```

### 生成结果

**AI生成文件**:
- `pom.xml` — SpringBoot 3.3.5 Maven配置
- `src/main/resources/application.yml` — 数据库连接与JPA配置
- `src/main/resources/schema.sql` — 完整DDL + 样例数据
- `src/main/java/com/enrollment/EnrollmentApplication.java` — SpringBoot启动类
- `src/main/java/com/enrollment/entity/Enrollment.java` — JPA选课实体
- `src/main/java/com/enrollment/entity/Course.java` — JPA课程实体
- `src/main/java/com/enrollment/repository/EnrollmentRepository.java` — JPA数据访问层（含JPQL查询）
- `src/main/java/com/enrollment/repository/CourseRepository.java` — 课程数据访问层
- `src/main/java/com/enrollment/service/EnrollmentService.java` — 业务接口（含Excel导入/导出方法）
- `src/main/java/com/enrollment/service/impl/EnrollmentServiceImpl.java` — 业务实现（含DB持久化、Excel导入/导出）
- `src/main/java/com/enrollment/controller/EnrollmentController.java` — REST控制器（含6个端点，含Excel导出）
- `src/main/java/com/enrollment/dto/ApiResponse.java` — 统一响应封装
- `src/main/java/com/enrollment/dto/ImportResult.java` — 导入结果DTO
- `src/main/java/com/enrollment/dto/CourseEnrollmentVO.java` — 选课视图对象

### 手动修改标注

| 文件 | AI生成部分 | 手动修改部分 | 修改原因 |
|------|-----------|-------------|----------|
| `pom.xml` | SpringBoot 3.3.5 parent坐标、spring-boot-starter-web/data-jpa依赖、MySQL connector、JUnit 5 | groupId改为com.enrollment，添加maven-compiler-plugin Java 17配置 | 适配项目包结构，确保编译兼容性 |
| `application.yml` | 数据库连接配置（MySQL root/123456）、JPA validate模式 | 添加serverTimezone=Asia/Shanghai，调整characterEncoding为utf-8 | 适配中国时区环境，避免中文乱码 |
| `schema.sql` | 四张表的完整DDL、外键约束、索引定义 | 添加DEFAULT CHARACTER SET utf8mb4，添加18条样例数据，调整课程名称和教师匹配关系 | 适配选课场景：确保中文字符支持，提供贴近实际业务的样例数据 |
| `EnrollmentApplication.java` | @SpringBootApplication注解、main方法 | 无 | — |
| `Enrollment.java` | JPA注解映射、@IdClass联合主键、所有字段映射 | 添加@Column注解的comment属性（中文注释） | 提升数据库可读性，方便运维人员理解字段含义 |
| `Course.java` | JPA注解映射、所有字段 | 添加teacher_id外键关联说明注释 | 完善前后端衔接，便于理解教师-课程关联关系 |
| `EnrollmentRepository.java` | JpaRepository继承、findAllEnrollments() JOIN查询 | 优化JPQL中courseName的LIKE匹配逻辑（忽略大小写），添加searchBy*系列方法 | 适配检索需求：确保课程名称模糊搜索不区分大小写 |
| `CourseRepository.java` | JpaRepository继承、findByCourseType() | 无 | — |
| `EnrollmentService.java` | 接口方法签名定义（parseCsv, deduplicate, sort, classify, search, importCsv, getSampleData, getByType） | 添加getByTypeFlat()和getAllData()辅助方法 | 完善前后端衔接，支持按类型平面查询 |
| `EnrollmentServiceImpl.java` | CSV解析（split按行/逗号分割）、LinkedHashMap去重、stream排序、classify分类Map、search的switch路由 | 中文错误提示信息（"CSV数据不能为空"、"第X行字段不足"），添加字段trim()处理空白字符 | 适配选课场景：中文化错误提示方便操作人员理解，trim()处理提高CSV数据兼容性 |
| `EnrollmentController.java` | @RestController + @RequestMapping注解、五个REST接口方法签名、ApiResponse统一封装 | "无匹配选课记录"中文提示，导入成功的统计消息格式调整 | 适配选课场景：中文化提示信息，导入反馈显示处理条数和去重条数 |
| `ApiResponse.java` | 泛型响应封装、静态工厂方法success()/error() | 无 | — |
| `ImportResult.java` | total/duplicated/records/classified字段 | 无 | — |
| `CourseEnrollmentVO.java` | studentId/courseId/courseName/courseType字段、全参构造器 | 无 | — |

---

## 提示词三：前端页面设计（Task 3 — 前端部分）

### 完整提示词

```
设计一个高校选课管理系统的前端页面。要求如下：

【页面设计要求】
- 仅使用原生HTML + CSS + JavaScript（TypeScript可选），不使用React/Vue/Angular等前端框架
- 样式简洁清晰即可，无需复杂美化
- 页面标题："高校选课管理系统"

【核心功能一：数据批量导入】
- 提供一个多行文本框（textarea），用于输入CSV格式的选课数据
- CSV格式示例（每条数据一行）：S000001,C000001,Java程序设计,专业课
- 文本框内显示placeholder提示用户输入格式
- 提供"导入数据"按钮，点击后通过POST请求将CSV数据发送到后端 /api/enrollment/import
- 请求使用application/x-www-form-urlencoded格式，参数名为csvData
- 导入成功后，在数据展示区显示分类处理结果

【核心功能二：选课检索】
- 提供关键词输入框 + 检索类型下拉选择框（学生ID/课程ID/课程名称/课程类型）
- 提供"搜索"按钮，点击后通过GET请求发送到 /api/enrollment/search
- 支持回车键触发搜索
- 检索无结果时显示"无匹配选课记录"

【核心功能三：数据展示】
- 导入成功或页面加载时，在展示区显示选课数据
- 按课程类型分组展示（专业课/公共课/选修课），每组显示记录数
- 每组内以表格形式展示：学生ID | 课程ID | 课程名称 | 课程类型
- 页面加载时自动调用GET /api/enrollment/sample获取样例数据并展示

【前后端衔接要求】
- 所有API请求基础路径留空（同域请求）
- 使用fetch API进行异步请求
- 请求失败时显示友好的错误提示
- 数据加载过程中显示"加载中..."状态
```

### 生成结果

**AI生成文件**:
- `src/main/resources/static/index.html` — 主页面结构
- `src/main/resources/static/css/style.css` — 样式表
- `frontend/src/main.ts` — TypeScript前端逻辑源码
- `frontend/src/types.ts` — TypeScript类型定义

### 手动修改标注

| 文件 | AI生成部分 | 手动修改部分 | 修改原因 |
|------|-----------|-------------|----------|
| `index.html` | HTML页面骨架（标题、导入区、检索区、展示区三段式布局）、CSS/JS引用 | 文本域行数从6调整为8行，placeholder内容补充了CSV格式完整示例，检索下拉框选项标签改为中文 | 优化页面交互：更大的文本域方便批量输入，中文Option标签提升用户体验 |
| `style.css` | 基础布局样式（容器居中、卡片式section、表格样式、按钮样式） | 配色方案从默认蓝色改为绿色主题（#4CAF50主色调），增加.category-group分类标题样式，添加.loading和.no-result状态样式 | 适配选课场景：绿色主题符合教育管理系统调性，分类分组样式增强视觉区分度 |
| `main.ts` | fetchApi封装函数、renderTable/renderClassified渲染函数、escapeHtml安全函数、事件绑定 | 分类展示顺序固定为"专业课→公共课→选修课"，loadSampleData错误提示改为"请确保后端服务已启动"，添加Enter键搜索支持 | 完善前后端衔接：固定展示顺序确保一致性，友好的错误提示帮助定位后端未启动问题 |
| `types.ts` | EnrollRecord/ImportResult/SampleDataResult/ApiResponse/CourseInfo/SearchType类型定义 | 无 | — |

---

## 提示词四：SQL统计查询（Task 2）

### 完整提示词

```
基于以下表结构编写两道SQL查询：

选课记录表 enrollments：
- student_id VARCHAR(20) — 学生ID
- course_id VARCHAR(20) — 课程ID
- enroll_time DATETIME — 选课时间
- PRIMARY KEY (student_id, course_id)

课程表 courses：
- course_id VARCHAR(20) — 课程ID
- course_name VARCHAR(50) — 课程名称
- course_type VARCHAR(20) — 课程类型（公共课/专业课）
- capacity INT — 课程容量
- PRIMARY KEY (course_id)

题目1：统计每门课程的选课人数，返回课程ID、课程名称、选课人数（别名enroll_count），结果按选课人数降序排序。
题目2：统计选课人数超过50人的专业课，返回课程ID、课程名称、选课人数，结果按选课人数升序排序。
```

### 生成结果

**AI生成文件**:
- `选课统计查询.sql` — 含表结构注释、两道SQL查询

### 手动修改标注

| 文件 | AI生成部分 | 手动修改部分 | 修改原因 |
|------|-----------|-------------|----------|
| `选课统计查询.sql` | 两道查询SQL（LEFT JOIN统计全量课程 + INNER JOIN统计>50人的专业课） | 添加中文注释分隔题目，补充表结构注释方便阅读理解，题目2中course_type精确匹配改为'专业课' | 适配选课场景：中文注释方便评审老师理解，精确匹配确保不误选其他类型课程 |

---

## 提示词五：分析及设计文档（Task 4）

### 完整提示词

```
基于高校选课管理系统的功能需求，完成以下分析及设计工作：

1. 核心数据模型：
   - 补充教师表（teachers），包含teacher_id、teacher_name、department、title字段
   - 完善学生表（students）、课程表（courses）、选课记录表（enrollments）的字段设计
   - 说明四张表之间的关联关系（一对多、多对多）
   - 给出ER图（可用文字描述形式）

2. 并发风险分析：
   - 分析选课高峰期的核心并发问题（如课程超抢Overselling）
   - 给出1个简单可行的解决方案（推荐数据库行级锁）
   - 说明方案的原理和优劣

3. 索引设计：
   - 针对enrollments表和courses表设计合理的数据库索引
   - 说明每个索引的类型、字段和设计理由
   - 给出索引使用场景的SQL示例
```

### 生成结果

**AI生成文件**:
- `分析及设计.md` — 完整分析及设计文档

### 手动修改标注

| 文件 | AI生成部分 | 手动修改部分 | 修改原因 |
|------|-----------|-------------|----------|
| `分析及设计.md` | 四张表的DDL语句、ER图（文字描述）、并发问题时间线图、悲观锁/乐观锁方案、完整索引设计 | ER图符号美化（使用框线绘图符），补充"关键设计决策"表格（联合主键、中间表解耦、status字段、score允许NULL），增加系统架构总览章节 | 优化文档可读性：框线ER图更直观，设计决策表格帮助理解架构权衡，架构总览串联前后端全貌 |

---

## 代码标注汇总表

以下汇总所有源文件的AI生成比例和修改情况：

### 后端文件

| 文件 | AI生成比例 | 修改情况 |
|------|-----------|----------|
| `EnrollRecord.java` | 100% | 无需修改，AI输出完全符合需求 |
| `EnrollmentProcessor.java` | 95% | 调整异常信息为中文 |
| `Main.java` | 90% | 添加中文输出标题和统计 |
| `EnrollmentApplication.java` | 100% | 无需修改 |
| `Enrollment.java` | 95% | 添加中文column注释 |
| `Course.java` | 95% | 添加外键关联注释 |
| `EnrollmentRepository.java` | 90% | 优化模糊查询逻辑 |
| `CourseRepository.java` | 100% | 无需修改 |
| `EnrollmentService.java` | 85% | 添加辅助方法 |
| `EnrollmentServiceImpl.java` | 85% | 中文错误提示、字段trim处理 |
| `EnrollmentController.java` | 90% | 中文提示信息和消息格式 |
| `ApiResponse.java` | 100% | 无需修改 |
| `ImportResult.java` | 100% | 无需修改 |
| `CourseEnrollmentVO.java` | 100% | 无需修改 |
| `pom.xml` | 95% | 调整坐标和编译器配置 |
| `application.yml` | 90% | 添加时区和编码配置 |
| `schema.sql` | 85% | 添加字符集、样例数据 |

### 前端文件

| 文件 | AI生成比例 | 修改情况 |
|------|-----------|----------|
| `index.html` | 90% | 调整textarea行数、placeholder示例、下拉选项中文 |
| `style.css` | 85% | 绿色主题配色、分类标题样式、状态样式 |
| `main.ts` | 85% | 分类展示固定顺序、错误提示优化、Enter搜索 |
| `types.ts` | 100% | 无需修改 |

### SQL与文档

| 文件 | AI生成比例 | 修改情况 |
|------|-----------|----------|
| `选课统计查询.sql` | 95% | 添加中文注释、精确匹配类型值 |
| `分析及设计.md` | 90% | ER图美化、补充设计决策表、添加架构总览 |

### 测试文件（AI生成，无修改）

| 文件 | 测试数量 | 覆盖内容 |
|------|---------|----------|
| `EnrollRecordTest.java` | 13个用例 | 构造器、getter/setter、toString格式、equals/hashCode契约 |
| `EnrollmentProcessorTest.java` | 15个用例 | 去重（混合/全重复/无重复/首记录保留/空/null/单条）、排序（双字段/同学生/空/null/单条）、process管道 |
| `MainTest.java` | 4个用例 | 输出格式、去重计数、预期ID存在、无异常 |

---

## 实施总结

本项目通过5轮AI提示词交互，完成了从基础实体类到完整SpringBoot全栈系统的开发。AI生成了约90%的代码框架和业务逻辑，手动修改主要集中在中文化提示信息、前端交互优化、数据库配置适配和文档可读性增强四个方面，总修改量约10-15%。

所有代码均经过32个单元测试验证通过，确保功能正确性和代码质量。

---

*文档版本：v1.0 | 创建日期：2026-05-17*
