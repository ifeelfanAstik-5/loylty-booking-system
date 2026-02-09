package com.loylty.moviebooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class TimezoneConfig {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convert UTC time to IST for API responses
     */
    public static String convertToIst(LocalDateTime utcTime) {
        if (utcTime == null) return null;
        return utcTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(IST_ZONE)
                .format(DATE_TIME_FORMATTER);
    }

    /**
     * Convert IST time to UTC for API requests
     */
    public static LocalDateTime convertToUtc(String istTime) {
        if (istTime == null) return null;
        return LocalDateTime.parse(istTime, DATE_TIME_FORMATTER)
                .atZone(IST_ZONE)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    /**
     * Get current IST time
     */
    public static LocalDateTime getCurrentIstTime() {
        return LocalDateTime.now(IST_ZONE);
    }

    /**
     * Check if a show time is in the future (IST time)
     */
    public static boolean isShowTimeInFuture(LocalDateTime showTime) {
        if (showTime == null) return false;
        return showTime.isAfter(getCurrentIstTime());
    }
}
