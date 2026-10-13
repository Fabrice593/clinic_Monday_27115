package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Office;
import kigali.clinic.clinicmanagement.repositories.OfficeRepository.OfficeLoad;
import kigali.clinic.clinicmanagement.services.OfficeService;
import kigali.clinic.clinicmanagement.utils.ApiResponses.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/office", "/api/offices"})
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Message open(@RequestBody Office.Details details) {
        officeService.open(details);
        return new Message("Office is saved Successfully");
    }

    @PutMapping("/update/{id}")
    public Message revise(@PathVariable("id") UUID officeId, @RequestBody Office.Details details) {
        officeService.revise(officeId, details);
        return new Message("Office is updated Successfully");
    }

    @GetMapping("/all")
    public List<Office> allOffices() {
        return officeService.allOffices();
    }

    @GetMapping("/free")
    public List<Office> freeOffices() {
        return officeService.freeOffices();
    }

    @GetMapping("/busiest")
    public OfficeLoad busiestOffice() {
        return officeService.busiestOffice();
    }

    @GetMapping("/{id}")
    public Office officeById(@PathVariable("id") UUID officeId) {
        return officeService.officeById(officeId);
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") UUID officeId) {
        officeService.delete(officeId);
        return new Message("Office is deleted Successfully");
    }
}
