package kigali.clinic.clinicmanagement.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static kigali.clinic.clinicmanagement.utils.Validations.ensure;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Entity
@Getter
@Table(name = "prescription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prescription extends BaseEntity {

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "dosage", nullable = false)
    private String dosage;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    public static Prescription issue(Appointment appointment, Details details, LocalDate issuedOn) {
        Prescription prescription = new Prescription();
        prescription.appointment = appointment;
        prescription.issuedDate = issuedOn;
        prescription.revise(details);
        return prescription;
    }

    public void revise(Details details) {
        details.requireValid();
        medicineName = details.medicineName();
        dosage = details.dosage();
        durationDays = details.durationDays();
    }

    public record Details(String medicineName, String dosage, Integer durationDays) {

        public Details {
            medicineName = normalize(medicineName);
            dosage = normalize(dosage);
        }

        void requireValid() {
            ensure(!isBlank(medicineName), "Medicine name is required");
            ensure(!isBlank(dosage), "Dosage is required");
            ensure(durationDays != null && durationDays >= 1, "Duration must be at least 1 day");
        }
    }
}
