package kigali.clinic.clinicmanagement.repositories;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByDoctorIdAndAppointmentDateBetweenAndStatusNot(
            UUID doctorId, LocalDateTime dayStart, LocalDateTime dayEnd, AppointmentStatus status);

    boolean existsByDoctorIdAndAppointmentDateBetweenAndStatusNotAndIdNot(
            UUID doctorId, LocalDateTime dayStart, LocalDateTime dayEnd, AppointmentStatus status, UUID id);

    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(LocalDateTime start, LocalDateTime end);

    boolean existsByDoctorId(UUID doctorId);

    boolean existsByPatientId(UUID patientId);

    List<Appointment> findByDoctorIdAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDateAsc(
            UUID doctorId, LocalDateTime dayStart, LocalDateTime dayEnd, AppointmentStatus status);

    List<Appointment> findByPatientAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
            Patient patient, LocalDateTime from, List<AppointmentStatus> statuses);

    long countByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(AppointmentStatus status);

    @Query("""
            SELECT new kigali.clinic.clinicmanagement.repositories.AppointmentRepository$StatusCount(
                a.status, COUNT(a))
            FROM Appointment a
            GROUP BY a.status
            ORDER BY COUNT(a) DESC, a.status ASC
            """)
    List<StatusCount> countPerStatus();

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Appointment a
            SET a.status = kigali.clinic.clinicmanagement.models.AppointmentStatus.CANCELLED,
                a.updatedAt = :now
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate >= :dayStart AND a.appointmentDate < :nextDayStart
              AND a.status NOT IN (kigali.clinic.clinicmanagement.models.AppointmentStatus.COMPLETED,
                                   kigali.clinic.clinicmanagement.models.AppointmentStatus.CANCELLED)
            """)
    int cancelDoctorDay(@Param("doctorId") UUID doctorId,
                        @Param("dayStart") LocalDateTime dayStart,
                        @Param("nextDayStart") LocalDateTime nextDayStart,
                        @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM Appointment a
            WHERE a.status = kigali.clinic.clinicmanagement.models.AppointmentStatus.CANCELLED
              AND a.appointmentDate < :cutoff
            """)
    int deleteCancelledBefore(@Param("cutoff") LocalDateTime cutoff);

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"status", "count"})
    record StatusCount(AppointmentStatus status, long count) {
    }
}
