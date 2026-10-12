package com.etl.api.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略租户（部门数据权限）条件注解
 *
 * <p>标注在方法或类上，执行期间 MyBatis-Flex 忽略租户条件：
 * 查询/更新/删除不再附加部门过滤条件，update 时租户列（deptId）也不会被过滤</p>
 *
 * <p>典型场景：新增/修改需要跨部门校验（如名称唯一性），或需要修改实体部门归属</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreTenant {
}