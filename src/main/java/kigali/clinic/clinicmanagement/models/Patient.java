package kigali.clinic.clinicmanagement.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static kigali.clinic.clinicmanagement.utils.Validations.containsDigit;
import static kigali.clinic.clinicmanagement.utils.Validations.ensure;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Entity
@Getter
@Table(name = "patients")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Patient extends BaseEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "ss_number", unique = true, length = 30)
    private String ssNumber;

    public static Patient register(Details details) {
        Patient patient = new Patient();
        patient.revise(details);
        return patient;
    }

    public void revise(Details details) {
        this.firstName = details.firstName();
        this.lastName = details.lastName();
        this.ssNumber = details.ssNumber();
    }

    public record Details(String firstName, String lastName, String ssNumber) {

        public Details {
            firstName = normalize(firstName);
            lastName = normalize(lastName);
            ssNumber = normalize(ssNumber);
        }

        public void requireValid() {
            ensure(!isBlank(firstName), "Patient first name is required");
            ensure(!isBlank(lastName), "Patient last name is required");
            ensure(!containsDigit(firstName) && !containsDigit(lastName), "Patient names must not contain numbers");
            ensure(!isBlank(ssNumber), "Patient ss number is required");
        }
    }
}
