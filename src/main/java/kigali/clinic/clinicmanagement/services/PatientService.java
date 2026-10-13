package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
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
public class PatientService {

    private static final String PATIENT_NOT_FOUND = "Patient not found";

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public void register(Patient.Details details) {
        details.requireValid();
        requireUnusedSsNumber(details.ssNumber(), null);
        patientRepository.save(Patient.register(details));
    }

    @Transactional
    public void revise(UUID patientId, Patient.Details details) {
        Patient patient = patientById(patientId);
        details.requireValid();
        requireUnusedSsNumber(details.ssNumber(), patientId);
        patient.revise(details);
    }

    public List<Patient> allPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> withLastName(String lastName) {
        return patientRepository.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName.trim());
    }

    public List<Patient> frequentPatients(long minimumVisits) {
        return patientRepository.findFrequentPatients(minimumVisits);
    }

    public List<Patient> patientsOfDoctor(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw notFound(DoctorService.DOCTOR_NOT_FOUND);
        }

        return patientRepository.findPatientsOfDoctor(doctorId);
    }

    public Patient patientById(UUID patientId) {
        return patientRepository.findById(patientId).orElseThrow(() -> notFound(PATIENT_NOT_FOUND));
    }

    @Transactional
    public void delete(UUID patientId) {
        Patient patient = patientById(patientId);

        if (appointmentRepository.existsByPatientId(patientId)) {
            throw conflict("Patient already has appointments and can not be deleted");
        }

        patientRepository.delete(patient);
    }

    private void requireUnusedSsNumber(String ssNumber, UUID patientId) {
        boolean ssNumberTaken = patientId == null
                ? patientRepository.existsBySsNumber(ssNumber)
                : patientRepository.existsBySsNumberAndIdNot(ssNumber, patientId);

        if (ssNumberTaken) {
            throw conflict("A patient with that ss number already exists");
        }
    }
}
