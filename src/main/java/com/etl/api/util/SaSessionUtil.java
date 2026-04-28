package com.etl.api.util;

import cn.dev33.satoken.stp.StpUtil;

public final class SaSessionUtil {

    public static final String PRINCIPAL = "principal";

    private SaSessionUtil() {
    }

    public static String getPrincipal() {
        return StpUtil.getSession().getString(PRINCIPAL);
    }

    public static void setPrincipal(String principal) {
        StpUtil.getSession().set(PRINCIPAL, principal);
    }
}
