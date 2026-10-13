package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Prescription;
import kigali.clinic.clinicmanagement.repositories.PrescriptionRepository.MedicineUsage;
import kigali.clinic.clinicmanagement.services.PrescriptionService;
import kigali.clinic.clinicmanagement.utils.ApiResponses.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Message issue(@RequestParam("appointmentId") UUID appointmentId,
                         @RequestBody Prescription.Details details) {
        prescriptionService.issue(appointmentId, details);
        return new Message("Prescription saved successfully");
    }

    @GetMapping("/appointment/{appointmentId}")
    public List<Prescription> prescriptionsOfAppointment(@PathVariable("appointmentId") UUID appointmentId) {
        return prescriptionService.prescriptionsOfAppointment(appointmentId);
    }

    @GetMapping("/patient/{ssNumber}")
    public List<Prescription> historyOfPatient(@PathVariable("ssNumber") String ssNumber) {
        return prescriptionService.historyOfPatient(ssNumber);
    }

    @GetMapping("/stats/top-medicines")
    public List<MedicineUsage> topMedicines() {
        return prescriptionService.rankMedicinesByUsage();
    }

    @PutMapping("/update/{id}")
    public Message revise(@PathVariable("id") UUID prescriptionId, @RequestBody Prescription.Details details) {
        prescriptionService.revise(prescriptionId, details);
        return new Message("Prescription updated successfully");
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") UUID prescriptionId) {
        prescriptionService.delete(prescriptionId);
        return new Message("Prescription deleted successfully");
    }
}
