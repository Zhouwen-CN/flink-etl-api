package com.etl.api.aop;

import com.mybatisflex.core.tenant.TenantManager;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 忽略租户条件切面
 *
 * <p>拦截标注了 {@link IgnoreTenant} 的方法（或类），
 * 执行期间设置 MyBatis-Flex 忽略租户标志，结束后恢复</p>
 */
@Aspect
@Component
public class IgnoreTenantAspect {

    @Around("@annotation(com.etl.api.aop.IgnoreTenant) || @within(com.etl.api.aop.IgnoreTenant)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 记录外层状态，支持嵌套调用（内层结束不误清外层标志）
        val outerIgnore = TenantManager.isIgnoreTenantCondition();
        TenantManager.ignoreTenantCondition();
        try {
            return pjp.proceed();
        } finally {
            if (!outerIgnore) {
                TenantManager.restoreTenantCondition();
            }
        }
    }
}