create table T_LOGIN_CAPTCHA
(
    id          varchar(35) primary key comment 'UUID',
    code        varchar(10) not null comment '验证码',
    create_time datetime default CURRENT_TIMESTAMP comment '创建时间'
) comment '登入验证码表';