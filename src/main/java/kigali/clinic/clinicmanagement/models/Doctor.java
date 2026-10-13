package kigali.clinic.clinicmanagement.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static kigali.clinic.clinicmanagement.utils.Validations.containsDigit;
import static kigali.clinic.clinicmanagement.utils.Validations.ensure;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Entity
@Getter
@Table(name = "doctors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Doctor extends BaseEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @OneToOne
    @JoinColumn(name = "office_id")
    private Office office;

    @ManyToMany
    @JoinTable(
            name = "doctor_specializations",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private List<Specialization> specializations = new ArrayList<>();

    public static Doctor register(Details details, Office office, List<Specialization> specializations) {
        Doctor doctor = new Doctor();
        doctor.revise(details, office, specializations);
        return doctor;
    }

    public void revise(Details details, Office office, List<Specialization> specializations) {
        this.firstName = details.firstName();
        this.lastName = details.lastName();
        this.office = office;
        this.specializations.clear();
        this.specializations.addAll(specializations);
    }

    public boolean addSpecialization(Specialization specialization) {
        return !specializations.contains(specialization) && specializations.add(specialization);
    }

    public boolean removeSpecialization(Specialization specialization) {
        return specializations.remove(specialization);
    }

    public void moveTo(Office office) {
        this.office = office;
    }

    public void vacateOffice() {
        if (office != null) {
            office.releaseDoctor();
            office = null;
        }
    }

    public record Details(String firstName, String lastName, Reference office, List<Reference> specializations) {

        public Details {
            firstName = normalize(firstName);
            lastName = normalize(lastName);
        }

        public void requireValidNames() {
            ensure(!isBlank(firstName), "Doctor first name is required");
            ensure(!isBlank(lastName), "Doctor last name is required");
            ensure(!containsDigit(firstName) && !containsDigit(lastName), "Doctor names must not contain numbers");
        }
    }
}
