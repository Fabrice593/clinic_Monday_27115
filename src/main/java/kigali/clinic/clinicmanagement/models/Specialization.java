package kigali.clinic.clinicmanagement.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name ="specializations")
public class Specialization extends BaseEntity {

    @Column(name = "name")
    private String name;


    @JsonIgnore
    @ManyToMany(mappedBy = "specializations")
    private List<Doctor> doctors = new ArrayList<>();

}
