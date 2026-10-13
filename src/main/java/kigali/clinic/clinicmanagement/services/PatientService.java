package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

    @Autowired
    private DoctorRepository doctorRepo;

    @Transactional
    public String savePatient(Patient patient) {

        String error = validatePatient(patient, null);

        if (error != null) {
            return error;
        }

        // two patients can carry the same name; only the ss number must be unique
        patient.setId(null);
        patient.setFirstName(normalize(patient.getFirstName()));
        patient.setLastName(normalize(patient.getLastName()));
        patient.setSsNumber(normalize(patient.getSsNumber()));
        patientRepo.save(patient);

        return "Patient is saved Successfully";
    }

    @Transactional
    public String updatePatient(UUID id, Patient patient) {

        Patient existing = patientRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Patient not found";
        }

        String error = validatePatient(patient, id);

        if (error != null) {
            return error;
        }

        existing.setFirstName(normalize(patient.getFirstName()));
        existing.setLastName(normalize(patient.getLastName()));
        existing.setSsNumber(normalize(patient.getSsNumber()));
        patientRepo.save(existing);

        return "Patient is updated Successfully";
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public List<Patient> getPatientsByLastName(String lastName) {
        return patientRepo.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName.trim());
    }

    public List<Patient> getFrequentPatients(long min) {
        return patientRepo.findFrequentPatients(min);
    }

    // empty means the doctor does not exist; an empty list means the doctor has no patients yet
    public Optional<List<Patient>> getPatientsOfDoctor(UUID doctorId) {

        if (!doctorRepo.existsById(doctorId)) {
            return Optional.empty();
        }

        return Optional.of(patientRepo.findPatientsOfDoctor(doctorId));
    }

    public Optional<Patient> getPatientById(UUID id) {
        return patientRepo.findById(id);
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

    // currentId is null on save and the patient's own id on update
    private String validatePatient(Patient patient, UUID currentId) {

        if (isBlank(patient.getFirstName())) {
            return "Patient first name is required";
        }

        if (isBlank(patient.getLastName())) {
            return "Patient last name is required";
        }

        if (containsDigit(patient.getFirstName()) || containsDigit(patient.getLastName())) {
            return "Patient names must not contain numbers";
        }

        if (isBlank(patient.getSsNumber())) {
            return "Patient ss number is required";
        }

        String ssNumber = normalize(patient.getSsNumber());
        boolean taken = currentId == null
                ? patientRepo.existsBySsNumber(ssNumber)
                : patientRepo.existsBySsNumberAndIdNot(ssNumber, currentId);

        if (taken) {
            return "A patient with that ss number already exists";
        }

        return null;
    }
}
