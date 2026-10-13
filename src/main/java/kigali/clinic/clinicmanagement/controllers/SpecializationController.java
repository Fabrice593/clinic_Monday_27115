package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Specialization;
import kigali.clinic.clinicmanagement.services.SpecializationService;
import kigali.clinic.clinicmanagement.utils.ApiResponses.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/specialization", "/api/specializations"})
@RequiredArgsConstructor
public class SpecializationController {

    private final SpecializationService specializationService;

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Message define(@RequestBody Specialization.Details details) {
        specializationService.define(details);
        return new Message("Specialization is saved Successfully");
    }

    @PutMapping("/update/{id}")
    public Message revise(@PathVariable("id") UUID specializationId, @RequestBody Specialization.Details details) {
        specializationService.revise(specializationId, details);
        return new Message("Specialization is updated Successfully");
    }

    @GetMapping("/all")
    public List<Specialization> allSpecializations() {
        return specializationService.allSpecializations();
    }

    @GetMapping("/unused")
    public List<Specialization> unusedSpecializations() {
        return specializationService.unusedSpecializations();
    }

    @GetMapping("/{id}")
    public Specialization specializationById(@PathVariable("id") UUID specializationId) {
        return specializationService.specializationById(specializationId);
    }

    @DeleteMapping("/delete/{id}")
    public Message delete(@PathVariable("id") UUID specializationId) {
        specializationService.delete(specializationId);
        return new Message("Specialization is deleted Successfully");
    }
}
