package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.services.PatientService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/patient", "/api/patients"})
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/save")
    public ResponseEntity<?> savePatient(@RequestBody Patient patient) {
        return ApiResponses.fromMessage(patientService.savePatient(patient), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable UUID id, @RequestBody Patient patient) {
        return ApiResponses.fromMessage(patientService.updatePatient(id, patient), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/by-last-name")
    public ResponseEntity<?> getPatientsByLastName(@RequestParam String lastName) {
        return ResponseEntity.ok(patientService.getPatientsByLastName(lastName));
    }

    @GetMapping("/frequent")
    public ResponseEntity<?> getFrequentPatients(@RequestParam long min) {
        return ResponseEntity.ok(patientService.getFrequentPatients(min));
    }

    @GetMapping("/of-doctor/{doctorId}")
    public ResponseEntity<?> getPatientsOfDoctor(@PathVariable UUID doctorId) {
        return ApiResponses.fromData(patientService.getPatientsOfDoctor(doctorId).orElse(null),
                "The doctor with that id does not exist");
    }

    @GetMapping("/{ssNumber}/upcoming")
    public ResponseEntity<?> getUpcomingAppointments(@PathVariable String ssNumber) {
        return ApiResponses.fromData(appointmentService.getUpcomingAppointments(ssNumber).orElse(null),
                "That patient with that ssnumber does not exist");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable UUID id) {
        return ApiResponses.fromData(patientService.getPatientById(id).orElse(null), "Patient not found");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable UUID id) {
        return ApiResponses.fromMessage(patientService.deletePatient(id), HttpStatus.OK);
    }
}
