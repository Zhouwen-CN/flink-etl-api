package com.etl.api.config;

import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.core.tenant.TenantFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 部门数据权限租户工厂。
 *
 * <p>借助 MyBatis-Flex 多租户机制：对标注了 {@code @Column(tenantId = true)} 的实体，
 * 每次查询/更新/删除时自动调用 {@link #getTenantIds()} 生成附加条件 {@code dept_id IN (...)}。
 *
 * <ul>
 *     <li>全量（dataScopeAll=true）→ 返回 null，不附加部门条件（管理员兜底）</li>
 *     <li>非全量 → 返回登录会话中的可见部门 id 集合</li>
 *     <li>无登录态（异步线程等）→ 返回 null，不附加部门条件（由调用方按需用 withoutTenantCondition 包裹）</li>
 * </ul>
 *
 * <p>Spring Boot starter 会自动识别容器中的 {@link TenantFactory} Bean 并注册到 {@code TenantManager}。
 */
@Component
public class DataScopeTenantFactory implements TenantFactory {

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
}