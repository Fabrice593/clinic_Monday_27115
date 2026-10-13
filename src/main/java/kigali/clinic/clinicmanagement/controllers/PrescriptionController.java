package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Prescription;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.services.PrescriptionService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/save")
    public ResponseEntity<?> savePrescription(@RequestParam UUID appointmentId, @RequestBody Prescription prescription) {
        return ApiResponses.fromMessage(prescriptionService.savePrescription(appointmentId, prescription), HttpStatus.CREATED);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getPrescriptionsOfAppointment(@PathVariable UUID appointmentId) {
        return ApiResponses.fromData(prescriptionService.getPrescriptionsOfAppointment(appointmentId).orElse(null),
                AppointmentService.APPOINTMENT_NOT_FOUND);
    }

    @GetMapping("/patient/{ssNumber}")
    public ResponseEntity<?> getPatientHistory(@PathVariable String ssNumber) {
        return ResponseEntity.ok(prescriptionService.getPatientHistory(ssNumber));
    }

    @GetMapping("/stats/top-medicines")
    public ResponseEntity<?> getTopMedicines() {
        return ResponseEntity.ok(prescriptionService.getTopMedicines());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePrescription(@PathVariable UUID id, @RequestBody Prescription prescription) {
        return ApiResponses.fromMessage(prescriptionService.updatePrescription(id, prescription), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePrescription(@PathVariable UUID id) {
        return ApiResponses.fromMessage(prescriptionService.deletePrescription(id), HttpStatus.OK);
    }
}
