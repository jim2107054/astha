package com.astha.disputes.sla;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Component
public class BangladeshSlaCalculator {

    public static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    // Configurable gazetted holidays (YYYY-MM-DD)
    private final Set<LocalDate> holidays = new HashSet<>();

    public BangladeshSlaCalculator() {
        // Seed standard sample holidays (e.g. Independence Day, Victory Day, Eid, etc.)
        holidays.add(LocalDate.of(2026, 2, 21)); // Language Martyrs' Day
        holidays.add(LocalDate.of(2026, 3, 26)); // Independence Day
        holidays.add(LocalDate.of(2026, 4, 14)); // Pohela Boishakh
        holidays.add(LocalDate.of(2026, 12, 16)); // Victory Day
    }

    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) {
            return false;
        }
        return !holidays.contains(date);
    }

    public LocalDateTime calculateWorkingDaysDeadline(LocalDateTime startDateTime, int workingDays) {
        LocalDateTime current = startDateTime;
        int daysAdded = 0;

        while (daysAdded < workingDays) {
            current = current.plusDays(1);
            if (isWorkingDay(current.toLocalDate())) {
                daysAdded++;
            }
        }
        return current;
    }

    public SlaStatus evaluateStatus(LocalDateTime deadline, LocalDateTime now) {
        if (now.isAfter(deadline)) {
            return new SlaStatus("BREACHED", 0);
        }

        long totalHours = java.time.Duration.between(deadline.minusDays(3), deadline).toHours();
        long remainingHours = java.time.Duration.between(now, deadline).toHours();

        if (totalHours > 0) {
            double percentRemaining = (double) remainingHours / totalHours;
            if (percentRemaining <= 0.20) return new SlaStatus("WARNING_80", remainingHours);
            if (percentRemaining <= 0.50) return new SlaStatus("WARNING_50", remainingHours);
        }

        return new SlaStatus("NORMAL", remainingHours);
    }

    public record SlaStatus(String warningLevel, long remainingHours) {}
}
