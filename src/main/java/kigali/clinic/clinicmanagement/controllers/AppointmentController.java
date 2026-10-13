package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/appointment", "/api/appointments"})
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/save")
    public ResponseEntity<?> saveAppointment(@RequestBody Appointment appointment) {
        return ApiResponses.fromMessage(appointmentService.saveAppointment(appointment), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable UUID id, @RequestBody Appointment appointment) {
        return ApiResponses.fromMessage(appointmentService.updateAppointment(id, appointment), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/by-status")
    public ResponseEntity<?> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    // a badly written date throws DateTimeParseException, which ApiExceptionHandler turns into 400
    @GetMapping("/between")
    public ResponseEntity<?> getAppointmentsBetween(@RequestParam String start, @RequestParam String end) {

        LocalDate startDate = LocalDate.parse(start.trim());
        LocalDate endDate = LocalDate.parse(end.trim());

        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body(Map.of("message", "End date must not be before start date"));
        }

        return ResponseEntity.ok(appointmentService.getAppointmentsBetween(startDate, endDate));
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<?> getAppointmentCountsByStatus() {
        return ResponseEntity.ok(appointmentService.getAppointmentCountsByStatus());
    }

    @PatchMapping("/cancel-day")
    public ResponseEntity<?> cancelDoctorDay(@RequestParam UUID doctorId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(Map.of("message", appointmentService.cancelDoctorDay(doctorId, date)));
    }

    // bonus: sort arrives as "property,direction", e.g. appointmentDate,desc
    @GetMapping("/page")
    public ResponseEntity<?> getAppointmentsPage(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "5") int size,
                                                 @RequestParam(defaultValue = "appointmentDate,asc") String sort) {

        if (page < 0 || size < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "Page must be 0 or more and size at least 1"));
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromOptionalString(parts[1].trim()).orElse(null)
                : Sort.Direction.ASC;

        if (property.isEmpty() || direction == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sort must look like appointmentDate,desc"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

        return ResponseEntity.ok(appointmentService.getAppointmentsPage(pageable));
    }

    @DeleteMapping("/cancelled-before")
    public ResponseEntity<?> deleteCancelledBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(Map.of("message", appointmentService.deleteCancelledBefore(date)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable UUID id, @RequestParam AppointmentStatus status) {
        return ApiResponses.fromMessage(appointmentService.changeStatus(id, status), HttpStatus.OK);
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<?> reschedule(@PathVariable UUID id,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate) {
        return ApiResponses.fromMessage(appointmentService.reschedule(id, newDate), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id) {
        return ApiResponses.fromData(appointmentService.getAppointmentById(id).orElse(null), AppointmentService.APPOINTMENT_NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable UUID id) {
        return ApiResponses.fromMessage(appointmentService.deleteAppointment(id), HttpStatus.OK);
    }
}
