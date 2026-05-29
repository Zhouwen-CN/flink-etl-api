package com.etl.api.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * <pre>
 * SPEL 表达式日期函数
 *
 *   #now(): 当前日期
 *   #now_format('yyyy-MM-dd'): 当前日期
 *   #add_month(#now(),-1): 上一个月
 *   #add_month_format(#now(),-1,'yyyy-MM-dd'): 上一个月
 *   #add_day(#now(),-1): 昨天
 *   #add_day_format(#now(),-1,'yyyy-MM-dd'): 昨天
 *   #add_hour(#now(),-1): 前一个小时
 *   #add_hour_format(#now(),-1,'yyyy-MM-dd HH'): 前一个小时
 *   #add_minute(#now(),-1): 前一分钟
 *   #add_minute_format(#now(),-1,'yyyy-MM-dd HH:mm'): 前一分钟
 * </pre>
 */
@Slf4j
public final class SPELUtil {

    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final StandardEvaluationContext context = new StandardEvaluationContext();

    static {
        val thisClass = SPELUtil.class;
        try {
            // now
            val now = thisClass.getMethod("now");
            context.registerFunction("now", now);
            val nowFormat = thisClass.getMethod("nowFormat", String.class);
            context.registerFunction("now_format", nowFormat);

            // month
            val addMonth = thisClass.getMethod("addMonth", String.class, Integer.class);
            context.registerFunction("add_month", addMonth);
            val addMonthFormat = thisClass.getMethod("addMonthFormat", String.class, Integer.class, String.class);
            context.registerFunction("add_month_format", addMonthFormat);

            // day
            val addDay = thisClass.getMethod("addDay", String.class, Integer.class);
            context.registerFunction("add_day", addDay);
            val addDayFormat = thisClass.getMethod("addDayFormat", String.class, Integer.class, String.class);
            context.registerFunction("add_day_format", addDayFormat);

            // hour
            val addHour = thisClass.getMethod("addHour", String.class, Integer.class);
            context.registerFunction("add_hour", addHour);
            val addHourFormat = thisClass.getMethod("addHourFormat", String.class, Integer.class, String.class);
            context.registerFunction("add_hour_format", addHourFormat);

            // minute
            val addMinute = thisClass.getMethod("addMinute", String.class, Integer.class);
            context.registerFunction("add_minute", addMinute);
            val addMinuteFormat = thisClass.getMethod("addMinuteFormat", String.class, Integer.class, String.class);
            context.registerFunction("add_minute_format", addMinuteFormat);
        } catch (NoSuchMethodException e) {
            // do nothing
        }
    }

    private SPELUtil() {
    }

    public static String now() {
        return nowFormat(DatePattern.NORM_DATETIME_PATTERN);
    }

    public static String nowFormat(String pattern) {
        return DateTime.now().toString(pattern);
    }

    public static String addMonth(String date, Integer months) {
        return calculationDate(date, months, DatePattern.NORM_DATETIME_PATTERN, DateCalcUnit.MONTH);
    }

    public static String addMonthFormat(String date, Integer months, String pattern) {
        return calculationDate(date, months, pattern, DateCalcUnit.MONTH);
    }

    public static String addDay(String date, Integer days) {
        return calculationDate(date, days, DatePattern.NORM_DATETIME_PATTERN, DateCalcUnit.DAY);
    }

    public static String addDayFormat(String date, Integer days, String pattern) {
        return calculationDate(date, days, pattern, DateCalcUnit.DAY);
    }

    public static String addHour(String date, Integer hours) {
        return calculationDate(date, hours, DatePattern.NORM_DATETIME_PATTERN, DateCalcUnit.HOUR);
    }

    public static String addHourFormat(String date, Integer hours, String pattern) {
        return calculationDate(date, hours, pattern, DateCalcUnit.HOUR);
    }

    public static String addMinute(String date, Integer minutes) {
        return calculationDate(date, minutes, DatePattern.NORM_DATETIME_PATTERN, DateCalcUnit.MINUTE);
    }

    public static String addMinuteFormat(String date, Integer minutes, String pattern) {
        return calculationDate(date, minutes, pattern, DateCalcUnit.MINUTE);
    }

    private static String calculationDate(String date, Integer num, String pattern, DateCalcUnit dateCalcUnit) {
        val localDateTime = DateUtil.parse(date).toLocalDateTime();

        val result = switch (dateCalcUnit) {
            case MONTH -> localDateTime.plusMonths(num);
            case DAY -> localDateTime.plusDays(num);
            case HOUR -> localDateTime.plusHours(num);
            case MINUTE -> localDateTime.plusMinutes(num);
        };

        return LocalDateTimeUtil.format(result, pattern);
    }

    public static <T> T parseExpression(String expr, Class<T> clazz) {
        val expression = parser.parseExpression(expr);
        return expression.getValue(context, clazz);
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

    @Getter
    public enum DateCalcUnit {
        MONTH,
        DAY,
        HOUR,
        MINUTE
    }
}
