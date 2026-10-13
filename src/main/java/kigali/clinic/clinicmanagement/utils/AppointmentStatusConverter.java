package kigali.clinic.clinicmanagement.utils;

import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

// Spring's own enum conversion is case sensitive; this one lets ?status=cancelled through.
// A wrong value still ends in ApiExceptionHandler as a 400.
@Component
public class AppointmentStatusConverter implements Converter<String, AppointmentStatus> {

    @Override
    public AppointmentStatus convert(String source) {
        return AppointmentStatus.from(source);
    }
}
