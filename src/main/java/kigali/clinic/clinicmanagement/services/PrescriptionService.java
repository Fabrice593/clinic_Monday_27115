package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.models.Prescription;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository.MedicineUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.services.AppointmentService.APPOINTMENT_NOT_FOUND;
import static kigali.clinic.clinicmanagement.utils.ApiResponses.badRequest;
import static kigali.clinic.clinicmanagement.utils.ApiResponses.notFound;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionService {

    private static final String PRESCRIPTION_NOT_FOUND = "The prescription with that id does not exist";
    private static final String APPOINTMENT_NOT_COMPLETED = "Prescriptions are only for COMPLETED appointments";

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void issue(UUID appointmentId, Prescription.Details details) {
        Appointment appointment = completedAppointment(appointmentId);
        prescriptionRepository.save(Prescription.issue(appointment, details, LocalDate.now()));
    }

    public List<Prescription> prescriptionsOfAppointment(UUID appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw notFound(APPOINTMENT_NOT_FOUND);
        }

        return prescriptionRepository.findAllByAppointmentIdOrderByCreatedAtAsc(appointmentId);
    }

    public List<Prescription> historyOfPatient(String ssNumber) {
        return prescriptionRepository.findHistoryOfPatient(ssNumber.trim());
    }

    public List<MedicineUsage> rankMedicinesByUsage() {
        return prescriptionRepository.rankMedicinesByUsage();
    }

    @Transactional
    public void revise(UUID prescriptionId, Prescription.Details details) {
        existingPrescription(prescriptionId).revise(details);
    }

    @Transactional
    public void delete(UUID prescriptionId) {
        prescriptionRepository.delete(existingPrescription(prescriptionId));
    }

    private Appointment completedAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> notFound(APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw badRequest(APPOINTMENT_NOT_COMPLETED);
        }

        return appointment;
    }

    private Prescription existingPrescription(UUID prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> notFound(PRESCRIPTION_NOT_FOUND));
    }
}
