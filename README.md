# Flink ETL API

一个轻量级的 Flink 任务管理平台，类似 [Apache StreamPark](https://streampark.apache.org/)，用于管理和调度 Flink ETL 任务。

配合 [flink-etl-tool](https://gitee.com/Zhouwen-CN/flink-etl-tool) 项目使用 —— 将 flink-etl-tool 打包的 JAR 通过本平台上传并提交到
Flink 集群运行。

## 功能特性

- **Flink 集群管理** - 注册和管理多个 Flink 集群，通过 JobManager REST API 通信
- **JAR 包管理** - 上传、更新 ETL 任务 JAR 包，自动管理集群上传缓存
- **ETL 任务编排** - 创建 BATCH/STREAMING 类型任务，关联集群、JAR 和 JSON 配置
- **任务运行与监控** - 提交任务到 Flink、实时状态同步、从 Checkpoint/Savepoint 恢复
- **定时调度** - 基于 Quartz（JDBC 持久化）的 Cron 定时任务，支持集群模式
- **RBAC 权限控制** - 基于 Sa-Token 的用户/角色/权限管理，JWT Token 认证
- **可观测性** - 集成 Spring Boot Admin、Prometheus 指标、HTTP 请求追踪
- **数据字典** - 通用字典管理，支持业务枚举配置化
- **操作日志** - 登录日志、错误日志记录与查询

## 技术栈

| 组件                | 版本     | 说明              |
|-------------------|--------|-----------------|
| Java              | 17     | JDK 版本          |
| Spring Boot       | 3.5.13 | 基础框架            |
| MyBatis-Flex      | 1.11.6 | ORM 框架          |
| Sa-Token          | 1.45.0 | 认证授权（JWT 模式）    |
| Quartz            | -      | 定时调度（JDBC 持久化）  |
| Flyway            | -      | 数据库版本迁移         |
| MapStruct         | 1.6.3  | 对象映射            |
| Spring Boot Admin | 3.5.8  | 应用监控            |
| Spring Doc        | 2.8.17 | API 文档（OpenAPI） |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- （可选）MySQL 8.0+（生产环境）

### 开发环境

开发环境使用 H2 内存数据库（MySQL 兼容模式），无需额外安装数据库。

```bash
# 克隆项目
git clone https://gitee.com/Zhouwen-CN/flink-etl-api.git
cd flink-etl-api

# 编译并运行
mvn spring-boot:run
```

启动后访问：

- 应用接口：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui
- H2 控制台：http://localhost:8080/h2-console
- Spring Boot Admin：http://localhost:8080/applications

### 默认账号

| 用户名      | 密码          | 角色    |
|----------|-------------|-------|
| admin    | admin123    | 超级管理员 |
| zhangsan | zhangsan123 | 普通用户  |

### 生产环境

```bash
# 打包
mvn clean package

# 运行（配置 MySQL 数据源）
java -jar target/flink-etl-api-0.0.1-SNAPSHOT.jar \
  --JDBC_DRIVER=com.mysql.cj.jdbc.Driver \
  --JDBC_URL=jdbc:mysql://localhost:3306/flink_etl?useSSL=false&serverTimezone=Asia/Shanghai \
  --JDBC_USERNAME=root \
  --JDBC_PASSWORD=your_password
```

也可以通过环境变量设置：`JDBC_DRIVER`、`JDBC_URL`、`JDBC_USERNAME`、`JDBC_PASSWORD`。

## 使用流程

```
1. 注册 Flink 集群        →  填写 JobManager URL，自动获取版本信息
2. 上传 JAR 包            →  上传 flink-etl-tool 打包的 JAR，指定 Main Class
3. 创建 ETL 任务          →  选择集群、JAR，配置任务参数（JSON），选择 BATCH/STREAMING 类型
4. 运行任务               →  手动运行 或 创建 Cron 定时调度
5. 监控任务实例            →  查看运行状态、取消任务、从 Checkpoint 恢复
```

## API 文档

启动应用后访问 Swagger UI 查看完整 API 文档：http://localhost:8080/swagger-ui

主要接口：

| 路径                            | 说明         |
|-------------------------------|------------|
| `POST /auth/login`            | 登录获取 Token |
| `/flink/cluster`              | Flink 集群管理 |
| `/jar`                        | JAR 包管理    |
| `/job`                        | ETL 任务管理   |
| `/instance`                   | 任务实例管理     |
| `/schedule`                   | 定时调度管理     |
| `/user`、`/role`、`/permission` | 用户权限管理     |
| `/dict/type`、`/dict/data`     | 字典管理       |
| `/log`                        | 日志查询       |

接口认证方式：请求头携带 `Authorization: Bearer <token>`。

## 项目结构

```
src/main/java/com/etl/api/
├── controller/          # REST API 控制器
├── service/
│   ├── impl/            # 业务服务实现
│   ├── manager/         # 业务编排（EtlJobManager、ScheduleJobManager）
│   └── provider/        # Flink API 集成（FlinkApiProvider）
├── domain/              # 实体、表单、VO、转换器
├── config/              # 配置类
├── mapper/              # MyBatis Mapper
├── scheduler/           # 定时任务（状态同步、过期清理）
└── util/                # 工具类

src/main/resources/
├── db/migration/
│   ├── base/            # 基础表迁移脚本（V1 ~ V14）
│   ├── h2/              # H2 专用脚本（Quartz 表）
│   └── mysql/           # MySQL 专用脚本（Quartz 表）
├── application.yaml     # 公共配置
├── application-dev.yaml # 开发环境（H2）
└── application-prod.yaml# 生产环境（MySQL）
```

## 相关项目

- [flink-etl-tool](https://gitee.com/Zhouwen-CN/flink-etl-tool) - Flink ETL 工具包，本平台管理的 JAR 包由该项目构建

## License

MIT
