package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.BaseEntity.Reference;
import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.models.Office;
import kigali.clinic.clinicmanagement.models.Specialization;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository.Workload;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository;
import kigali.clinic.clinicmanagement.repositories.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.ApiResponses.conflict;
import static kigali.clinic.clinicmanagement.utils.ApiResponses.notFound;
import static kigali.clinic.clinicmanagement.utils.Dates.endOfDay;
import static kigali.clinic.clinicmanagement.utils.Validations.ensure;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    public static final String DOCTOR_NOT_FOUND = "The doctor with that id does not exist";

    private static final String SPECIALIZATION_NOT_FOUND = "That specialization does not exist";

    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void register(Doctor.Details details) {
        details.requireValidNames();
        Office office = assignableOffice(details.office(), null);
        doctorRepository.save(Doctor.register(details, office, specializationsFor(details.specializations())));
    }

    @Transactional
    public void revise(UUID doctorId, Doctor.Details details) {
        Doctor doctor = doctorById(doctorId);
        details.requireValidNames();
        Office office = assignableOffice(details.office(), doctorId);
        doctor.revise(details, office, specializationsFor(details.specializations()));
    }

    public List<Doctor> allDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> withSpecialization(String specializationName) {
        return doctorRepository.findAllBySpecializationName(specializationName.trim());
    }

    public List<Doctor> withoutOffice() {
        return doctorRepository.findAllWithoutOffice();
    }

    public List<Doctor> searchByName(String nameFragment) {
        return doctorRepository.searchByNameFragment(nameFragment.trim());
    }

    public List<Workload> completedWorkload(LocalDate firstDay, LocalDate lastDay) {
        return doctorRepository.summarizeCompletedWorkload(firstDay.atStartOfDay(), endOfDay(lastDay));
    }

    public Doctor doctorById(UUID doctorId) {
        return doctorRepository.findById(doctorId).orElseThrow(() -> notFound(DOCTOR_NOT_FOUND));
    }

    @Transactional
    public void addSpecialization(UUID doctorId, UUID specializationId) {
        Doctor doctor = doctorById(doctorId);

        if (!doctor.addSpecialization(existingSpecialization(specializationId))) {
            throw conflict("The doctor already has that specialization");
        }
    }

    @Transactional
    public void removeSpecialization(UUID doctorId, UUID specializationId) {
        Doctor doctor = doctorById(doctorId);

        if (!doctor.removeSpecialization(existingSpecialization(specializationId))) {
            throw conflict("The doctor does not have that specialization");
        }
    }

    @Transactional
    public void moveToOffice(UUID doctorId, String officeNumber) {
        Doctor doctor = doctorById(doctorId);
        Office office = officeRepository.findByOfficeNumber(normalize(officeNumber))
                .orElseThrow(() -> notFound("The office with that office number does not exist"));

        if (officeHeldByAnotherDoctor(office.getId(), doctorId)) {
            throw conflict("That office is taken by other doctor");
        }

        doctor.moveTo(office);
    }

    @Transactional
    public void delete(UUID doctorId) {
        Doctor doctor = doctorById(doctorId);

        if (appointmentRepository.existsByDoctorId(doctorId)) {
            throw conflict("Doctor already has appointments and can not be deleted");
        }

        doctor.vacateOffice();
        doctorRepository.delete(doctor);
    }

    private Office assignableOffice(Reference officeReference, UUID doctorId) {
        if (officeReference == null || officeReference.id() == null) {
            return null;
        }

        Office office = officeRepository.findById(officeReference.id())
                .orElseThrow(() -> notFound("Office not found"));

        if (officeHeldByAnotherDoctor(office.getId(), doctorId)) {
            throw conflict("Office is already assigned to another doctor");
        }

        return office;
    }

    private boolean officeHeldByAnotherDoctor(UUID officeId, UUID doctorId) {
        return doctorId == null
                ? doctorRepository.existsByOfficeId(officeId)
                : doctorRepository.existsByOfficeIdAndIdNot(officeId, doctorId);
    }

    private List<Specialization> specializationsFor(List<Reference> specializationReferences) {
        return Optional.ofNullable(specializationReferences).orElseGet(List::of).stream()
                .map(this::specializationFor)
                .toList();
    }

    private Specialization specializationFor(Reference specializationReference) {
        ensure(specializationReference != null && specializationReference.id() != null,
                "Specialization id is required");

        return specializationRepository.findById(specializationReference.id())
                .orElseThrow(() -> notFound("Specialization not found"));
    }

    private Specialization existingSpecialization(UUID specializationId) {
        return specializationRepository.findById(specializationId)
                .orElseThrow(() -> notFound(SPECIALIZATION_NOT_FOUND));
    }
}
