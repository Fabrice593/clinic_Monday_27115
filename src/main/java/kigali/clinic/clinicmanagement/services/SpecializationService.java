package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Specialization;
import kigali.clinic.clinicmanagement.repositories.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.ApiResponses.conflict;
import static kigali.clinic.clinicmanagement.utils.ApiResponses.notFound;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecializationService {

    private static final String SPECIALIZATION_NOT_FOUND = "Specialization not found";

    private final SpecializationRepository specializationRepository;

    @Transactional
    public void define(Specialization.Details details) {
        details.requireValid();
        requireUnusedName(details.name(), null);
        specializationRepository.save(Specialization.define(details));
    }

    @Transactional
    public void revise(UUID specializationId, Specialization.Details details) {
        Specialization specialization = specializationById(specializationId);
        details.requireValid();
        requireUnusedName(details.name(), specializationId);
        specialization.revise(details);
    }

    public List<Specialization> allSpecializations() {
        return specializationRepository.findAll();
    }

    public List<Specialization> unusedSpecializations() {
        return specializationRepository.findUnusedSpecializations();
    }

    public Specialization specializationById(UUID specializationId) {
        return specializationRepository.findById(specializationId)
                .orElseThrow(() -> notFound(SPECIALIZATION_NOT_FOUND));
    }

    @Transactional
    public void delete(UUID specializationId) {
        Specialization specialization = specializationById(specializationId);

        if (specialization.hasDoctors()) {
            throw conflict("Specialization is already given to doctors and can not be deleted");
        }

        specializationRepository.delete(specialization);
    }

    private void requireUnusedName(String name, UUID specializationId) {
        boolean nameTaken = specializationId == null
                ? specializationRepository.existsByNameIgnoreCase(name)
                : specializationRepository.existsByNameIgnoreCaseAndIdNot(name, specializationId);

        if (nameTaken) {
            throw conflict("Specialization " + name + " already exists");
        }
    }
}
