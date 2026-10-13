package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.models.BaseEntity.Reference;
import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository.StatusCount;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.models.AppointmentStatus.CANCELLED;
import static kigali.clinic.clinicmanagement.models.AppointmentStatus.CONFIRMED;
import static kigali.clinic.clinicmanagement.models.AppointmentStatus.SCHEDULED;
import static kigali.clinic.clinicmanagement.utils.ApiResponses.conflict;
import static kigali.clinic.clinicmanagement.utils.ApiResponses.notFound;
import static kigali.clinic.clinicmanagement.utils.Dates.endOfDay;
import static kigali.clinic.clinicmanagement.utils.Validations.ensure;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    public static final String APPOINTMENT_NOT_FOUND = "The appointment with that id does not exist";

    private static final String DOCTOR_BOOKED = "Doctor is already booked on that date";

    private static final String PAST_DATE = "Appointment date cannot be in the past";

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Transactional
    public void book(Appointment.Details details) {
        Patient patient = patientFor(details.patient());
        Doctor doctor = doctorFor(details.doctor());
        LocalDate day = details.requiredDay();

        ensure(!day.isBefore(LocalDate.now()), PAST_DATE);
        requireDoctorFree(doctor, day, null);

        appointmentRepository.save(Appointment.book(patient, doctor, details));
    }

    @Transactional
    public void revise(UUID appointmentId, Appointment.Details details) {
        Appointment appointment = appointmentById(appointmentId);
        Patient patient = patientFor(details.patient());
        Doctor doctor = doctorFor(details.doctor());
        LocalDate day = details.requiredDay();

        if (appointment.getStatus() != CANCELLED && appointment.changesDoctorOrDay(doctor, day)) {
            requireDoctorFree(doctor, day, appointmentId);
        }

        appointment.revise(patient, doctor, details);
    }

    @Transactional
    public void changeStatus(UUID appointmentId, AppointmentStatus target) {
        appointmentById(appointmentId).moveTo(target);
    }

    @Transactional
    public void reschedule(UUID appointmentId, LocalDate newDay) {
        Appointment appointment = appointmentById(appointmentId);

        ensure(!appointment.getStatus().isFinal(),
                "A " + appointment.getStatus() + " appointment cannot be rescheduled");
        ensure(!newDay.isBefore(LocalDate.now()), PAST_DATE);
        requireDoctorFree(appointment.getDoctor(), newDay, appointmentId);

        appointment.rescheduleTo(newDay);
    }

    public List<Appointment> allAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> withStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> between(LocalDate firstDay, LocalDate lastDay) {
        ensure(!lastDay.isBefore(firstDay), "End date must not be before start date");

        return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAsc(
                firstDay.atStartOfDay(), endOfDay(lastDay));
    }

    public List<StatusCount> countsPerStatus() {
        return appointmentRepository.countPerStatus();
    }

    @Transactional
    public int cancelDoctorDay(UUID doctorId, LocalDate day) {
        return appointmentRepository.cancelDoctorDay(
                doctorId, day.atStartOfDay(), day.plusDays(1).atStartOfDay(), LocalDateTime.now());
    }

    public Page<Appointment> page(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Transactional
    public int deleteCancelledBefore(LocalDate day) {
        return appointmentRepository.deleteCancelledBefore(day.atStartOfDay());
    }

    public List<Appointment> agendaOf(UUID doctorId, LocalDate day) {
        if (!doctorRepository.existsById(doctorId)) {
            throw notFound(DoctorService.DOCTOR_NOT_FOUND);
        }

        return appointmentRepository.findByDoctorIdAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDateAsc(
                doctorId, day.atStartOfDay(), endOfDay(day), CANCELLED);
    }

    public List<Appointment> upcomingFor(String ssNumber) {
        Patient patient = patientRepository.findBySsNumber(ssNumber.trim())
                .orElseThrow(() -> notFound("That patient with that ssnumber does not exist"));

        return appointmentRepository.findByPatientAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
                patient, LocalDate.now().atStartOfDay(), List.of(SCHEDULED, CONFIRMED));
    }

    public Appointment appointmentById(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> notFound(APPOINTMENT_NOT_FOUND));
    }

    @Transactional
    public void delete(UUID appointmentId) {
        Appointment appointment = appointmentById(appointmentId);

        if (prescriptionRepository.existsByAppointmentId(appointmentId)) {
            throw conflict("That appointment has prescriptions, delete them first");
        }

        appointmentRepository.delete(appointment);
    }

    private void requireDoctorFree(Doctor doctor, LocalDate day, UUID appointmentToIgnore) {
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = endOfDay(day);

        boolean booked = appointmentToIgnore == null
                ? appointmentRepository.existsByDoctorIdAndAppointmentDateBetweenAndStatusNot(
                        doctor.getId(), dayStart, dayEnd, CANCELLED)
                : appointmentRepository.existsByDoctorIdAndAppointmentDateBetweenAndStatusNotAndIdNot(
                        doctor.getId(), dayStart, dayEnd, CANCELLED, appointmentToIgnore);

        if (booked) {
            throw conflict(DOCTOR_BOOKED);
        }
    }

    private Patient patientFor(Reference patientReference) {
        ensure(patientReference != null && patientReference.id() != null, "Patient is required");

        return patientRepository.findById(patientReference.id())
                .orElseThrow(() -> notFound("Patient not found"));
    }

    private Doctor doctorFor(Reference doctorReference) {
        ensure(doctorReference != null && doctorReference.id() != null, "Doctor is required");

        return doctorRepository.findById(doctorReference.id())
                .orElseThrow(() -> notFound("Doctor not found"));
    }
}
