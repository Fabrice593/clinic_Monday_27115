package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Office;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository.OfficeLoad;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.ApiResponses.conflict;
import static kigali.clinic.clinicmanagement.utils.ApiResponses.notFound;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficeService {

    private static final String OFFICE_NOT_FOUND = "Office not found";

    private final OfficeRepository officeRepository;

    @Transactional
    public void open(Office.Details details) {
        details.requireValid();
        requireUnusedNumber(details.officeNumber(), null);
        officeRepository.save(Office.open(details));
    }

    @Transactional
    public void revise(UUID officeId, Office.Details details) {
        Office office = officeById(officeId);
        details.requireValid();
        requireUnusedNumber(details.officeNumber(), officeId);
        office.revise(details);
    }

    public List<Office> allOffices() {
        return officeRepository.findAll();
    }

    public List<Office> freeOffices() {
        return officeRepository.findFreeOffices();
    }

    public OfficeLoad busiestOffice() {
        return officeRepository.rankByAppointmentCount(Limit.of(1)).stream()
                .findFirst()
                .orElseThrow(() -> notFound("No appointments yet"));
    }

    public Office officeById(UUID officeId) {
        return officeRepository.findById(officeId).orElseThrow(() -> notFound(OFFICE_NOT_FOUND));
    }

    @Transactional
    public void delete(UUID officeId) {
        Office office = officeById(officeId);

        if (office.hasDoctor()) {
            throw conflict("Office is already assigned to a doctor and can not be deleted");
        }

        officeRepository.delete(office);
    }

    private void requireUnusedNumber(String officeNumber, UUID officeId) {
        boolean numberTaken = officeId == null
                ? officeRepository.existsByOfficeNumberIgnoreCase(officeNumber)
                : officeRepository.existsByOfficeNumberIgnoreCaseAndIdNot(officeNumber, officeId);

        if (numberTaken) {
            throw conflict("Office with number " + officeNumber + " already exists");
        }
    }
}
