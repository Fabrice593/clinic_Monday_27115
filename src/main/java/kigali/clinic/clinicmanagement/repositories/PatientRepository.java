package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(String firstName, String lastName, UUID id);

    boolean existsBySsNumber(String ssNumber);

    boolean existsBySsNumberAndIdNot(String ssNumber, UUID id);

    Optional<Patient> findBySsNumber(String ssNumber);

    // B4: Patient has no link to its appointments, so the query starts from Appointment
    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Patient> findPatientsOfDoctor(@Param("doctorId") UUID doctorId);

    // C2
    @Query("SELECT p FROM Appointment a JOIN a.patient p "
            + "GROUP BY p HAVING COUNT(a) >= :min "
            + "ORDER BY COUNT(a) DESC, p.lastName ASC, p.firstName ASC")
    List<Patient> findFrequentPatients(@Param("min") long min);
}
