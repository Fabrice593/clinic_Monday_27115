package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OfficeRepository extends JpaRepository<Office, UUID> {

    boolean existsByOfficeNumberIgnoreCase(String officeNumber);

    boolean existsByOfficeNumberIgnoreCaseAndIdNot(String officeNumber, UUID id);
}
