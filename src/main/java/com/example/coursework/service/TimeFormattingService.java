package com.example.coursework.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TimeFormattingService {

    public String formatTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long secondsAgo = ChronoUnit.SECONDS.between(dateTime, now);
        long minutesAgo = ChronoUnit.MINUTES.between(dateTime, now);
        long hoursAgo = ChronoUnit.HOURS.between(dateTime, now);
        long daysAgo = ChronoUnit.DAYS.between(dateTime, now);

        if (secondsAgo < 60) {
            return "щойно"; // Якщо менше хвилини тому
        } else if (minutesAgo < 60) {
            return formatMinutes(minutesAgo) + " назад";
        } else if (hoursAgo < 24) {
            return formatHours(hoursAgo) + " назад";
        } else {
            return formatDays(daysAgo) + " тому";
        }
    }

    public String formatTimeUntil(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutesUntil = ChronoUnit.MINUTES.between(now, dateTime);
        long hoursUntil = ChronoUnit.HOURS.between(now, dateTime);
        long daysUntil = ChronoUnit.DAYS.between(now, dateTime);

        if (minutesUntil < 60) {
            return "через " + formatMinutes(minutesUntil);
        } else if (hoursUntil < 24) {
            return "через " + formatHours(hoursUntil);
        } else {
            return "через " + formatDays(daysUntil);
        }
    }

    private String formatMinutes(long minutes) {
        return minutes + (minutes == 1 ? " хвилину" : " хвилин");
    }

    private String formatHours(long hours) {
        return hours + (hours == 1 ? " годину" : " годин");
    }

    private String formatDays(long days) {
        return days + (days == 1 ? " день" : " днів");
    }
}