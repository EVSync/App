package tqs.evsync.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/outlets")
public class ChargingOutletController {

    @Autowired
    private ChargingOutletRepository outletRepository;

    @Autowired
    private ChargingStationRepository stationRepository;

    @PostMapping("/{stationId}")
    public ResponseEntity<ChargingOutlet> createOutlet(
            @PathVariable Long stationId,
            @RequestBody ChargingOutlet newOutlet) {
        
        Optional<ChargingStation> station = stationRepository.findById(stationId);
        if (!station.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }

        station.get().addChargingOutlet(newOutlet);
        ChargingOutlet savedOutlet = outletRepository.save(newOutlet);

        return ResponseEntity.ok(savedOutlet);
    }

    @GetMapping
    public ResponseEntity<?> listOutlets() {
        return ResponseEntity.ok(outletRepository.findAll());
    }
}
