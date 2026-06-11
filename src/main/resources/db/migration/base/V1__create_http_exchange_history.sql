create table T_HTTP_EXCHANGE_HISTORY
(
    id               bigint primary key auto_increment comment '自增主键',
    timestamp        bigint       not null comment '时间戳',
    request_url      varchar(200) not null comment '请求url',
    request_ip       varchar(20) default null comment '请求ip',
    request_method   varchar(10)  not null comment '请求方法',
    request_headers  text        default null comment '请求头',
    response_status  int          not null comment '响应状态码',
    response_headers text        default null comment '响应头',
    taken_time       bigint       not null comment '花费时间',
    create_user varchar(30) default null comment '创建者',
    index idx_http_exchange_timestamp (timestamp desc)
) comment 'http请求历史表';