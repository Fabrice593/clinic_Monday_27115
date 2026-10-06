package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class AppointmentService {

    private static final List<String> ALLOWED_STATUSES = List.of("SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED");

    private static final String CANCELLED = "CANCELLED";

    private static final String DEFAULT_STATUS = "SCHEDULED";

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Transactional
    public String saveAppointment(Appointment appointment) {

        String error = resolvePatient(appointment);

        if (error != null) {
            return error;
        }

        error = resolveDoctor(appointment);

        if (error != null) {
            return error;
        }

        if (appointment.getDate() == null) {
            return "Appointment date is required";
        }

        if (appointment.getDate().isBefore(LocalDateTime.now())) {
            return "Appointment date must be in the future";
        }

        error = applyStatus(appointment, DEFAULT_STATUS);

        if (error != null) {
            return error;
        }

        if (appointmentRepo.existsByDoctorIdAndDateAndStatusNot(
                appointment.getDoctor().getId(), appointment.getDate(), CANCELLED)) {
            return "Doctor already has an appointment at that time";
        }

        appointment.setId(null);
        appointment.setReason(normalize(appointment.getReason()));
        appointmentRepo.save(appointment);

        return "Appointment is saved Successfully";
    }

    @Transactional
    public String updateAppointment(UUID id, Appointment appointment) {

        Appointment existing = appointmentRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Appointment not found";
        }

        String error = resolvePatient(appointment);

        if (error != null) {
            return error;
        }

        error = resolveDoctor(appointment);

        if (error != null) {
            return error;
        }

        if (appointment.getDate() == null) {
            return "Appointment date is required";
        }

        // a past date stays allowed here, so old appointments can still be closed
        error = applyStatus(appointment, existing.getStatus());

        if (error != null) {
            return error;
        }

        if (appointmentRepo.existsByDoctorIdAndDateAndStatusNotAndIdNot(
                appointment.getDoctor().getId(), appointment.getDate(), CANCELLED, id)) {
            return "Doctor already has an appointment at that time";
        }

        existing.setPatient(appointment.getPatient());
        existing.setDoctor(appointment.getDoctor());
        existing.setDate(appointment.getDate());
        existing.setStatus(appointment.getStatus());
        existing.setReason(normalize(appointment.getReason()));
        appointmentRepo.save(existing);

        return "Appointment is updated Successfully";
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public List<Appointment> getAppointmentsByStatus(String status) {
        return appointmentRepo.findByStatusOrderByDateAsc(status);
    }

    public List<Appointment> getAppointmentsBetween(LocalDateTime start, LocalDateTime end) {
        return appointmentRepo.findByDateBetweenOrderByDateAsc(start, end);
    }

    public boolean isValidStatus(String status) {
        return ALLOWED_STATUSES.contains(status);
    }

    public Appointment getAppointmentById(UUID id) {
        return appointmentRepo.findById(id).orElse(null);
    }

    @Transactional
    public String deleteAppointment(UUID id) {

        Appointment existing = appointmentRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Appointment not found";
        }

        appointmentRepo.delete(existing);

        return "Appointment is deleted Successfully";
    }

    private String resolvePatient(Appointment appointment) {

        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            return "Patient is required";
        }

        Patient patient = patientRepo.findById(appointment.getPatient().getId()).orElse(null);

        if (patient == null) {
            return "Patient not found";
        }

        appointment.setPatient(patient);

        return null;
    }

    private String resolveDoctor(Appointment appointment) {

        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return "Doctor is required";
        }

        Doctor doctor = doctorRepo.findById(appointment.getDoctor().getId()).orElse(null);

        if (doctor == null) {
            return "Doctor not found";
        }

        appointment.setDoctor(doctor);

        return null;
    }

    private String applyStatus(Appointment appointment, String fallback) {

        if (isBlank(appointment.getStatus())) {
            appointment.setStatus(fallback);
            return null;
        }

        String status = appointment.getStatus().trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(status)) {
            return "Status must be one of " + String.join(", ", ALLOWED_STATUSES);
        }

        appointment.setStatus(status);

        return null;
    }
}
