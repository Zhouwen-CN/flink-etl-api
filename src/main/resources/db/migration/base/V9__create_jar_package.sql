create table T_JAR_PACKAGE
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(30)  not null comment 'jar包名称',
    file_name varchar(50)  not null comment '文件名',
    file_path varchar(200) not null comment 'jar包地址',
    main_class  varchar(100) not null comment '入口类',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_upload_jar_name_path (name)
) comment 'jar包管理表';