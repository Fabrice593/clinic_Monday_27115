package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.services.DoctorService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping({"/api/doctor", "/api/doctors"})
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/save")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor) {
        return ApiResponses.fromMessage(doctorService.saveDoctor(doctor), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctor) {
        return ApiResponses.fromMessage(doctorService.updateDoctor(id, doctor), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/by-specialization")
    public ResponseEntity<?> getDoctorsBySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(name));
    }

    @GetMapping("/without-office")
    public ResponseEntity<?> getDoctorsWithoutOffice() {
        return ResponseEntity.ok(doctorService.getDoctorsWithoutOffice());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDoctors(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.searchDoctors(name));
    }

    @GetMapping("/workload")
    public ResponseEntity<?> getWorkload(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(doctorService.getWorkload(start, end));
    }

    @GetMapping("/{doctorId}/agenda")
    public ResponseEntity<?> getAgenda(@PathVariable UUID doctorId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponses.fromData(appointmentService.getDoctorAgenda(doctorId, date).orElse(null),
                DoctorService.DOCTOR_NOT_FOUND);
    }

    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<?> addSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        return ApiResponses.fromMessage(doctorService.addSpecialization(doctorId, specializationId), HttpStatus.OK);
    }

    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<?> removeSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        return ApiResponses.fromMessage(doctorService.removeSpecialization(doctorId, specializationId), HttpStatus.OK);
    }

    @PatchMapping("/{doctorId}/office")
    public ResponseEntity<?> changeOffice(@PathVariable UUID doctorId, @RequestParam String officeNumber) {
        return ApiResponses.fromMessage(doctorService.changeOffice(doctorId, officeNumber), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id) {
        return ApiResponses.fromData(doctorService.getDoctorById(id).orElse(null), DoctorService.DOCTOR_NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable UUID id) {
        return ApiResponses.fromMessage(doctorService.deleteDoctor(id), HttpStatus.OK);
    }
}
