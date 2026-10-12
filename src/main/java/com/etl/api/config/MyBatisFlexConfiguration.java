package com.etl.api.config;

import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.tenant.TenantFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.List;

/**
 * mysql flex sql日志
 */
@Slf4j
@Configuration
public class MyBatisFlexConfiguration {

    public MyBatisFlexConfiguration(Environment environment) {
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            //开启审计功能
            AuditManager.setAuditEnable(true);

            //设置 SQL 审计收集器
            MessageCollector collector = new ConsoleMessageCollector();
            AuditManager.setMessageCollector(collector);
        }
    }


    /**
     * 部门数据权限租户工厂。
     *
     * <pre>
     *  借助 MyBatis-Flex 多租户机制：对标注了 {@code @Column(tenantId = true)} 的实体，
     *  select 时 自动调用 {@link TenantFactory#getTenantIds()} 生成附加条件 {@code dept_id IN (...)}。
     *  insert、update，如果对应的租户字段有值的话，不会进行覆盖填充
     * </pre>
     *
     * <ul>
     *     <li>全量（dataScopeAll=true）→ 返回 null，不附加部门条件（管理员兜底）</li>
     *     <li>非全量 → 返回登录会话中的可见部门 id 集合</li>
     *     <li>无登录态（异步线程等）→ 返回 null，不附加部门条件</li>
     * </ul>
     */
    @Bean
    public TenantFactory tenantFactory() {
        return () -> {
            val userInfo = SaSessionUtil.getUserInfo();
            if (userInfo.isDataScopeAll()) {
                return null; // 全量忽略租户条件
            }
            List<Long> deptIds = userInfo.getDataScopeDeptIds();
            if (deptIds == null || deptIds.isEmpty()) {
                return null;
            }
            return deptIds.toArray();
        };
    }
}