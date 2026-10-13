package kigali.clinic.clinicmanagement.utils;

import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStatusConverter implements Converter<String, AppointmentStatus> {

    @Override
    public AppointmentStatus convert(String source) {
        return AppointmentStatus.from(source);
    }
}
