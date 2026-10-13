package kigali.clinic.clinicmanagement.repositories;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kigali.clinic.clinicmanagement.models.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    List<Prescription> findAllByAppointmentIdOrderByCreatedAtAsc(UUID appointmentId);

    boolean existsByAppointmentId(UUID appointmentId);

    @Query("""
            SELECT p FROM Prescription p
            WHERE p.appointment.patient.ssNumber = :ssNumber
            ORDER BY p.issuedDate DESC, p.createdAt DESC
            """)
    List<Prescription> findHistoryOfPatient(@Param("ssNumber") String ssNumber);

    @Query("""
            SELECT new kigali.clinic.clinicmanagement.repositories.PrescriptionRepository$MedicineUsage(
                p.medicineName, COUNT(p))
            FROM Prescription p
            GROUP BY p.medicineName
            ORDER BY COUNT(p) DESC, p.medicineName ASC
            """)
    List<MedicineUsage> rankMedicinesByUsage();

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"medicineName", "timesPrescribed"})
    record MedicineUsage(String medicineName, long timesPrescribed) {
    }
}
