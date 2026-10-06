package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.services.DoctorService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor) {
        return ApiResponses.fromMessage(doctorService.saveDoctor(doctor), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctor) {
        return ApiResponses.fromMessage(doctorService.updateDoctor(id, doctor), HttpStatus.OK);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id) {
        return ApiResponses.fromData(doctorService.getDoctorById(id), "Doctor not found");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteDoctor(@PathVariable UUID id) {
        return ApiResponses.fromMessage(doctorService.deleteDoctor(id), HttpStatus.OK);
    }
}
