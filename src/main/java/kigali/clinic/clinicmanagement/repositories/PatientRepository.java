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

    boolean existsBySsNumber(String ssNumber);

    boolean existsBySsNumberAndIdNot(String ssNumber, UUID id);

    Optional<Patient> findBySsNumber(String ssNumber);

    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Patient> findPatientsOfDoctor(@Param("doctorId") UUID doctorId);

    @Query("""
            SELECT p FROM Appointment a JOIN a.patient p
            GROUP BY p
            HAVING COUNT(a) >= :minimumVisits
            ORDER BY COUNT(a) DESC, p.lastName ASC, p.firstName ASC
            """)
    List<Patient> findFrequentPatients(@Param("minimumVisits") long minimumVisits);
}
