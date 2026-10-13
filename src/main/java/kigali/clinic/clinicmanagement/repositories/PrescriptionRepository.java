package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    // C2
    List<Prescription> findByAppointment(Appointment appointment);

    // C4: an appointment with prescriptions can not be deleted
    boolean existsByAppointmentId(UUID appointmentId);

    // C3: single-valued relations are walked with dots, no JOIN needed
    @Query("SELECT p FROM Prescription p WHERE p.appointment.patient.ssNumber = :ssNumber "
            + "ORDER BY p.issuedDate DESC, p.createdAt DESC")
    List<Prescription> findPatientHistory(@Param("ssNumber") String ssNumber);

    // bonus: one row per medicine, e.g. ["Amoxicillin", 3]
    @Query("SELECT p.medicineName, COUNT(p) FROM Prescription p "
            + "GROUP BY p.medicineName ORDER BY COUNT(p) DESC, p.medicineName ASC")
    List<Object[]> findTopMedicines();
}
