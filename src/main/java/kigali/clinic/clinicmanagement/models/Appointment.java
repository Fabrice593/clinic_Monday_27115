package kigali.clinic.clinicmanagement.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static kigali.clinic.clinicmanagement.utils.Validations.ensure;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Entity
@Getter
@Table(name = "appointments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Appointment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDate;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    public static Appointment book(Patient patient, Doctor doctor, Details details) {
        Appointment appointment = new Appointment();
        appointment.status = AppointmentStatus.SCHEDULED;
        appointment.revise(patient, doctor, details);
        return appointment;
    }

    public void revise(Patient patient, Doctor doctor, Details details) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = details.appointmentDate();
        this.reason = details.reason();
    }

    public boolean changesDoctorOrDay(Doctor otherDoctor, LocalDate otherDay) {
        return !doctor.getId().equals(otherDoctor.getId()) || !appointmentDate.toLocalDate().equals(otherDay);
    }

    public void moveTo(AppointmentStatus target) {
        ensure(status.canMoveTo(target), "Cannot change status from " + status + " to " + target);
        status = target;
    }

    public void rescheduleTo(LocalDate newDay) {
        appointmentDate = newDay.atTime(appointmentDate.toLocalTime());
        status = AppointmentStatus.SCHEDULED;
    }

    public record Details(Reference patient,
                          Reference doctor,
                          @JsonAlias("date") LocalDateTime appointmentDate,
                          String reason,
                          @JsonProperty("status") AppointmentStatus ignoredStatus) {

        public Details {
            reason = normalize(reason);
        }

        public LocalDate requiredDay() {
            ensure(appointmentDate != null, "Appointment date is required");
            return appointmentDate.toLocalDate();
        }
    }
}
