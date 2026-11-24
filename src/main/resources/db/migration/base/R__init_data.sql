-- ============================================
-- 初始化数据（随 Flyway repeatable migration 每次变更后执行）
-- ============================================

-- 用户
-- admin 密码：admin123
-- zhangsan 密码：zhangsan123
insert into T_SYS_USER(id, username, password, nickname, gender, status)
values (1, 'admin', 'EnC/6IK+x2W2KNDMckXlmg==', '超级管理员', 0, 1);
insert into T_SYS_USER(id, username, password, nickname, gender, status)
values (2, 'zhangsan', 'FpbV8C6ZJxqGkDGUqYNqkQ==', '张三', 1, 1);

-- 角色
insert into T_SYS_ROLE(id, name, code, data_scope)
values (1, '管理员', 'admin', 1);
insert into T_SYS_ROLE(id, name, code)
values (2, '开发', 'dev');

-- 用户角色关系
insert into T_SYS_USER_ROLE(id, role_id, user_id)
values (1, 1, 1);
insert into T_SYS_USER_ROLE(id, role_id, user_id)
values (2, 2, 2);

-- 权限
-- admin
insert into T_SYS_PERMISSION(id, name, code, type)
values (1, '所有权限(管理员)', '*', 1);

-- sys user
insert into T_SYS_PERMISSION(id, name, code)
values (2, '查询用户', 'user.select');
insert into T_SYS_PERMISSION(id, name, code)
values (3, '新增用户', 'user.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (4, '更新用户', 'user.update');
insert into T_SYS_PERMISSION(id, name, code)
values (5, '删除用户', 'user.delete');

-- sys role
insert into T_SYS_PERMISSION(id, name, code)
values (6, '查询角色', 'role.select');
insert into T_SYS_PERMISSION(id, name, code)
values (7, '新增角色', 'role.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (8, '更新角色', 'role.update');
insert into T_SYS_PERMISSION(id, name, code)
values (9, '删除角色', 'role.delete');

-- sys permission
insert into T_SYS_PERMISSION(id, name, code)
values (10, '查询权限', 'permission.select');
insert into T_SYS_PERMISSION(id, name, code)
values (11, '新增权限', 'permission.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (12, '更新权限', 'permission.update');
insert into T_SYS_PERMISSION(id, name, code)
values (13, '删除权限', 'permission.delete');

-- sys dept
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (14, '查询部门', 'dept.select');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (15, '新增部门', 'dept.insert');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (16, '更新部门', 'dept.update');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (17, '删除部门', 'dept.delete');

-- sys dict
insert into T_SYS_PERMISSION(id, name, code)
values (18, '查询字典', 'dict.select');
insert into T_SYS_PERMISSION(id, name, code)
values (19, '新增字典', 'dict.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (20, '更新字典', 'dict.update');
insert into T_SYS_PERMISSION(id, name, code)
values (21, '删除字典', 'dict.delete');

-- sys variable
insert into T_SYS_PERMISSION(id, name, code)
values (22, '查询变量', 'variable.select');
insert into T_SYS_PERMISSION(id, name, code)
values (23, '新增变量', 'variable.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (24, '更新变量', 'variable.update');
insert into T_SYS_PERMISSION(id, name, code)
values (25, '删除变量', 'variable.delete');

-- etl project
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (26, '查询项目', 'project.select');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (27, '新增项目', 'project.insert');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (28, '更新项目', 'project.update');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (29, '删除项目', 'project.delete');

-- etl job
insert into T_SYS_PERMISSION(id, name, code)
values (30, '查询任务', 'job.select');
insert into T_SYS_PERMISSION(id, name, code)
values (31, '新增任务', 'job.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (32, '更新任务', 'job.update');
insert into T_SYS_PERMISSION(id, name, code)
values (33, '删除任务', 'job.delete');

-- etl instance
insert into T_SYS_PERMISSION(id, name, code)
values (34, '查询实例', 'instance.select');
insert into T_SYS_PERMISSION(id, name, code)
values (35, '新增实例', 'instance.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (36, '更新实例', 'instance.update');
insert into T_SYS_PERMISSION(id, name, code)
values (37, '删除实例', 'instance.delete');

-- etl schedule
insert into T_SYS_PERMISSION(id, name, code)
values (38, '查询定时', 'schedule.select');
insert into T_SYS_PERMISSION(id, name, code)
values (39, '新增定时', 'schedule.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (40, '更新定时', 'schedule.update');
insert into T_SYS_PERMISSION(id, name, code)
values (41, '删除定时', 'schedule.delete');

-- etl alert
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (42, '查询告警', 'alert.select');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (43, '新增告警', 'alert.insert');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (44, '更新告警', 'alert.update');
INSERT INTO T_SYS_PERMISSION (id, name, code)
VALUES (45, '删除告警', 'alert.delete');

-- etl cluster
insert into T_SYS_PERMISSION(id, name, code)
values (46, '查询集群', 'cluster.select');
insert into T_SYS_PERMISSION(id, name, code)
values (47, '新增集群', 'cluster.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (48, '更新集群', 'cluster.update');
insert into T_SYS_PERMISSION(id, name, code)
values (49, '删除集群', 'cluster.delete');

-- etl jar
insert into T_SYS_PERMISSION(id, name, code)
values (50, '查询jar', 'jar.select');
insert into T_SYS_PERMISSION(id, name, code)
values (51, '新增jar', 'jar.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (52, '更新jar', 'jar.update');
insert into T_SYS_PERMISSION(id, name, code)
values (53, '删除jar', 'jar.delete');

-- sys log
insert into T_SYS_PERMISSION(id, name, code)
values (54, '查询登入日志', 'login-log.select');
insert into T_SYS_PERMISSION(id, name, code)
values (55, '查询错误日志', 'error-log.select');
insert into T_SYS_PERMISSION (id, name, code)
values (56, '查询请求日志', 'request-log.select');

-- ai chat
insert into T_SYS_PERMISSION(id, name, code)
values (57, '查询对话', 'chat.select');
insert into T_SYS_PERMISSION(id, name, code)
values (58, '新增对话', 'chat.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (59, '更新对话', 'chat.update');
insert into T_SYS_PERMISSION(id, name, code)
values (60, '删除对话', 'chat.delete');

-- ai bot
insert into T_SYS_PERMISSION(id, name, code)
values (61, '查询机器人', 'bot.select');
insert into T_SYS_PERMISSION(id, name, code)
values (62, '新增机器人', 'bot.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (63, '更新机器人', 'bot.update');
insert into T_SYS_PERMISSION(id, name, code)
values (64, '删除机器人', 'bot.delete');

-- ai model
insert into T_SYS_PERMISSION(id, name, code)
values (65, '查询模型', 'model.select');
insert into T_SYS_PERMISSION(id, name, code)
values (66, '新增模型', 'model.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (67, '更新模型', 'model.update');
insert into T_SYS_PERMISSION(id, name, code)
values (68, '删除模型', 'model.delete');

-- ai mcp
insert into T_SYS_PERMISSION(id, name, code)
values (69, '查询MCP', 'mcp.select');
insert into T_SYS_PERMISSION(id, name, code)
values (70, '新增MCP', 'mcp.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (71, '更新MCP', 'mcp.update');
insert into T_SYS_PERMISSION(id, name, code)
values (72, '删除MCP', 'mcp.delete');

-- ai knowledge
insert into T_SYS_PERMISSION(id, name, code)
values (73, '查询知识库', 'knowledge.select');
insert into T_SYS_PERMISSION(id, name, code)
values (74, '新增知识库', 'knowledge.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (75, '更新知识库', 'knowledge.update');
insert into T_SYS_PERMISSION(id, name, code)
values (76, '删除知识库', 'knowledge.delete');

-- ai doc
insert into T_SYS_PERMISSION(id, name, code)
values (77, '查询文档', 'doc.select');
insert into T_SYS_PERMISSION(id, name, code)
values (78, '新增文档', 'doc.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (79, '更新文档', 'doc.update');
insert into T_SYS_PERMISSION(id, name, code)
values (80, '删除文档', 'doc.delete');

-- ai vectordb
insert into T_SYS_PERMISSION(id, name, code)
values (81, '查询向量库', 'vectordb.select');
insert into T_SYS_PERMISSION(id, name, code)
values (82, '新增向量库', 'vectordb.insert');
insert into T_SYS_PERMISSION(id, name, code)
values (83, '更新向量库', 'vectordb.update');
insert into T_SYS_PERMISSION(id, name, code)
values (84, '删除向量库', 'vectordb.delete');

-- 角色权限关系
insert into T_SYS_ROLE_PERMISSION(id, role_id, permission_id)
values (1, 1, 1);

-- 部门
insert into T_SYS_DEPT(id, name, parent_path)
values (1, '海勃数科', '/1/');

-- 字典类型
insert into T_SYS_DICT_TYPE(id, name, remark, status)
values (1, 'sys_user_gender', '性别', 1);
insert into T_SYS_DICT_TYPE(id, name, remark, status)
values (2, 'sys_data_scope', '数据范围', 1);
insert into T_SYS_DICT_TYPE(id, name, remark, status)
values (3, 'etl_job_type', '任务类型', 1);
insert into T_SYS_DICT_TYPE(id, name, remark, status)
values (4, 'ai_api_format', 'ai接口格式', 1);
insert into T_SYS_DICT_TYPE(id, name, remark, status)
values (5, 'ai_model_type', 'ai模型类型', 1);
insert into T_SYS_DICT_TYPE(id, name, remark, status)
values (6, 'ai_vectordb_type', 'ai向量数据库类型', 1);

-- 字典数据
-- 性别
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (1, 1, '未知', 0, 0);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (2, 1, '男', 1, 1);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (3, 1, '女', 2, 2);

-- 数据范围
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (4, 2, '全部', 1, 1);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (5, 2, '本部门及以下', 2, 2);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (6, 2, '本部门', 3, 3);

-- 任务类型
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (7, 3, 'BATCH', 1, 1);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (8, 3, 'STREAMING', 2, 2);

-- ai接口格式
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (9, 4, 'OpenAI Chat Completions', 1, 1);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (10, 4, 'OpenAI Responses', 2, 2);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (11, 4, 'Anthropic Messages', 3, 3);

-- ai模型类型
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (12, 5, 'Chat', 1, 1);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (13, 5, 'Embedding', 2, 2);

-- ai向量数据库类型
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (14, 6, 'Redis', 1, 1);
insert into T_SYS_DICT_DATA(id, type_id, label, `value`, sort_id)
values (15, 6, 'Milvus', 2, 2);

-- 变量配置
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (1, 'now', '#now()', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (2, 'now_format', '#now_format("yyyy-MM-dd")', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (3, 'add_month', '#add_month(#now(),-1)', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (4, 'add_month_format', '#add_month_format(#now(),-1,"yyyy-MM-dd")', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (5, 'add_day', '#add_day(#now(),-1)', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (6, 'add_day_format', '#add_day_format(#now(),-1,"yyyy-MM-dd")', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (7, 'add_hour', '#add_hour(#now(),-1)', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (8, 'add_hour_format', '#add_hour_format(#now(),-1,"yyyy-MM-dd HH")', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (9, 'add_minute', '#add_minute(#now(),-1)', 0);
INSERT INTO T_SYS_VAR_SETTING (id, name, `value`, status)
VALUES (10, 'add_minute_format', '#add_minute_format(#now(),-1,"yyyy-MM-dd HH:mm")', 0);
