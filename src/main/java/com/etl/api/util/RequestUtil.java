package com.etl.api.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * <p>
 * 请求对象工具
 * </p>
 *
 * @author chen
 * @since 2025-05-29
 */
public final class RequestUtil {

    private RequestUtil() {
    }

    /**
     * 从线程变量中获取 request 对象
     *
     * @return request
     */
    public static HttpServletRequest getHttpServletRequest() {
        val requestAttributes = Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
}
