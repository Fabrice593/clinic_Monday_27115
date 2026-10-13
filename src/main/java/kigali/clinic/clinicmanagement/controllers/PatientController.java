package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.services.PatientService;
import kigali.clinic.clinicmanagement.utils.ApiResponses.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/patient", "/api/patients"})
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Message register(@RequestBody Patient.Details details) {
        patientService.register(details);
        return new Message("Patient is saved Successfully");
    }

    @PutMapping("/update/{id}")
    public Message revise(@PathVariable("id") UUID patientId, @RequestBody Patient.Details details) {
        patientService.revise(patientId, details);
        return new Message("Patient is updated Successfully");
    }

    @GetMapping("/all")
    public List<Patient> allPatients() {
        return patientService.allPatients();
    }

    @GetMapping("/by-last-name")
    public List<Patient> withLastName(@RequestParam("lastName") String lastName) {
        return patientService.withLastName(lastName);
    }

    @GetMapping("/frequent")
    public List<Patient> frequentPatients(@RequestParam("min") long minimumVisits) {
        return patientService.frequentPatients(minimumVisits);
    }

    @GetMapping("/of-doctor/{doctorId}")
    public List<Patient> patientsOfDoctor(@PathVariable("doctorId") UUID doctorId) {
        return patientService.patientsOfDoctor(doctorId);
    }

    @GetMapping("/{ssNumber}/upcoming")
    public List<Appointment> upcomingAppointments(@PathVariable("ssNumber") String ssNumber) {
        return appointmentService.upcomingFor(ssNumber);
    }

    @GetMapping("/{id}")
    public Patient patientById(@PathVariable("id") UUID patientId) {
        return patientService.patientById(patientId);
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") UUID patientId) {
        patientService.delete(patientId);
        return new Message("Patient is deleted Successfully");
    }
}
