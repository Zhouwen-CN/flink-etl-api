package com.etl.api.util;

import lombok.val;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public final class SPELUtil {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private SPELUtil() {
    }

    public static <T> T parseExpression(String expr, Class<T> clazz) {
        val expression = parser.parseExpression(expr);
        return expression.getValue(clazz);
    }

    public static <T> T parseExpression(String expr, Class<T> clazz, T defaultValue) {
        T result = null;
        try {
            result = parseExpression(expr, clazz);
        } catch (Exception e) {
            // do nothing
        }
        return result == null ? defaultValue : result;
    }
}
