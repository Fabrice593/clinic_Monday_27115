package kigali.clinic.clinicmanagement.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class Dates {

    private Dates() {
    }

    public static LocalDateTime endOfDay(LocalDate day) {
        return day.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.MICROS);
    }
}
