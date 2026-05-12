package com.etl.api.util;

import cn.dev33.satoken.stp.StpUtil;

public final class SaSessionUtil {

    public static final String USERNAME = "username";
    public static final String NICKNAME = "nickname";

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
}
