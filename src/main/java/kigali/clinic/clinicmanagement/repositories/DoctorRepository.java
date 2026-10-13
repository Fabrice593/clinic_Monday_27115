package kigali.clinic.clinicmanagement.repositories;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

    boolean existsByOfficeId(UUID officeId);

    boolean existsByOfficeIdAndIdNot(UUID officeId, UUID id);

    @Query("""
            SELECT DISTINCT d FROM Doctor d JOIN d.specializations s
            WHERE LOWER(s.name) = LOWER(:name)
            ORDER BY d.lastName ASC, d.firstName ASC
            """)
    List<Doctor> findAllBySpecializationName(@Param("name") String name);

    @Query("""
            SELECT d FROM Doctor d
            WHERE d.office IS NULL
            ORDER BY d.lastName ASC, d.firstName ASC
            """)
    List<Doctor> findAllWithoutOffice();

    @Query("""
            SELECT d FROM Doctor d
            WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :fragment, '%'))
               OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :fragment, '%'))
            ORDER BY d.lastName ASC, d.firstName ASC
            """)
    List<Doctor> searchByNameFragment(@Param("fragment") String fragment);

    @Query("""
            SELECT new kigali.clinic.clinicmanagement.repositories.DoctorRepository$Workload(
                d.firstName, d.lastName, COUNT(a))
            FROM Appointment a JOIN a.doctor d
            WHERE a.status = kigali.clinic.clinicmanagement.models.AppointmentStatus.COMPLETED
              AND a.appointmentDate BETWEEN :start AND :end
            GROUP BY d.id, d.firstName, d.lastName
            ORDER BY COUNT(a) DESC, d.lastName ASC
            """)
    List<Workload> summarizeCompletedWorkload(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"firstName", "lastName", "completedAppointments"})
    record Workload(String firstName, String lastName, long completedAppointments) {
    }
}
