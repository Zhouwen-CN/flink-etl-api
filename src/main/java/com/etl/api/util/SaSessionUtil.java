package com.etl.api.util;

import cn.dev33.satoken.stp.StpUtil;

import java.util.ArrayList;
import java.util.List;

public final class SaSessionUtil {

    public static final String USERNAME = "username";
    public static final String NICKNAME = "nickname";
    public static final String DATA_SCOPE_ALL = "dataScopeAll";
    public static final String DEPT_IDS = "deptIds";

    private SaSessionUtil() {
    }

    public static String getUsername() {
        try {
            return StpUtil.getSession().getString(USERNAME);
        } catch (Exception ex) {
            // do nothing
        }
        return null;
    }

    public static void setUsername(String username) {
        StpUtil.getSession().set(USERNAME, username);
    }

    public static String getNickname() {
        return StpUtil.getSession().getString(NICKNAME);
    }

    public static void setNickname(String nickname) {
        StpUtil.getSession().set(NICKNAME, nickname);
    }

    /**
     * 全量数据范围标志（true = 不限制部门）
     */
    public static boolean isDataScopeAll() {
        try {
            Object value = StpUtil.getSession().get(DATA_SCOPE_ALL);
            if (value instanceof Boolean all) {
                return all;
            }
        } catch (Exception ex) {
            // 无登录态（如 Quartz 线程）时忽略
        }
        return false;
    }

    public static void setDataScopeAll(boolean all) {
        StpUtil.getSession().set(DATA_SCOPE_ALL, all);
    }

    public static List<Long> getDeptIds() {
        try {
            Object value = StpUtil.getSession().get(DEPT_IDS);
            if (value instanceof List<?> list) {
                List<Long> result = new ArrayList<>(list.size());
                for (Object item : list) {
                    if (item instanceof Number number) {
                        result.add(number.longValue());
                    }
                }
                return result;
            }
        } catch (Exception ex) {
            // 无登录态（如 Quartz 线程）时忽略
        }
        return null;
    }

    public static void setDeptIds(List<Long> deptIds) {
        StpUtil.getSession().set(DEPT_IDS, deptIds);
    }
}
