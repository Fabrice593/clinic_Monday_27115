package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Office;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.isNotPositiveNumber;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository officeRepo;

    @Transactional
    public String saveOffice(Office office) {

        if (isBlank(office.getOfficeNumber())) {
            return "Office number is required";
        }

        String officeNumber = normalize(office.getOfficeNumber());

        if (isNotPositiveNumber(officeNumber)) {
            return "Office number must be a positive number";
        }

        if (officeRepo.existsByOfficeNumberIgnoreCase(officeNumber)) {
            return "Office with number " + officeNumber + " already exists";
        }

        office.setId(null);
        office.setOfficeNumber(officeNumber);
        officeRepo.save(office);

        return "Office is saved Successfully";
    }

    @Transactional
    public String updateOffice(UUID id, Office office) {

        Office existing = officeRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Office not found";
        }

        if (isBlank(office.getOfficeNumber())) {
            return "Office number is required";
        }

        String officeNumber = normalize(office.getOfficeNumber());

        if (isNotPositiveNumber(officeNumber)) {
            return "Office number must be a positive number";
        }

        if (officeRepo.existsByOfficeNumberIgnoreCaseAndIdNot(officeNumber, id)) {
            return "Office with number " + officeNumber + " already exists";
        }

        existing.setOfficeNumber(officeNumber);
        officeRepo.save(existing);

        return "Office is updated Successfully";
    }

    public List<Office> getAllOffices() {
        return officeRepo.findAll();
    }

    public Office getOfficeById(UUID id) {
        return officeRepo.findById(id).orElse(null);
    }

    @Transactional
    public String deleteOffice(UUID id) {

        Office existing = officeRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Office not found";
        }

        if (existing.getDoctor() != null) {
            return "Office is already assigned to a doctor and can not be deleted";
        }

        officeRepo.delete(existing);

        return "Office is deleted Successfully";
    }
}
