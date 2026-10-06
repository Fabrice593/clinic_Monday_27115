package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Office;
import kigali.clinic.clinicmanagement.services.OfficeService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/office")
public class OfficeController {

    @Autowired
    private OfficeService officeService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveOffice(@RequestBody Office office) {
        return ApiResponses.fromMessage(officeService.saveOffice(office), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        return ApiResponses.fromMessage(officeService.updateOffice(id, office), HttpStatus.OK);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllOffices() {
        return ResponseEntity.ok(officeService.getAllOffices());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOfficeById(@PathVariable UUID id) {
        return ApiResponses.fromData(officeService.getOfficeById(id), "Office not found");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteOffice(@PathVariable UUID id) {
        return ApiResponses.fromMessage(officeService.deleteOffice(id), HttpStatus.OK);
    }
}
