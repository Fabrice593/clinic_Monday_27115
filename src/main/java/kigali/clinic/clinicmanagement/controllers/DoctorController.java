package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository.Workload;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.services.DoctorService;
import kigali.clinic.clinicmanagement.utils.ApiResponses.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/doctor", "/api/doctors"})
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Message register(@RequestBody Doctor.Details details) {
        doctorService.register(details);
        return new Message("Doctor is saved Successfully");
    }

    @PutMapping("/update/{id}")
    public Message revise(@PathVariable("id") UUID doctorId, @RequestBody Doctor.Details details) {
        doctorService.revise(doctorId, details);
        return new Message("Doctor is updated Successfully");
    }

    @GetMapping("/all")
    public List<Doctor> allDoctors() {
        return doctorService.allDoctors();
    }

    @GetMapping("/by-specialization")
    public List<Doctor> withSpecialization(@RequestParam("name") String specializationName) {
        return doctorService.withSpecialization(specializationName);
    }

    @GetMapping("/without-office")
    public List<Doctor> withoutOffice() {
        return doctorService.withoutOffice();
    }

    @GetMapping("/search")
    public List<Doctor> searchByName(@RequestParam("name") String nameFragment) {
        return doctorService.searchByName(nameFragment);
    }

    @GetMapping("/workload")
    public List<Workload> completedWorkload(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate firstDay,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastDay) {
        return doctorService.completedWorkload(firstDay, lastDay);
    }

    @GetMapping("/{doctorId}/agenda")
    public List<Appointment> agenda(@PathVariable("doctorId") UUID doctorId,
                                    @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return appointmentService.agendaOf(doctorId, day);
    }

    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public Message addSpecialization(@PathVariable("doctorId") UUID doctorId,
                                     @PathVariable("specializationId") UUID specializationId) {
        doctorService.addSpecialization(doctorId, specializationId);
        return new Message("Specialization added to the doctor");
    }

    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public Message removeSpecialization(@PathVariable("doctorId") UUID doctorId,
                                        @PathVariable("specializationId") UUID specializationId) {
        doctorService.removeSpecialization(doctorId, specializationId);
        return new Message("Specialization removed from the doctor");
    }

    @PatchMapping("/{doctorId}/office")
    public Message moveToOffice(@PathVariable("doctorId") UUID doctorId,
                                @RequestParam("officeNumber") String officeNumber) {
        doctorService.moveToOffice(doctorId, officeNumber);
        return new Message("Office changed successfully");
    }

    @GetMapping("/{id}")
    public Doctor doctorById(@PathVariable("id") UUID doctorId) {
        return doctorService.doctorById(doctorId);
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") UUID doctorId) {
        doctorService.delete(doctorId);
        return new Message("Doctor is deleted Successfully");
    }
}
