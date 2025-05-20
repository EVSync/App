package tqs.evsync.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.OperatorRepository;
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.enums.ChargingStationStatus;

import java.util.List;

@Service
public class ChargingStationService {

    @Autowired
    private ChargingStationRepository chargingRepo;
    
    @Autowired
    private OperatorRepository operatorRepo;

    @Autowired
    private ChargingOutletRepository chargingOutletRepo;

    public ChargingStationService(ChargingStationRepository chargingRepo, OperatorRepository operatorRepo, ChargingOutletRepository chargingOutletRepo) {
        this.chargingRepo = chargingRepo;
        this.operatorRepo = operatorRepo;
        this.chargingOutletRepo = chargingOutletRepo;
    }

    public ChargingStation getStationById(Long id) {
        return chargingRepo.findById(id).orElse(null);
    }

    public List<ChargingStation> getAllStations() {
        return chargingRepo.findAll();
    }

    public List<ChargingStation> getStationsNear(double lat, double lon, double maxDistanceKm) {
        return chargingRepo.findAll().stream()
                .filter(s -> distanceKm(lat, lon, s.getLatitude(), s.getLongitude()) <= maxDistanceKm)
                .toList();
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2)) * 111;
    }

    public List<ChargingStation> getStationsByOperator(Long operatorId) {
        if (!operatorRepo.existsById(operatorId)) {
            return List.of();
        }
        
        return chargingRepo.findAll().stream()
                .filter(s -> s.getOperator().getId().equals(operatorId))
                .toList();
    }

    public ChargingStation addChargingStation(ChargingStation chargingStation) {
        Operator operator = operatorRepo.findById(chargingStation.getOperator().getId())
            .orElseThrow(() -> new RuntimeException("Meal with ID " + chargingStation.getOperator().getId() + " not found"));
        chargingStation.setOperator(operator);

        return chargingRepo.save(chargingStation);
    }
    
    public ChargingStation updateChargingStationStatus(Long id,  ChargingStationStatus status) {
        ChargingStation chargingStation = chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + id + " not found"));
        
        chargingStation.setStatus(status);
        return chargingRepo.save(chargingStation);
    }

    public boolean deleteChargingStation(Long id) {
        ChargingStation chargingStation = chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + id + " not found"));
        
        if (chargingStation.getStatus() == ChargingStationStatus.OCCUPIED) {
            // can't delete a station that is in use
            return false;
        }else{
            chargingRepo.delete(chargingStation);
            return true;
        }
    }

    public ChargingStation addChargingOutlet(Long id, ChargingOutlet chargingOutlet) {
        ChargingStation chargingStation = chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + id + " not found"));
        ChargingOutlet checkChargingOutlet = chargingOutletRepo.findById(chargingOutlet.getId())
            .orElseThrow(() -> new RuntimeException("Charging outlet with ID = " + chargingOutlet.getId() + " not found"));

        chargingStation.addChargingOutlet(checkChargingOutlet);
        
        return chargingRepo.save(chargingStation);
    }

    public ChargingStation removeChargingOutlet(Long id, ChargingOutlet chargingOutlet) {
        ChargingStation chargingStation = chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + id + " not found"));
        ChargingOutlet checkChargingOutlet = chargingOutletRepo.findById(chargingOutlet.getId())
            .orElseThrow(() -> new RuntimeException("Charging outlet with ID = " + chargingOutlet.getId() + " not found"));

        chargingStation.removeChargingOutlet(checkChargingOutlet);
        
        return chargingRepo.save(chargingStation);
    }

}
