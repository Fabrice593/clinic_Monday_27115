package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.models.Prescription;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.services.AppointmentService.APPOINTMENT_NOT_FOUND;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class PrescriptionService {

    private static final String PRESCRIPTION_NOT_FOUND = "The prescription with that id does not exist";

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Transactional
    public String savePrescription(UUID appointmentId, Prescription prescription) {

        Appointment appointment = appointmentRepo.findById(appointmentId).orElse(null);

        if (appointment == null) {
            return APPOINTMENT_NOT_FOUND;
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return "Prescriptions are only for COMPLETED appointments";
        }

        String error = validate(prescription);

        if (error != null) {
            return error;
        }

        prescription.setId(null);
        prescription.setMedicineName(normalize(prescription.getMedicineName()));
        prescription.setDosage(normalize(prescription.getDosage()));
        prescription.setIssuedDate(LocalDate.now());
        prescription.setAppointment(appointment);
        prescriptionRepo.save(prescription);

        return "Prescription saved successfully";
    }

    public Optional<List<Prescription>> getPrescriptionsOfAppointment(UUID appointmentId) {
        return appointmentRepo.findById(appointmentId).map(prescriptionRepo::findByAppointment);
    }


    public List<Prescription> getPatientHistory(String ssNumber) {
        return prescriptionRepo.findPatientHistory(ssNumber.trim());
    }

    public List<Object[]> getTopMedicines() {
        return prescriptionRepo.findTopMedicines();
    }

     @Transactional
    public String updatePrescription(UUID id, Prescription prescription) {

        Prescription existing = prescriptionRepo.findById(id).orElse(null);

        if (existing == null) {
            return PRESCRIPTION_NOT_FOUND;
        }

        String error = validate(prescription);

        if (error != null) {
            return error;
        }

        existing.setMedicineName(normalize(prescription.getMedicineName()));
        existing.setDosage(normalize(prescription.getDosage()));
        existing.setDurationDays(prescription.getDurationDays());
        prescriptionRepo.save(existing);

        return "Prescription updated successfully";
    }

    @Transactional
    public String deletePrescription(UUID id) {

        if (!prescriptionRepo.existsById(id)) {
            return PRESCRIPTION_NOT_FOUND;
        }

        prescriptionRepo.deleteById(id);

        return "Prescription deleted successfully";
    }

    private String validate(Prescription prescription) {

        if (isBlank(prescription.getMedicineName())) {
            return "Medicine name is required";
        }

        if (isBlank(prescription.getDosage())) {
            return "Dosage is required";
        }

        if (prescription.getDurationDays() < 1) {
            return "Duration must be at least 1 day";
        }

        return null;
    }
}
