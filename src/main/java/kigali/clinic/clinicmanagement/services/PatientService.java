package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Validations.containsDigit;
import static kigali.clinic.clinicmanagement.utils.Validations.isBlank;
import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Transactional
    public String savePatient(Patient patient) {

        String error = validateNames(patient);

        if (error != null) {
            return error;
        }

        // two patients can carry the same name, so no duplicate check here
        patient.setId(null);
        patient.setFirstName(normalize(patient.getFirstName()));
        patient.setLastName(normalize(patient.getLastName()));
        patientRepo.save(patient);

        return "Patient is saved Successfully";
    }

    @Transactional
    public String updatePatient(UUID id, Patient patient) {

        Patient existing = patientRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Patient not found";
        }

        String error = validateNames(patient);

        if (error != null) {
            return error;
        }

        existing.setFirstName(normalize(patient.getFirstName()));
        existing.setLastName(normalize(patient.getLastName()));
        patientRepo.save(existing);

        return "Patient is updated Successfully";
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public List<Patient> getPatientsByLastName(String lastName) {
        return patientRepo.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName.trim());
    }

    public Patient getPatientById(UUID id) {
        return patientRepo.findById(id).orElse(null);
    }

    @Transactional
    public String deletePatient(UUID id) {

        Patient existing = patientRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Patient not found";
        }

        if (appointmentRepo.existsByPatientId(id)) {
            return "Patient already has appointments and can not be deleted";
        }

        patientRepo.delete(existing);

        return "Patient is deleted Successfully";
    }

    private String validateNames(Patient patient) {

        if (isBlank(patient.getFirstName())) {
            return "Patient first name is required";
        }

        if (isBlank(patient.getLastName())) {
            return "Patient last name is required";
        }

        if (containsDigit(patient.getFirstName()) || containsDigit(patient.getLastName())) {
            return "Patient names must not contain numbers";
        }

        return null;
    }
}
