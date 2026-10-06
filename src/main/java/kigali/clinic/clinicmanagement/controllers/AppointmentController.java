package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = {"/api/appointment", "/api/appointments"})
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveAppointment(@RequestBody Appointment appointment) {
        return ApiResponses.fromMessage(appointmentService.saveAppointment(appointment), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAppointment(@PathVariable UUID id, @RequestBody Appointment appointment) {
        return ApiResponses.fromMessage(appointmentService.updateAppointment(id, appointment), HttpStatus.OK);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping(value = "/by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentsByStatus(@RequestParam String status) {

        String normalized = status.trim().toUpperCase();

        if (!appointmentService.isValidStatus(normalized)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Status must be one of SCHEDULED, CONFIRMED, COMPLETED, CANCELLED"));
        }

        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(normalized));
    }

    @GetMapping(value = "/between", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentsBetween(@RequestParam String start, @RequestParam String end) {

        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = LocalDate.parse(start.trim());
            endDate = LocalDate.parse(end.trim());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Dates must use the format yyyy-MM-dd"));
        }

        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body(Map.of("message", "End date must not be before start date"));
        }

        return ResponseEntity.ok(appointmentService.getAppointmentsBetween(
                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id) {
        return ApiResponses.fromData(appointmentService.getAppointmentById(id), "Appointment not found");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAppointment(@PathVariable UUID id) {
        return ApiResponses.fromMessage(appointmentService.deleteAppointment(id), HttpStatus.OK);
    }
}
