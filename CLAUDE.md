# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring Boot 3.5 的 Flink ETL API 应用，提供了 HTTP 请求监控、IP 地址解析等功能。项目集成了 Spring Boot Admin 用于监控和管理，使用 MyBatis-Flex 作为 ORM 框架。

## 核心技术栈

- **Java 17** - JDK 版本
- **Spring Boot 3.5.13** - 基础框架
- **MyBatis-Flex 1.11.6** - ORM 框架，替代 MyBatis-Plus
- **Flyway** - 数据库版本控制和迁移
- **H2 Database** - 开发环境数据库（支持 MySQL 模式）
- **Spring Doc (OpenAPI)** - API 文档生成
- **Spring Boot Admin** - 应用监控和管理
- **Lombok + MapStruct** - 代码生成和简化
- **IP2Region** - IP 地址解析库

## 常用命令

### 构建和运行

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目（生成可执行 JAR）
mvn clean package

# 运行应用（开发环境）
mvn spring-boot:run

# 运行应用（生产环境）
java -jar target/flink-etl-api-0.0.1-SNAPSHOT.jar
```

### 数据库相关

数据库迁移脚本位于 `src/main/resources/db/mysql/` 目录下，Flyway 会在应用启动时自动执行迁移。

开发环境使用 H2 数据库，可通过以下端点访问控制台：
- URL: `/h2`
- JDBC URL: `jdbc:h2:file:./database;MODE=MySQL;DB_CLOSE_DELAY=-1`
- Username: `sa`
- Password: `123`

生产环境需要配置以下环境变量：
- `JDBC_DRIVER`
- `JDBC_URL`
- `JDBC_USERNAME`
- `JDBC_PASSWORD`

### 代码生成

MyBatis-Flex 提供了代码生成器，可以基于数据库表生成 Entity、Mapper、Service 等代码。参见 `FlinkEtlApiApplicationTests.contextLoads()` 测试方法中的配置示例。

## 项目架构

### 分层结构

```
com.etl.api
├── config/          # 配置类（MyBatis、Endpoint、数据源等）
├── controller/      # REST API 控制器
├── domain/          # 领域对象
│   ├── base/        # 基础实体和监听器
│   ├── entity/      # 实体类（对应数据库表）
│   └── vo/          # 视图对象（ResponseVO、PageVO）
├── exception/       # 异常处理
├── mapper/          # MyBatis Mapper 接口
├── service/         # 业务服务接口
│   └ impl/          # 服务实现
└── util/            # 工具类
```

### 核心设计模式

1. **统一响应格式**：所有 API 使用 `ResponseVO<T>` 作为标准响应格式，包含 `success`、`code`、`data`、`message` 字段。

2. **全局异常处理**：`GlobalExceptionHandler` 统一处理参数校验异常和业务异常，返回标准错误响应。

3. **MyBatis-Flex Service 模式**：Service 实现类继承 `ServiceImpl<Mapper, Entity>`，提供了链式查询方法 `queryChain()`。

4. **实体监听器**：通过 `InsertListener` 和 `UpdateListener` 自动填充实体的创建时间和更新时间字段（继承 `BaseEntity` 的实体）。

5. **HTTP Exchange 监控**：自定义 `HttpExchangeRepository` 实现将 HTTP 请求历史记录存储到数据库，并过滤特定 URL（如 `/actuator`、`/h2`、`/swagger-ui`）。

## 配置要点

### 多环境配置

- `application.yaml` - 公共配置
- `application-dev.yaml` - 开发环境（H2 数据库）
- `application-prod.yaml` - 生产环境（MySQL，通过环境变量配置）

### Actuator 端点

应用暴露了完整的 Actuator 端点用于监控：
- `/actuator` - 所有监控端点
- `/actuator/info` - 构建信息、Git 信息
- `/actuator/health` - 健康检查
- `/actuator/httpexchanges` - HTTP 请求历史
- `/actuator/prometheus` - Prometheus 指标

### Spring Doc 配置

API 文档通过 Spring Doc 自动生成：
- Swagger UI: `/swagger-ui`
- API Docs: `/v3/api-docs`

开发环境允许在 Swagger UI 中执行 "Try it out"，生产环境禁用。

### 数据源配置

使用 HikariCP 连接池，关键配置参数：
- `maxPoolSize`: 5
- `minIdle`: 1
- `connectionTimeout`: 5000ms
- `idleTimeout`: 600000ms (10分钟)
- `maxLifetime`: 1200000ms (20分钟)

## 可观测性集成

项目集成了完整的可观测性功能：
1. **Spring Boot Admin Server** - 自托管监控平台（`@EnableAdminServer`）
2. **Spring Boot Admin Client** - 应用自身作为客户端注册到 Admin Server
3. **Micrometer + Prometheus** - 指标收集和导出
4. **Actuator** - 健康检查、信息暴露、HTTP 请求追踪

## 依赖注入和注解处理

项目使用 Lombok 和 MapStruct 进行代码生成，需特别注意：
- Lombok 的 `@RequiredArgsConstructor` 用于构造器注入
- MapStruct 用于对象映射转换
- 注解处理器配置在 `pom.xml` 的 `maven-compiler-plugin` 中，顺序很重要（Lombok 必须在 MapStruct 之前）

## 数据库表命名规范

数据库表使用 `T_` 前缀（如 `T_HTTP_EXCHANGE_HISTORY`），实体类使用驼峰命名（如 `HttpExchangeHistory`）。MyBatis-Flex 配置了表前缀策略，生成代码时会自动去除前缀。