package tqs.evsync.backend.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.model.enums.ChargingStationStatus;
import tqs.evsync.backend.service.ChargingStationService;

@Controller
@RequestMapping("/charging-station")
public class ChargingStationController {
    @Autowired
    private ChargingStationService chargingStationService;


    // GET ENDPOINTS

    @GetMapping
    public ResponseEntity<?> getAllChargingStations() {
        return ResponseEntity.ok(chargingStationService.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChargingStationById(@PathVariable Long id) {
        return ResponseEntity.ok(chargingStationService.getStationById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getChargingStationsNearby(@PathVariable double lat,@PathVariable double lon,@PathVariable double maxDistanceKm) {
        return ResponseEntity.ok(chargingStationService.getStationsNear(lat, lon, maxDistanceKm));
    }

    @GetMapping("/available-nearby")
    public ResponseEntity<?> getAvailableChargingStationsNearby(@PathVariable double lat,@PathVariable double lon,@PathVariable double maxDistanceKm) {
        return ResponseEntity.ok(chargingStationService.getAvailableStationsNear(lat, lon, maxDistanceKm));
    }

    @GetMapping("operator/{operatorId}")
    public ResponseEntity<?> getChargingStationsByOperator(@PathVariable Long operatorId) {
        List<ChargingStation> stations = chargingStationService.getStationsByOperator(operatorId);
        if (stations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }else {
            return ResponseEntity.ok(stations);
        }
    }
    

    
    // These endpoints are for the OPERATOR to update the status of the charging stations
    // PUT ENDPOINTS
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateChargingStationStatus(@PathVariable Long id, @RequestParam ChargingStationStatus status) {
        try{
            ChargingStation updatedStation = chargingStationService.updateChargingStationStatus(id, status);
            return ResponseEntity.ok(updatedStation);

        }catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{id}/add-charging-outlet")
    public ResponseEntity<?> addChargingOutlet(@PathVariable Long id, @RequestBody ChargingOutlet chargingOutlet) {
        try{
            ChargingStation updatedStation = chargingStationService.addChargingOutlet(id, chargingOutlet);
            return ResponseEntity.ok(updatedStation);

        }catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{id}/remove-charging-outlet")
    public ResponseEntity<?> removeChargingOutlet(@PathVariable Long id, @RequestBody ChargingOutlet chargingOutlet) {
        try{
            ChargingStation updatedStation = chargingStationService.removeChargingOutlet(id, chargingOutlet);
            return ResponseEntity.ok(updatedStation);

        }catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }
    

    // These endpoints are for the ADMIN to create and delete the charging stations
    // POST ENDPOINTS
    @PostMapping
    public ResponseEntity<?> addChargingStation(@RequestBody ChargingStation chargingStation) {
        ChargingStation savedStation = chargingStationService.addChargingStation(chargingStation);
        return ResponseEntity.ok(savedStation);
    }

    // DELETE ENDPOINTS
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChargingStation(@PathVariable Long id) {
        try {
            boolean deleted = chargingStationService.deleteChargingStation(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(400).body("Charging station cannot be deleted");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
