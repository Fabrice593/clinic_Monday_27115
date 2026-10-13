package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Office;
import kigali.clinic.clinicmanagement.services.OfficeService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/office", "/api/offices"})
public class OfficeController {

    @Autowired
    private OfficeService officeService;

    @PostMapping("/save")
    public ResponseEntity<?> saveOffice(@RequestBody Office office) {
        return ApiResponses.fromMessage(officeService.saveOffice(office), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        return ApiResponses.fromMessage(officeService.updateOffice(id, office), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOffices() {
        return ResponseEntity.ok(officeService.getAllOffices());
    }

    @GetMapping("/free")
    public ResponseEntity<?> getFreeOffices() {
        return ResponseEntity.ok(officeService.getFreeOffices());
    }

    @GetMapping("/busiest")
    public ResponseEntity<?> getBusiestOffice() {
        return ApiResponses.fromData(officeService.getBusiestOffice().orElse(null), "No appointments yet");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOfficeById(@PathVariable UUID id) {
        return ApiResponses.fromData(officeService.getOfficeById(id).orElse(null), "Office not found");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOffice(@PathVariable UUID id) {
        return ApiResponses.fromMessage(officeService.deleteOffice(id), HttpStatus.OK);
    }
}
