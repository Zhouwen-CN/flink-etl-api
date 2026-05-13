package com.etl.api.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class LocalDateTimeUtil {

    private LocalDateTimeUtil() {
    }

    public static long toMs(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    public static LocalDateTime fromMs(long ms) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
    }
}
