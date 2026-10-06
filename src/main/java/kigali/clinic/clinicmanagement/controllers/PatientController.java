package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.services.PatientService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = {"/api/patient", "/api/patients"})
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> savePatient(@RequestBody Patient patient) {
        return ApiResponses.fromMessage(patientService.savePatient(patient), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePatient(@PathVariable UUID id, @RequestBody Patient patient) {
        return ApiResponses.fromMessage(patientService.updatePatient(id, patient), HttpStatus.OK);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping(value = "/by-last-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPatientsByLastName(@RequestParam String lastName) {
        return ResponseEntity.ok(patientService.getPatientsByLastName(lastName));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPatientById(@PathVariable UUID id) {
        return ApiResponses.fromData(patientService.getPatientById(id), "Patient not found");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deletePatient(@PathVariable UUID id) {
        return ApiResponses.fromMessage(patientService.deletePatient(id), HttpStatus.OK);
    }
}
