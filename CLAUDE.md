# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于 Spring Boot 3.5 的 Flink ETL 管理平台，提供 Flink 集群管理、JAR 包管理、ETL 任务编排与调度、任务实例监控等功能。应用自带
Spring Boot Admin 监控、Sa-Token RBAC 认证授权、Quartz 动态定时调度。

## 核心技术栈

- **Java 17** / **Spring Boot 3.5.13**
- **MyBatis-Flex 1.11.6** - ORM 框架，Service 继承 `ServiceImpl<Mapper, Entity>`，支持 `queryChain()` 链式查询
- **Sa-Token 1.45.0** - JWT Token 模式认证授权，`@SaCheckPermission` 控制接口权限
- **Quartz (JDBC 持久化)** - 动态定时调度 ETL 任务，支持集群模式
- **Flyway** - 数据库版本迁移
- **MapStruct 1.6.3 + Lombok** - 对象转换和代码简化（注解处理器顺序：Lombok → MapStruct）
- **Spring Boot Admin** - 自托管监控（既是 Server 又是 Client）
- **H2 Database** - 开发环境（MySQL 兼容模式）
- **Hutool / IP2Region** - 工具库和 IP 地址解析

## 常用命令

```bash
mvn clean compile                                      # 编译
mvn test                                               # 运行所有测试
mvn test -Dtest=FlinkEtlApiApplicationTests            # 运行单个测试类
mvn test -Dtest=FlinkEtlApiApplicationTests#contextLoads  # 运行单个方法
mvn clean package                                      # 打包
mvn spring-boot:run                                    # 开发环境运行
```

## 核心业务流程

### ETL 任务生命周期

1. 管理员注册 Flink 集群（`FlinkCluster`，含 JobManager URL）
2. 上传 JAR 包（`JarPackage`，含 mainClass）
3. 创建 ETL 任务（`EtlJob`，关联集群、JAR、JSON 配置，类型：BATCH/STREAMING）
4. 手动运行或通过 Quartz 定时触发：
   - `EtlJobManager` 校验集群可用性，管理 JAR 上传缓存（`ClusterUploadedJar`）
   - `FlinkApiProvider` 通过 Flink REST API 提交任务，返回 Flink jobId
   - 创建 `EtlJobInstance` 记录
5. 后台定时任务 `SyncJobInstanceStatus` 每 5 秒轮询 Flink API 同步状态
6. 用户可查看实例状态、取消运行中的任务、从 Checkpoint 恢复

### Flink REST API 集成

`FlinkApiProvider` 通过 `RestClient` 与 Flink JobManager 通信：

- `/config` - 获取集群版本
- `/jars/upload` - 上传 JAR
- `/jars/{jarId}/run` - 提交任务（配置 Base64 编码，支持 savepoint 恢复）
- `/jobs/{jobId}` - 查询任务状态
- `/jobs/{jobId}/stop` - 停止任务
- `/jobs/{jobId}/checkpoints` - 获取 checkpoint 历史

### 双调度系统

- **@Scheduled 固定任务**（`scheduler/` 包）：清理过期记录（30min）、同步集群 JAR 列表（10min）、同步任务实例状态（5s）
- **Quartz 动态调度**（JDBC 持久化、集群模式）：用户通过 `/schedule` API 创建/管理 cron 定时任务，`ScheduleJobHandler` 触发
  ETL 任务执行

## 项目架构

### 关键分层

```
com.etl.api
├── controller/          # REST API（13 个控制器）
├── service/
│   ├── impl/            # 业务服务实现（继承 ServiceImpl）
│   ├── manager/         # 业务编排（EtlJobManager、ScheduleJobManager）
│   └── provider/        # 外部 API 集成（FlinkApiProvider）
├── domain/
│   ├── entity/          # 数据库实体（T_ 前缀表）
│   ├── form/            # 请求表单（含 Bean Validation）
│   ├── convert/         # MapStruct 转换器
│   ├── vo/              # 响应视图对象
│   ├── base/            # BaseEntity + InsertListener/UpdateListener（自动填充时间）
│   └── validator/       # 自定义验证器（FieldMatch、CronExpression）
├── config/              # 配置类（Sa-Token、Quartz、RestClient、数据源等）
├── aop/                 # @NonTransaction 注解 + TransactionAspect
├── mapper/              # MyBatis Mapper 接口
├── enumeration/         # 枚举（ETLJobTypeEnum、FlinkJobStatusEnum 等）
├── exception/           # GlobalExceptionHandler 统一异常处理
├── scheduler/           # @Scheduled 定时任务
└── util/                # 工具类
```

### 核心设计模式

- **统一响应**：所有 API 返回 `ResponseVO<T>`（success/code/data/message）
- **三层业务编排**：Controller → Manager（编排逻辑）→ Service（CRUD）→ Mapper
- **实体监听器**：继承 `BaseEntity` 的实体自动填充 `createTime`/`updateTime`
- **AOP 事务控制**：默认 `@Transactional`，可通过 `@NonTransaction` 注解跳过
- **权限模型**：User → Role → Permission，权限码格式 `resource.action`（如 `job.select`、`instance.delete`）

## 数据库

### 迁移脚本

- 基础表：`src/main/resources/db/migration/base/`（V1 ~ V15）
- Quartz 表：`src/main/resources/db/migration/{vendor}/`（H2 和 MySQL 脚本不通用，`{vendor}` 由 Flyway 按数据库类型自动解析；使用 `R__` 重复迁移）

### 开发环境（H2）

- 控制台：`/h2-console`
- JDBC URL：`jdbc:h2:file:./database;MODE=MySQL;DB_CLOSE_DELAY=-1`
- 用户名：`sa`，密码：`123`

### 生产环境

通过环境变量配置：`JDBC_DRIVER`、`JDBC_URL`、`JDBC_USERNAME`、`JDBC_PASSWORD`

### 表命名规范

数据库表使用 `T_` 前缀（如 `T_ETL_JOB`），MyBatis-Flex 配置了表前缀策略，实体类使用驼峰命名（如 `EtlJob`）。

## REST API 端点

| 路径                        | 控制器                                   | 说明                        |
|---------------------------|---------------------------------------|---------------------------|
| `/auth`                   | AuthController                        | 登录/登出/验证码                 |
| `/job`                    | ETLJobController                      | ETL 任务 CRUD、运行、checkpoint |
| `/instance`               | ETLJobInstanceController              | 任务实例查询、取消                 |
| `/flink/cluster`          | FlinkClusterController                | Flink 集群管理                |
| `/jar`                    | JarPackageController                  | JAR 包上传管理                 |
| `/schedule`               | ScheduleJobController                 | Quartz 定时任务管理             |
| `/user`                   | UserController                        | 用户管理、密码修改                 |
| `/role`                   | RoleController                        | 角色管理                      |
| `/permission`             | PermissionController                  | 权限管理                      |
| `/dict/type`、`/dict/data` | DictTypeController、DictDataController | 字典管理                      |
| `/variable`               | JobVariableController                 | 任务变量管理（用于 ETL 配置占位符替换）   |
| `/log`                    | LogController                         | 登录日志、错误日志查询               |

## 配置要点

- 多环境：`application.yaml`（公共）、`application-dev.yaml`（H2）、`application-prod.yaml`（MySQL）
- Sa-Token：JWT Token，`Authorization: Bearer <token>`，单设备登录，30 天过期
- Quartz：JDBC 持久化，集群模式（`isClustered: true`），misfire 阈值 60s
- 文件上传：最大 200MB
- Swagger UI：`/swagger-ui`（开发环境可 Try it out，生产禁用）
- Spring Boot Admin：`/applications`（自监控）
- EndpointConfiguration：dev 环境用 `InMemoryHttpExchangeRepository`，prod 环境持久化到数据库

## 代码生成

MyBatis-Flex 代码生成器配置示例在 `FlinkEtlApiApplicationTests.contextLoads()` 中，可基于数据库表生成
Entity/Mapper/Service 代码。
