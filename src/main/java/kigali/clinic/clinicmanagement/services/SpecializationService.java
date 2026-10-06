package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Specialization;
import kigali.clinic.clinicmanagement.repositories.SpecializationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepo;

    @Transactional
    public String saveSpecialization(Specialization specialization) {

        if (isBlank(specialization.getName())) {
            return "Specialization name is required";
        }

        String name = normalize(specialization.getName());

        if (specializationRepo.existsByNameIgnoreCase(name)) {
            return "Specialization " + name + " already exists";
        }

        specialization.setId(null);
        specialization.setName(name);
        specializationRepo.save(specialization);

        return "Specialization is saved Successfully";
    }

    @Transactional
    public String updateSpecialization(UUID id, Specialization specialization) {

        Specialization existing = specializationRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Specialization not found";
        }

        if (isBlank(specialization.getName())) {
            return "Specialization name is required";
        }

        String name = normalize(specialization.getName());

        if (specializationRepo.existsByNameIgnoreCaseAndIdNot(name, id)) {
            return "Specialization " + name + " already exists";
        }

        existing.setName(name);
        specializationRepo.save(existing);

        return "Specialization is updated Successfully";
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepo.findAll();
    }

    public Specialization getSpecializationById(UUID id) {
        return specializationRepo.findById(id).orElse(null);
    }

    @Transactional
    public String deleteSpecialization(UUID id) {

        Specialization existing = specializationRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Specialization not found";
        }

        if (!existing.getDoctors().isEmpty()) {
            return "Specialization is already given to doctors and can not be deleted";
        }

        specializationRepo.delete(existing);

        return "Specialization is deleted Successfully";
    }
}
