package kigali.clinic.clinicmanagement.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED;

    @JsonCreator
    public static AppointmentStatus from(String value) {
        String wanted = value.trim();

        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status must be one of " + allowedNames()));
    }

    public boolean canMoveTo(AppointmentStatus target) {
        return switch (this) {
            case SCHEDULED -> target == CONFIRMED || target == CANCELLED;
            case CONFIRMED -> target == COMPLETED || target == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }

    private static String allowedNames() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
