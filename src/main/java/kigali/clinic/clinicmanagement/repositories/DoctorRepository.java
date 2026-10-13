package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(String firstName, String lastName, UUID id);

    boolean existsByOfficeId(UUID officeId);

    boolean existsByOfficeIdAndIdNot(UUID officeId, UUID id);

    // B1: specializations is a collection, so it needs a real JOIN instead of a dot path
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.specializations s "
            + "WHERE LOWER(s.name) = LOWER(:name) "
            + "ORDER BY d.lastName ASC, d.firstName ASC")
    List<Doctor> findDoctorsBySpecialization(@Param("name") String name);

    // B2
    @Query("SELECT d FROM Doctor d WHERE d.office IS NULL ORDER BY d.lastName ASC, d.firstName ASC")
    List<Doctor> findDoctorsWithoutOffice();

    // D3: "gis" matches Mugisha; LOWER on both sides ignores letter case
    @Query("SELECT d FROM Doctor d "
            + "WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "ORDER BY d.lastName ASC, d.firstName ASC")
    List<Doctor> searchByName(@Param("name") String name);

    // bonus: the inner JOIN + WHERE leave out doctors with no COMPLETED appointment in the range
    @Query("SELECT d.firstName, d.lastName, COUNT(a) FROM Appointment a JOIN a.doctor d "
            + "WHERE a.status = kigali.clinic.clinicmanagement.models.AppointmentStatus.COMPLETED "
            + "AND a.appointmentDate BETWEEN :start AND :end "
            + "GROUP BY d.id, d.firstName, d.lastName "
            + "ORDER BY COUNT(a) DESC, d.lastName ASC")
    List<Object[]> findDoctorWorkload(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
