package kigali.clinic.clinicmanagement.services;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static kigali.clinic.clinicmanagement.utils.Dates.endOfDay;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public Summary summary() {
        LocalDate today = LocalDate.now();

        return new Summary(
                patientRepository.count(),
                doctorRepository.count(),
                officeRepository.findFreeOffices().size(),
                appointmentRepository.countByAppointmentDateBetween(today.atStartOfDay(), endOfDay(today)),
                appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED),
                prescriptionRepository.count());
    }

    @JsonPropertyOrder({"patients", "doctors", "freeOffices", "appointmentsToday", "scheduled", "prescriptions"})
    public record Summary(long patients,
                          long doctors,
                          long freeOffices,
                          long appointmentsToday,
                          long scheduled,
                          long prescriptions) {
    }
}
