package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByDoctorIdAndDateAndStatusNot(UUID doctorId, LocalDateTime date, String status);

    boolean existsByDoctorIdAndDateAndStatusNotAndIdNot(UUID doctorId, LocalDateTime date, String status, UUID id);

    List<Appointment> findByStatusOrderByDateAsc(String status);

    List<Appointment> findByDateBetweenOrderByDateAsc(LocalDateTime start, LocalDateTime end);

    boolean existsByDoctorId(UUID doctorId);

    boolean existsByPatientId(UUID patientId);
}
