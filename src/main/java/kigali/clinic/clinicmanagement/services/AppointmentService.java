package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.models.AppointmentStatus.CANCELLED;
import static kigali.clinic.clinicmanagement.models.AppointmentStatus.COMPLETED;
import static kigali.clinic.clinicmanagement.models.AppointmentStatus.CONFIRMED;
import static kigali.clinic.clinicmanagement.models.AppointmentStatus.SCHEDULED;
import static kigali.clinic.clinicmanagement.utils.Dates.endOfDay;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class AppointmentService {

    public static final String APPOINTMENT_NOT_FOUND = "The appointment with that id does not exist";

    private static final AppointmentStatus DEFAULT_STATUS = SCHEDULED;

    private static final String DOCTOR_BOOKED = "Doctor is already booked on that date";

    private static final String PAST_DATE = "Appointment date cannot be in the past";

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

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

        if (appointment.getAppointmentDate() == null) {
            return "Appointment date is required";
        }

        LocalDate day = appointment.getAppointmentDate().toLocalDate();

        // B1: today is still bookable, only earlier days are refused
        if (day.isBefore(LocalDate.now())) {
            return PAST_DATE;
        }

        if (isDoctorBooked(appointment.getDoctor().getId(), day, null)) {
            return DOCTOR_BOOKED;
        }

        // B1: a new appointment always starts as SCHEDULED, whatever the body says
        appointment.setId(null);
        appointment.setStatus(DEFAULT_STATUS);
        appointment.setReason(normalize(appointment.getReason()));
        appointmentRepo.save(appointment);

        return "Appointment is saved Successfully";
    }

    @Transactional
    public String updateAppointment(UUID id, Appointment appointment) {

        Appointment existing = appointmentRepo.findById(id).orElse(null);

        if (existing == null) {
            return APPOINTMENT_NOT_FOUND;
        }

        String error = resolvePatient(appointment);

        if (error != null) {
            return error;
        }

        error = resolveDoctor(appointment);

        if (error != null) {
            return error;
        }

        if (appointment.getAppointmentDate() == null) {
            return "Appointment date is required";
        }

        // a live appointment is re-checked only when it moves to another doctor or day
        LocalDate day = appointment.getAppointmentDate().toLocalDate();
        boolean moves = !existing.getDoctor().getId().equals(appointment.getDoctor().getId())
                || !existing.getAppointmentDate().toLocalDate().equals(day);

        if (existing.getStatus() != CANCELLED && moves && isDoctorBooked(appointment.getDoctor().getId(), day, id)) {
            return DOCTOR_BOOKED;
        }

        // B2: the status is not copied; it only changes through PATCH /{id}/status
        existing.setPatient(appointment.getPatient());
        existing.setDoctor(appointment.getDoctor());
        existing.setAppointmentDate(appointment.getAppointmentDate());
        existing.setReason(normalize(appointment.getReason()));
        appointmentRepo.save(existing);

        return "Appointment is updated Successfully";
    }

    // B2
    @Transactional
    public String changeStatus(UUID id, AppointmentStatus status) {

        Appointment appointment = appointmentRepo.findById(id).orElse(null);

        if (appointment == null) {
            return APPOINTMENT_NOT_FOUND;
        }

        if (!canMove(appointment.getStatus(), status)) {
            return "Cannot change status from " + appointment.getStatus() + " to " + status;
        }

        appointment.setStatus(status);
        appointmentRepo.save(appointment);

        return "Appointment status updated successfully";
    }

    // B3: the date moves, the time of day stays, and the appointment starts over as SCHEDULED
    @Transactional
    public String reschedule(UUID id, LocalDate newDate) {

        Appointment appointment = appointmentRepo.findById(id).orElse(null);

        if (appointment == null) {
            return APPOINTMENT_NOT_FOUND;
        }

        if (appointment.getStatus() == COMPLETED || appointment.getStatus() == CANCELLED) {
            return "A " + appointment.getStatus() + " appointment cannot be rescheduled";
        }

        if (newDate.isBefore(LocalDate.now())) {
            return PAST_DATE;
        }

        if (isDoctorBooked(appointment.getDoctor().getId(), newDate, id)) {
            return DOCTOR_BOOKED;
        }

        appointment.setAppointmentDate(newDate.atTime(appointment.getAppointmentDate().toLocalTime()));
        appointment.setStatus(SCHEDULED);
        appointmentRepo.save(appointment);

        return "Appointment rescheduled successfully";
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end) {
        return appointmentRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start.atStartOfDay(), endOfDay(end));
    }

    public List<Object[]> getAppointmentCountsByStatus() {
        return appointmentRepo.countAppointmentsPerStatus();
    }

    // COMPLETED visits stay untouched; already CANCELLED ones are skipped so the count is only what changed
    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date) {

        int cancelled = appointmentRepo.cancelDoctorDay(
                doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), LocalDateTime.now());

        return cancelled + " appointments cancelled";
    }

    public Page<Appointment> getAppointmentsPage(Pageable pageable) {
        return appointmentRepo.findAll(pageable);
    }

    @Transactional
    public String deleteCancelledBefore(LocalDate date) {

        int deleted = appointmentRepo.deleteCancelledBefore(date.atStartOfDay());

        return deleted + " appointments deleted";
    }

    // D1: empty means the doctor does not exist
    public Optional<List<Appointment>> getDoctorAgenda(UUID doctorId, LocalDate date) {

        if (!doctorRepo.existsById(doctorId)) {
            return Optional.empty();
        }

        return Optional.of(appointmentRepo.findByDoctorIdAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDateAsc(
                doctorId, date.atStartOfDay(), endOfDay(date), CANCELLED));
    }

    // D2: empty means no patient has that ss number
    public Optional<List<Appointment>> getUpcomingAppointments(String ssNumber) {
        return patientRepo.findBySsNumber(ssNumber.trim()).map(patient ->
                appointmentRepo.findByPatientAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
                        patient, LocalDate.now().atStartOfDay(), List.of(SCHEDULED, CONFIRMED)));
    }

    public Optional<Appointment> getAppointmentById(UUID id) {
        return appointmentRepo.findById(id);
    }

    @Transactional
    public String deleteAppointment(UUID id) {

        Appointment existing = appointmentRepo.findById(id).orElse(null);

        if (existing == null) {
            return APPOINTMENT_NOT_FOUND;
        }

        // C4
        if (prescriptionRepo.existsByAppointmentId(id)) {
            return "That appointment has prescriptions, delete them first";
        }

        appointmentRepo.delete(existing);

        return "Appointment is deleted Successfully";
    }

    // B2 arrows: SCHEDULED -> CONFIRMED or CANCELLED, CONFIRMED -> COMPLETED or CANCELLED; the rest are final
    private static boolean canMove(AppointmentStatus from, AppointmentStatus to) {

        if (from == SCHEDULED) {
            return to == CONFIRMED || to == CANCELLED;
        }

        if (from == CONFIRMED) {
            return to == COMPLETED || to == CANCELLED;
        }

        return false;
    }

    private boolean isDoctorBooked(UUID doctorId, LocalDate day, UUID ignoredId) {
        return ignoredId == null
                ? appointmentRepo.existsByDoctorIdAndAppointmentDateBetweenAndStatusNot(
                        doctorId, day.atStartOfDay(), endOfDay(day), CANCELLED)
                : appointmentRepo.existsByDoctorIdAndAppointmentDateBetweenAndStatusNotAndIdNot(
                        doctorId, day.atStartOfDay(), endOfDay(day), CANCELLED, ignoredId);
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
}
