package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository.StatusCount;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.utils.ApiResponses.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Validations.ensure;

@RestController
@RequestMapping({"/api/appointment", "/api/appointments"})
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Message book(@RequestBody Appointment.Details details) {
        appointmentService.book(details);
        return new Message("Appointment is saved Successfully");
    }

    @PutMapping("/update/{id}")
    public Message revise(@PathVariable("id") UUID appointmentId, @RequestBody Appointment.Details details) {
        appointmentService.revise(appointmentId, details);
        return new Message("Appointment is updated Successfully");
    }

    @GetMapping("/all")
    public List<Appointment> allAppointments() {
        return appointmentService.allAppointments();
    }

    @GetMapping("/by-status")
    public List<Appointment> withStatus(@RequestParam("status") AppointmentStatus status) {
        return appointmentService.withStatus(status);
    }

    @GetMapping("/between")
    public List<Appointment> between(@RequestParam("start") String start, @RequestParam("end") String end) {
        return appointmentService.between(LocalDate.parse(start.trim()), LocalDate.parse(end.trim()));
    }

    @GetMapping("/stats/by-status")
    public List<StatusCount> countsPerStatus() {
        return appointmentService.countsPerStatus();
    }

    @PatchMapping("/cancel-day")
    public Message cancelDoctorDay(@RequestParam("doctorId") UUID doctorId,
                                   @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return new Message(appointmentService.cancelDoctorDay(doctorId, day) + " appointments cancelled");
    }

    @GetMapping("/page")
    public Page<Appointment> page(@RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "5") int size,
                                  @RequestParam(value = "sort", defaultValue = "appointmentDate,asc") String sort) {
        return appointmentService.page(pageRequest(page, size, sort));
    }

    @DeleteMapping("/cancelled-before")
    public Message deleteCancelledBefore(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return new Message(appointmentService.deleteCancelledBefore(day) + " appointments deleted");
    }

    @PatchMapping("/{id}/status")
    public Message changeStatus(@PathVariable("id") UUID appointmentId,
                                @RequestParam("status") AppointmentStatus status) {
        appointmentService.changeStatus(appointmentId, status);
        return new Message("Appointment status updated successfully");
    }

    @PatchMapping("/{id}/reschedule")
    public Message reschedule(@PathVariable("id") UUID appointmentId,
                              @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDay) {
        appointmentService.reschedule(appointmentId, newDay);
        return new Message("Appointment rescheduled successfully");
    }

    @GetMapping("/{id}")
    public Appointment appointmentById(@PathVariable("id") UUID appointmentId) {
        return appointmentService.appointmentById(appointmentId);
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") UUID appointmentId) {
        appointmentService.delete(appointmentId);
        return new Message("Appointment is deleted Successfully");
    }

    private static Pageable pageRequest(int page, int size, String sort) {
        ensure(page >= 0 && size >= 1, "Page must be 0 or more and size at least 1");

        String[] sortParts = sort.split(",");
        String property = sortParts.length > 0 ? sortParts[0].trim() : "";
        Optional<Sort.Direction> direction = sortParts.length > 1
                ? Sort.Direction.fromOptionalString(sortParts[1].trim())
                : Optional.of(Sort.Direction.ASC);

        ensure(!property.isEmpty() && direction.isPresent(), "Sort must look like appointmentDate,desc");

        return PageRequest.of(page, size, Sort.by(direction.get(), property));
    }
}
