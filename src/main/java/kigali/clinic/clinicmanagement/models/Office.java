package kigali.clinic.clinicmanagement.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name ="offices")
public class Office extends BaseEntity  {


    @Column(name = "office_number", nullable = false, length = 20)
    private String officeNumber;

    @JsonIgnore
    @OneToOne(mappedBy = "office")
    private Doctor doctor;
}
