package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Specialization;
import kigali.clinic.clinicmanagement.services.SpecializationService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/specialization", "/api/specializations"})
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @PostMapping("/save")
    public ResponseEntity<?> saveSpecialization(@RequestBody Specialization specialization) {
        return ApiResponses.fromMessage(specializationService.saveSpecialization(specialization), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSpecialization(@PathVariable UUID id,
                                                  @RequestBody Specialization specialization) {
        return ApiResponses.fromMessage(specializationService.updateSpecialization(id, specialization), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSpecializations() {
        return ResponseEntity.ok(specializationService.getAllSpecializations());
    }

    @GetMapping("/unused")
    public ResponseEntity<?> getUnusedSpecializations() {
        return ResponseEntity.ok(specializationService.getUnusedSpecializations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSpecializationById(@PathVariable UUID id) {
        return ApiResponses.fromData(specializationService.getSpecializationById(id).orElse(null), "Specialization not found");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSpecialization(@PathVariable UUID id) {
        return ApiResponses.fromMessage(specializationService.deleteSpecialization(id), HttpStatus.OK);
    }
}
