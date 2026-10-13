package kigali.clinic.clinicmanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static kigali.clinic.clinicmanagement.utils.Validations.ensure;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.isNotPositiveNumber;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Entity
@Getter
@Table(name = "offices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Office extends BaseEntity {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "office_number", nullable = false, length = 20)
    private String officeNumber;

    @JsonIgnore
    @OneToOne(mappedBy = "office")
    private Doctor doctor;

    public static Office open(Details details) {
        Office office = new Office();
        office.revise(details);
        return office;
    }

    public void revise(Details details) {
        this.name = details.name();
        this.officeNumber = details.officeNumber();
    }

    public boolean hasDoctor() {
        return doctor != null;
    }

    void releaseDoctor() {
        doctor = null;
    }

    public record Details(String name, String officeNumber) {

        public Details {
            name = normalize(name);
            officeNumber = normalize(officeNumber);
        }

        public void requireValid() {
            ensure(!isBlank(officeNumber), "Office number is required");
            ensure(!isNotPositiveNumber(officeNumber), "Office number must be a positive number");
        }
    }
}
