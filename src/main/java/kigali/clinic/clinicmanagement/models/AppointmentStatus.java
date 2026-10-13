package kigali.clinic.clinicmanagement.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED;

    // any letter case is accepted, so "cancelled" works in a URL and in a request body
    @JsonCreator
    public static AppointmentStatus from(String value) {

        for (AppointmentStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }

        String allowed = Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));

        throw new IllegalArgumentException("Status must be one of " + allowed);
    }
}
