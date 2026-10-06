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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Validations.containsDigit;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class DoctorService {

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
            return "Doctor not found";
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

    public Doctor getDoctorById(UUID id) {
        return doctorRepo.findById(id).orElse(null);
    }

    @Transactional
    public String deleteDoctor(UUID id) {

        Doctor existing = doctorRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Doctor not found";
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
