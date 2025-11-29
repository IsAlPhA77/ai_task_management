# AI任务管理系统

一个基于Spring Boot和AI技术的智能任务管理系统，支持自然语言创建任务、智能优先级推荐、耗时预测和自动报告生成等功能。

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [API文档](#api文档)
- [AI使用记录](#ai使用记录)
- [开发规范](#开发规范)

## 项目简介

AI任务管理系统是一个集成了人工智能能力的任务管理平台，旨在帮助用户更高效地管理日常任务。系统支持通过自然语言快速创建任务，并利用AI技术提供智能化的任务管理建议。

### 核心亮点

- **AI驱动的任务创建**: 支持自然语言输入，自动解析任务信息
- **智能优先级推荐**: 基于截止时间、依赖关系等因素智能推荐任务优先级
- **耗时预测**: 结合历史数据和AI模型预测任务完成时间
- **自动报告生成**: 自动生成周报、月报等任务总结报告

## 技术栈

### 后端框架

- **Spring Boot 3.2.4**: 核心框架
- **Java 21**: 开发语言
- **MyBatis-Plus 3.5.14**: ORM框架
- **Spring Security**: 安全框架
- **Spring Data Redis**: 缓存支持

### AI集成

- **DashScope SDK 2.12.0**: 阿里云通义千问API
- **OpenAI API**: GPT模型支持（可选）
- **多模型降级策略**: 支持多个AI服务提供商自动切换

### 其他技术

- **JWT**: 无状态认证
- **Swagger/OpenAPI**: API文档生成
- **Lombok**: 代码简化
- **MySQL**: 关系型数据库
- **Redis**: 缓存和Token黑名单

## 功能特性

### 基础功能

- 用户注册、登录、登出
- JWT Token认证（AccessToken + RefreshToken）
- 任务CRUD操作
- 任务查询和筛选
- 任务历史记录追踪

### AI增强功能

- **自然语言任务创建**: 通过自然语言描述自动创建任务
- **任务优先级推荐**: 智能分析任务特征，推荐最优优先级
- **任务耗时预测**: 基于历史数据和AI模型预测任务耗时
- **自动报告生成**: 自动生成周报、月报，包含AI总结

### 高级功能

- 任务统计分析
- 多条件任务查询
- 任务依赖关系管理
- 任务标签分类
- 任务截止时间提醒

## 项目结构

```
ai_task_management/
├── backend/
│   ├── src/main/java/com/scu/ai_task_management/
│   │   ├── boot/                          # 启动类
│   │   ├── common/                        # 公共模块
│   │   │   ├── annotation/               # 自定义注解
│   │   │   ├── constants/                # 常量定义
│   │   │   ├── exception/                # 异常处理
│   │   │   └── utils/                     # 工具类
│   │   ├── infra/                         # 基础设施层
│   │   │   ├── ai/                        # AI服务集成
│   │   │   ├── mybatis/                  # MyBatis配置
│   │   │   ├── redis/                    # Redis配置
│   │   │   ├── security/                 # 安全配置
│   │   │   └── web/                      # Web配置
│   │   ├── user/                          # 用户模块
│   │   ├── task_basic/                    # 任务基础模块
│   │   ├── task_query/                    # 任务查询模块
│   │   ├── task_statistic/                # 任务统计模块
│   │   ├── task_ai_assistant/            # AI助手模块
│   │   ├── task_priority_recommendation/  # 优先级推荐模块
│   │   ├── task_duration_prediction/      # 耗时预测模块
│   │   └── task_report_generation/        # 报告生成模块
│   ├── src/main/resources/
│   │   ├── application.yml                # 应用配置
│   │   └── sql/
│   │       └── schema.sql                 # 数据库脚本
│   ├── pom.xml
│   └── .gitignore
└── README.md
```

## 项目展示

![屏幕截图 2025-11-29 221429](C:\Users\MZH\Pictures\Screenshots\屏幕截图 2025-11-29 221429.png)

![屏幕截图 2025-11-29 221440](C:\Users\MZH\Pictures\Screenshots\屏幕截图 2025-11-29 221440.png)

![屏幕截图 2025-11-29 221453](C:\Users\MZH\Pictures\Screenshots\屏幕截图 2025-11-29 221453.png)

![屏幕截图 2025-11-29 221506](C:\Users\MZH\Pictures\Screenshots\屏幕截图 2025-11-29 221506.png)

![屏幕截图 2025-11-29 221525](C:\Users\MZH\Pictures\Screenshots\屏幕截图 2025-11-29 221525.png)

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 配置步骤

1. **克隆项目**

```bash
git clone <repository-url>
cd ai_task_management
```

1. **创建数据库**

```bash
mysql -u root -p < backend/src/main/resources/sql/schema.sql
```

1. **配置环境变量**

创建 `.env` 文件或设置环境变量:

```properties
QWEN_API_KEY=your_qwen_api_key
OPENAI_API_KEY=your_openai_api_key  # 可选
```

1. **修改数据库配置**

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_task_management
    username: your_username
    password: your_password
```

1. **启动项目**

```bash
cd backend
mvn spring-boot:run
```

1. **访问API文档**

```
http://localhost:8080/swagger-ui.html
```

## API文档

项目集成了Swagger/OpenAPI，启动后可通过以下地址访问:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### 主要API端点

#### 用户管理

- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/update` - 更新用户信息

#### 任务管理

- `POST /api/tasks` - 创建任务
- `GET /api/tasks/{id}` - 获取任务详情
- `PUT /api/tasks/{id}` - 更新任务
- `DELETE /api/tasks/{id}` - 删除任务

#### AI功能

- `POST /api/tasks/natural-language` - 自然语言创建任务
- `POST /api/task-priority/recommendations` - 获取优先级推荐
- `POST /api/task-duration/predictions` - 获取耗时预测
- `POST /api/task-reports/generate` - 生成任务报告

## AI调用记录

**输入**: "明天下午3点开会讨论项目进度，4点写周报"

AI能够返回以下内容，存储具体任务到任务表中

```json
{
  "tasks": [
    {
      "title": "开会讨论项目进度",
      "description": "讨论项目进度",
      "status": "TODO",
      "category": "工作",
      "deadline": "2025-11-30 15:00:00",
      "estimatedDuration": 60,
      "tags": ["会议", "项目"],
      "priority": 70,
      "confidence": 0.95
    },
    {
      "title": "写周报",
      "description": "编写本周工作总结",
      "status": "TODO",
      "category": "工作",
      "deadline": "2025-11-30 16:00:00",
      "estimatedDuration": 30,
      "tags": ["文档"],
      "priority": 60,
      "confidence": 0.90
    }
  ],
  "overallConfidence": 0.92
}
```

## AI使用记录

本项目在开发过程中使用了Claude AI辅助开发，以下是详细的使用记录和证明材料。

### 使用的AI模型

- **模型名称**: Claude 3.5 Sonnet
- **使用平台**: Claude.ai
- **使用目的**: 代码生成、架构设计、代码重构

------

### 记录1: 进阶功能模块开发

#### 背景说明

在完成基础的任务CRUD和自然语言创建功能后，需要扩展三个AI增强功能模块。

#### 提示词（Prompt）

```
我已经完成了任务增删改查的基础功能和AI的api接口实现自然语言创建任务。
接下来请参考我的业务封装逻辑（task_basic等），创建新的业务来实现以下进阶功能:

2. 进阶功能
- 任务优先级推荐（根据截止时间、依赖关系等）
- 任务耗时预测（基于历史数据或AI模型）
- 任务报告自动生成（如周报总结已完成任务）

请按照现有的项目结构（controller/service/model分层）实现这三个模块。
```

#### AI输出摘要

Claude生成了三个完整的业务模块:

**1. 任务优先级推荐模块** (`task_priority_recommendation`)

- `TaskPriorityRecommendationController.java`: 提供4个API端点
- `TaskPriorityRecommendationService.java`: 实现多因素优先级算法
- `PriorityRecommendationDTO.java` 和 `PriorityRecommendationVO.java`: 数据传输对象

**2. 任务耗时预测模块** (`task_duration_prediction`)

- `TaskDurationPredictionController.java`: 提供3个API端点
- `TaskDurationPredictionService.java`: 实现三种预测方法（SIMPLE/AI/HYBRID）
- `DurationPredictionDTO.java` 和 `DurationPredictionVO.java`: 数据传输对象

**3. 任务报告生成模块** (`task_report_generation`)

- `TaskReportGenerationController.java`: 提供4个API端点
- `TaskReportGenerationService.java`: 实现周报、月报生成和AI总结
- `TaskReportDTO.java` 和 `TaskReportVO.java`: 数据传输对象

#### 实际修改内容

1. 创建了3个新的业务包，共18个Java文件
2. 实现了优先级推荐算法，综合考虑截止时间、依赖关系、任务类型等5个因素
3. 实现了耗时预测功能，支持历史数据分析和AI模型预测
4. 实现了报告生成功能，自动统计任务数据并调用AI生成工作总结
5. 所有代码遵循现有的分层架构和命名规范

#### 证明材料

- Git提交记录: `feat: 添加任务优先级推荐、耗时预测和报告生成模块`
- 代码文件路径:
  - `backend/src/main/java/com/scu/ai_task_management/task_priority_recommendation/`
  - `backend/src/main/java/com/scu/ai_task_management/task_duration_prediction/`
  - `backend/src/main/java/com/scu/ai_task_management/task_report_generation/`

------

### 记录2: 代码注释格式统一

#### 背景说明

项目中存在多种注释格式不统一的问题，包括emoji符号和中英文标点混用。

#### 提示词（Prompt）

```
将所有文件中存在注释的部分统一注释的格式，要求:
1. 移除所有emoji符号
2. 统一使用英文冒号
3. 保持JavaDoc格式一致性
```

#### AI输出摘要

Claude系统性地修改了所有Java文件的注释格式:

1. **移除emoji符号**: 移除了所有装饰性符号（如 ✅ 📊 等）
2. **统一标点符号**: 将所有中文冒号（：）改为英文冒号（:）
3. **规范化注释**: 确保所有类、方法、字段注释符合JavaDoc规范

#### 实际修改内容

修改了30+个Java文件，主要包括:

- 常量类: `TokenConstants.java`, `AIConstants.java`, `ExceptionConstants.java`
- 模型类: `Task.java`, `TaskReportDTO.java`, `DurationPredictionDTO.java`
- 服务类: `AIServiceImpl.java`, `TaskReportGenerationServiceImpl.java`
- 配置类: `WebConfig.java`, `SecurityConfig.java`, `MybatisPlusConfig.java`

修改示例:

```java
// 修改前
报告类型：WEEKLY-周报 📊
任务状态：TODO-待办 ✅

// 修改后
报告类型: WEEKLY-周报
任务状态: TODO-待办
```

#### 证明材料

- Git提交记录: `style: 统一代码注释格式，移除emoji并规范标点`
- 修改对比: 可通过 `git diff b4c6d8e` 查看详细变更

------

### 记录3: 类级别JavaDoc注释补充

#### 背景说明

部分工具类、配置类缺少类级别的说明注释，影响代码可读性。

#### 提示词（Prompt）

```
对所有java文件的类定义前加入这个类的涵义（一句即可），格式要求:
1. 使用标准JavaDoc格式 /** */
2. 简洁明了，一句话说明类的作用
3. 确保所有类都有注释
```

#### AI输出摘要

Claude为所有缺少类注释的Java类添加了JavaDoc说明，涵盖:

- 常量类: 说明常量的用途和作用域
- 工具类: 说明工具类提供的功能
- 服务类: 说明服务的业务职责
- 控制器: 说明API的业务范围
- 配置类: 说明配置的目标和作用

#### 实际修改内容

为50+个类添加或完善了类级别注释，示例:

```java
/**
 * Token相关常量配置类
 */
public class TokenConstants { ... }

/**
 * 统一响应结果封装类
 */
public class Result<T> { ... }

/**
 * 任务工具类，提供任务相关的通用方法
 */
public class TaskUtil { ... }

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig { ... }
```

#### 证明材料

- Git提交记录: `docs: 为所有Java类添加类级别JavaDoc注释`
- 覆盖范围: 所有 `backend/src/main/java/` 下的 Java 类文件

------

### AI使用声明

本项目在开发过程中使用了Claude AI作为辅助工具，主要用于:

1. **代码生成**: 根据需求描述生成标准化的业务模块代码
2. **代码重构**: 统一代码风格、注释格式等
3. **架构设计**: 提供模块划分和接口设计建议

所有AI生成的代码都经过了人工审核、测试和必要的修改，确保代码质量和功能正确性。AI辅助开发提高了开发效率，但最终的技术决策和代码质量由开发者负责。

### 证明材料清单

为证明AI使用过程的真实性，项目保留了以下证明材料:

1. **Git提交历史**: 每次AI辅助的重要修改都有对应的commit记录
2. **代码对比**: 可通过Git历史查看AI生成代码的修改过程
3. **AI模型信息**: 记录使用的具体模型版本和平台

------

## 开发规范

### 代码风格

- 使用Lombok简化代码
- 统一使用JavaDoc注释格式
- 遵循RESTful API设计规范
- 使用统一的异常处理机制

### 命名规范

- Controller: `Controller`
- Service接口: `Service`
- Service实现: `ServiceImpl`
- DTO: `DTO`
- VO: `VO`
- Domain: ``entity``, ``mapper``

### 模块划分

每个业务模块包含:

- `controller/`: REST API控制器
- `service/`: 业务逻辑接口和实现
- `model/`: DTO和VO数据传输对象
- `domain/`: 实体类和Mapper（如需要）

## 许可证

本项目采用 MIT 许可证。

## 贡献

欢迎提交Issue和Pull Request。

------

**最后更新**: 2025年11月29日