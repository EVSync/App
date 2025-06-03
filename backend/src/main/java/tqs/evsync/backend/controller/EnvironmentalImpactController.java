package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tqs.evsync.backend.service.EnvironmentalImpactService;

import java.util.Map;

@RestController
@RequestMapping("/api/environment")
public class EnvironmentalImpactController {

    @Autowired
    private EnvironmentalImpactService impactService;

    @GetMapping("/impact")
    public Map<String, Object> getImpact(@RequestParam double energyUsed) {
        double co2 = impactService.calculateCO2(energyUsed);
        double km = impactService.CalKM(energyUsed);

        return Map.of(
            "energyUsedKWh", energyUsed,
            "co2AvoidedKg", co2,
            "kmEquivalent", km
        );
    }

}
