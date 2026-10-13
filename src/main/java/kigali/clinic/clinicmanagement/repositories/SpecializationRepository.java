package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    // B3: IS EMPTY tests the doctors collection directly, no counting
    @Query("SELECT s FROM Specialization s WHERE s.doctors IS EMPTY ORDER BY s.name ASC")
    List<Specialization> findUnusedSpecializations();
}
