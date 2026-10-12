package com.etl.api.config;

import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.tenant.TenantFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.List;
import java.util.Set;

/**
 * mysql flex sql日志
 */
@Slf4j
@Configuration
public class MyBatisFlexConfiguration {

    private final Set<String> TABLES = Set.of(
            "t_etl_project",
            "t_etl_job",
            "t_etl_job_instance",
            "t_schedule_job"
    );


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
     * <p>借助 MyBatis-Flex 多租户机制：对标注了 {@code @Column(tenantId = true)} 的实体，
     * 每次查询/更新/删除时自动调用 {@link TenantFactory#getTenantIds(String)} 生成附加条件 {@code dept_id IN (...)}。
     *
     * <ul>
     *     <li>全量（dataScopeAll=true）→ 返回 null，不附加部门条件（管理员兜底）</li>
     *     <li>非全量 → 返回登录会话中的可见部门 id 集合</li>
     *     <li>无登录态（异步线程等）→ 返回 null，不附加部门条件</li>
     * </ul>
     *
     * <p>Spring Boot starter 会自动识别容器中的 {@link TenantFactory} Bean 并注册到 {@code TenantManager}。
     */
    @Bean
    public TenantFactory tenantFactory() {
        return new TenantFactory() {
            @Override
            public Object[] getTenantIds() {
                if (SaSessionUtil.isDataScopeAll()) {
                    return null; // 全量忽略租户条件
                }
                List<Long> deptIds = SaSessionUtil.getDeptIds();
                if (deptIds == null || deptIds.isEmpty()) {
                    return null;
                }
                return deptIds.toArray();
            }

            @Override
            public Object[] getTenantIds(String tableName) {
                if (TABLES.contains(tableName.toLowerCase())) {
                    return this.getTenantIds();
                }
                return null;
            }
        };
    }
}