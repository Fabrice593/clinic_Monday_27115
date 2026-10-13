package kigali.clinic.clinicmanagement.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "prescription")
public class Prescription extends BaseEntity {

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    // e.g. "1 tablet, 3 times a day"
    @Column(name = "dosage", nullable = false)
    private String dosage;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    // one appointment, many prescriptions; Appointment keeps no list back, so the JSON has no loop
    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;
}
