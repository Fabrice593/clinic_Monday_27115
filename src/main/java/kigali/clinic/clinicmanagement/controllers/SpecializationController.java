package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Specialization;
import kigali.clinic.clinicmanagement.services.SpecializationService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/specialization")
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveSpecialization(@RequestBody Specialization specialization) {
        return ApiResponses.fromMessage(specializationService.saveSpecialization(specialization), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateSpecialization(@PathVariable UUID id,
                                                  @RequestBody Specialization specialization) {
        return ApiResponses.fromMessage(specializationService.updateSpecialization(id, specialization), HttpStatus.OK);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllSpecializations() {
        return ResponseEntity.ok(specializationService.getAllSpecializations());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSpecializationById(@PathVariable UUID id) {
        return ApiResponses.fromData(specializationService.getSpecializationById(id), "Specialization not found");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteSpecialization(@PathVariable UUID id) {
        return ApiResponses.fromMessage(specializationService.deleteSpecialization(id), HttpStatus.OK);
    }
}
