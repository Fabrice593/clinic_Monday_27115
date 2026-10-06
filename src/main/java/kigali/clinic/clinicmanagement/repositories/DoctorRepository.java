package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(String firstName, String lastName, UUID id);

    boolean existsByOfficeId(UUID officeId);

    boolean existsByOfficeIdAndIdNot(UUID officeId, UUID id);
}
