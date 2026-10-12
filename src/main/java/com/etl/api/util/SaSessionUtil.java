package com.etl.api.util;

import cn.dev33.satoken.stp.StpUtil;
import lombok.Data;

import java.util.List;

public final class SaSessionUtil {
    public static final String USER_INFO = "USER_INFO";

    public static SaSessionUserInfo getUserInfo() {
        try {
            return StpUtil.getSession().getModel(USER_INFO, SaSessionUserInfo.class);
        } catch (Exception e) {
            // 无登录态（如 Quartz 线程）时忽略
        }

        // 返回空对象
        return new SaSessionUserInfo();
    }

    private SaSessionUtil() {
    }

    public static void setUserInfo(SaSessionUserInfo userInfo) {
        StpUtil.getSession().set(USER_INFO, userInfo);
    }

    @Data
    public static class SaSessionUserInfo {
        // 用户名
        private String username;
        // 昵称
        private String nickname;
        // 用户所属部门
        private Long deptId;
        // 数据权限-所有
        private boolean dataScopeAll;
        // 数据权限-部门列表
        private List<Long> dataScopeDeptIds;
    }
}
