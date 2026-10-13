package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.services.DashboardService;
import kigali.clinic.clinicmanagement.services.DashboardService.Summary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public Summary summary() {
        return dashboardService.summary();
    }
}
