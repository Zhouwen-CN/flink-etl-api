package com.etl.api.domain.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * http请求历史表 实体类。
 *
 * @author chen
 * @since 2026-06-11
 */
@Data
@Builder
@Table(value = "T_HTTP_EXCHANGE_HISTORY")
public class HttpExchangeHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 请求url
     */
    private String requestUrl;

    /**
     * 请求ip
     */
    private String requestIp;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求头
     */
    private String requestHeaders;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应头
     */
    private String responseHeaders;

    /**
     * 花费时间
     */
    private Long takenTime;

    /**
     * 创建者
     */
    private String createUser;

}
