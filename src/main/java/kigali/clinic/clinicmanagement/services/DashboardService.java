package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static kigali.clinic.clinicmanagement.utils.Dates.endOfDay;

@Service
public class DashboardService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private OfficeRepository officeRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    // D4: LinkedHashMap keeps the keys in this order in the JSON
    public Map<String, Long> getSummary() {

        LocalDate today = LocalDate.now();
        Map<String, Long> summary = new LinkedHashMap<>();

        summary.put("patients", patientRepo.count());
        summary.put("doctors", doctorRepo.count());
        summary.put("freeOffices", (long) officeRepo.findFreeOffices().size());
        summary.put("appointmentsToday", appointmentRepo.countByAppointmentDateBetween(today.atStartOfDay(), endOfDay(today)));
        summary.put("scheduled", appointmentRepo.countByStatus(AppointmentStatus.SCHEDULED));
        summary.put("prescriptions", prescriptionRepo.count());

        return summary;
    }
}
