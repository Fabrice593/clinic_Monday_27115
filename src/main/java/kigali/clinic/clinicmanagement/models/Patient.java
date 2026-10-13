package kigali.clinic.clinicmanagement.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name ="patients")

public class Patient extends BaseEntity  {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    // social security number: the patient's public key in URLs like /api/patients/{ssNumber}/upcoming
    @Column(name = "ss_number", unique = true, length = 30)
    private String ssNumber;

}
