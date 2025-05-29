package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;

@RestController
@RequestMapping("/api/outlets")
public class ChargingOutletController {

    @Autowired
    private ChargingOutletRepository outletRepository;

    @Autowired
    private ChargingStationRepository stationRepository;

    @PostMapping
    public ResponseEntity<?> createOutlet(@RequestBody ChargingOutlet outlet) {
        if (outlet.getChargingStation() == null || outlet.getChargingStation().getId() == null ||
            !stationRepository.existsById(outlet.getChargingStation().getId())) {
            return ResponseEntity.badRequest().body("Invalid or missing charging station");
        }

        return ResponseEntity.ok(outletRepository.save(outlet));
    }

    @GetMapping
    public ResponseEntity<?> listOutlets() {
        return ResponseEntity.ok(outletRepository.findAll());
    }
}
