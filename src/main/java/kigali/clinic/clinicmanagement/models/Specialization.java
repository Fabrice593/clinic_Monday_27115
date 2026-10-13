package kigali.clinic.clinicmanagement.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static kigali.clinic.clinicmanagement.utils.Validations.ensure;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Entity
@Getter
@Table(name = "specializations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Specialization extends BaseEntity {

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "specializations")
    private List<Doctor> doctors = new ArrayList<>();

    public static Specialization define(Details details) {
        Specialization specialization = new Specialization();
        specialization.revise(details);
        return specialization;
    }

    public void revise(Details details) {
        this.name = details.name();
    }

    public boolean hasDoctors() {
        return !doctors.isEmpty();
    }

    public record Details(String name) {

        public Details {
            name = normalize(name);
        }

        public void requireValid() {
            ensure(!isBlank(name), "Specialization name is required");
        }
    }
}
