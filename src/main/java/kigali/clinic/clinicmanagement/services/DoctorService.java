package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.models.Office;
import kigali.clinic.clinicmanagement.models.Specialization;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository;
import kigali.clinic.clinicmanagement.repositories.SpecializationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Dates.endOfDay;
import static kigali.clinic.clinicmanagement.utils.Validations.containsDigit;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class DoctorService {

    public static final String DOCTOR_NOT_FOUND = "The doctor with that id does not exist";

    private static final String SPECIALIZATION_NOT_FOUND = "That specialization does not exist";

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private OfficeRepository officeRepo;

    @Autowired
    private SpecializationRepository specializationRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Transactional
    public String saveDoctor(Doctor doctor) {

        String error = validateNames(doctor);

        if (error != null) {
            return error;
        }

        error = resolveOffice(doctor, null);

        if (error != null) {
            return error;
        }

        error = resolveSpecializations(doctor);

        if (error != null) {
            return error;
        }

        // doctors can share the same name, so only the office is kept unique
        doctor.setId(null);
        doctor.setFirstName(normalize(doctor.getFirstName()));
        doctor.setLastName(normalize(doctor.getLastName()));
        doctorRepo.save(doctor);

        return "Doctor is saved Successfully";
    }

    @Transactional
    public String updateDoctor(UUID id, Doctor doctor) {

        Doctor existing = doctorRepo.findById(id).orElse(null);

        if (existing == null) {
            return DOCTOR_NOT_FOUND;
        }

        String error = validateNames(doctor);

        if (error != null) {
            return error;
        }

        error = resolveOffice(doctor, id);

        if (error != null) {
            return error;
        }

        error = resolveSpecializations(doctor);

        if (error != null) {
            return error;
        }

        existing.setFirstName(normalize(doctor.getFirstName()));
        existing.setLastName(normalize(doctor.getLastName()));
        existing.setOffice(doctor.getOffice());
        existing.setSpecializations(doctor.getSpecializations());
        doctorRepo.save(existing);

        return "Doctor is updated Successfully";
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    public List<Doctor> getDoctorsBySpecialization(String name) {
        return doctorRepo.findDoctorsBySpecialization(name.trim());
    }

    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorRepo.findDoctorsWithoutOffice();
    }

    public List<Doctor> searchDoctors(String name) {
        return doctorRepo.searchByName(name.trim());
    }

    // bonus: both dates are inclusive, so the range runs to the very end of the last day
    public List<Object[]> getWorkload(LocalDate start, LocalDate end) {
        return doctorRepo.findDoctorWorkload(start.atStartOfDay(), endOfDay(end));
    }

    public Optional<Doctor> getDoctorById(UUID id) {
        return doctorRepo.findById(id);
    }

    // A1 + A2: Doctor owns the join table, so editing its list and saving the doctor writes the link
    @Transactional
    public String addSpecialization(UUID doctorId, UUID specializationId) {

        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        if (doctor == null) {
            return DOCTOR_NOT_FOUND;
        }

        Specialization specialization = specializationRepo.findById(specializationId).orElse(null);

        if (specialization == null) {
            return SPECIALIZATION_NOT_FOUND;
        }

        if (doctor.getSpecializations().contains(specialization)) {
            return "The doctor already has that specialization";
        }

        doctor.getSpecializations().add(specialization);
        doctorRepo.save(doctor);

        return "Specialization added to the doctor";
    }

    // only the row in doctor_specializations goes; the specialization itself stays
    @Transactional
    public String removeSpecialization(UUID doctorId, UUID specializationId) {

        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        if (doctor == null) {
            return DOCTOR_NOT_FOUND;
        }

        Specialization specialization = specializationRepo.findById(specializationId).orElse(null);

        if (specialization == null) {
            return SPECIALIZATION_NOT_FOUND;
        }

        if (!doctor.getSpecializations().remove(specialization)) {
            return "The doctor does not have that specialization";
        }

        doctorRepo.save(doctor);

        return "Specialization removed from the doctor";
    }

    // A4: Doctor holds office_id, so the move is saved on the doctor and the old office is free again
    @Transactional
    public String changeOffice(UUID doctorId, String officeNumber) {

        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        if (doctor == null) {
            return DOCTOR_NOT_FOUND;
        }

        Office office = officeRepo.findByOfficeNumber(normalize(officeNumber)).orElse(null);

        if (office == null) {
            return "The office with that office number does not exist";
        }

        if (doctorRepo.existsByOfficeIdAndIdNot(office.getId(), doctorId)) {
            return "That office is taken by other doctor";
        }

        doctor.setOffice(office);
        doctorRepo.save(doctor);

        return "Office changed successfully";
    }

    @Transactional
    public String deleteDoctor(UUID id) {

        Doctor existing = doctorRepo.findById(id).orElse(null);

        if (existing == null) {
            return DOCTOR_NOT_FOUND;
        }

        if (appointmentRepo.existsByDoctorId(id)) {
            return "Doctor already has appointments and can not be deleted";
        }

        // the office holds a back reference, so it has to let go before the row disappears
        if (existing.getOffice() != null) {
            existing.getOffice().setDoctor(null);
            existing.setOffice(null);
        }

        doctorRepo.delete(existing);

        return "Doctor is deleted Successfully";
    }

    private String validateNames(Doctor doctor) {

        if (isBlank(doctor.getFirstName())) {
            return "Doctor first name is required";
        }

        if (isBlank(doctor.getLastName())) {
            return "Doctor last name is required";
        }

        if (containsDigit(doctor.getFirstName()) || containsDigit(doctor.getLastName())) {
            return "Doctor names must not contain numbers";
        }

        return null;
    }

    private String resolveOffice(Doctor doctor, UUID currentDoctorId) {

        if (doctor.getOffice() == null || doctor.getOffice().getId() == null) {
            doctor.setOffice(null);
            return null;
        }

        UUID officeId = doctor.getOffice().getId();
        Office office = officeRepo.findById(officeId).orElse(null);

        if (office == null) {
            return "Office not found";
        }

        boolean taken = currentDoctorId == null
                ? doctorRepo.existsByOfficeId(officeId)
                : doctorRepo.existsByOfficeIdAndIdNot(officeId, currentDoctorId);

        if (taken) {
            return "Office is already assigned to another doctor";
        }

        doctor.setOffice(office);

        return null;
    }

    private String resolveSpecializations(Doctor doctor) {

        List<Specialization> found = new ArrayList<>();

        if (doctor.getSpecializations() != null) {

            for (Specialization item : doctor.getSpecializations()) {

                if (item.getId() == null) {
                    return "Specialization id is required";
                }

                Specialization specialization = specializationRepo.findById(item.getId()).orElse(null);

                if (specialization == null) {
                    return "Specialization not found";
                }

                found.add(specialization);
            }
        }

        doctor.setSpecializations(found);

        return null;
    }
}
