package tqs.evsync.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.OperatorRepository;
import tqs.evsync.backend.service.OpenStreetMapService.Coordinates;
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.enums.ChargingStationStatus;

import java.util.List;

@Service
public class ChargingStationService {
    private static final double EARTH_RADIUS_KM = 6378.0;

    @Autowired
    private ChargingStationRepository chargingRepo;
    
    @Autowired
    private OperatorRepository operatorRepo;

    @Autowired
    private ChargingOutletRepository chargingOutletRepo;

    @Autowired
    private OpenStreetMapService osmService;

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

    public List<ChargingStation> getAvailableStationsNear(double lat, double lon, double maxDistanceKm) {
        return chargingRepo.findAll().stream()
                .filter(s -> distanceKm(lat, lon, s.getLatitude(), s.getLongitude()) <= maxDistanceKm) 
                .filter(s -> s.getStatus() == ChargingStationStatus.AVAILABLE) 
                .toList();
    }

    public List<ChargingStation> getStationsNear(double lat, double lon, double maxDistanceKm) {
        return chargingRepo.findAll().stream()
                .filter(s -> distanceKm(lat, lon, s.getLatitude(), s.getLongitude()) <= maxDistanceKm)
                .toList();
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_RADIUS_KM * c;
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
            .orElseThrow(() -> new RuntimeException("ChargingStation with ID " + chargingStation.getOperator().getId() + " not found"));
        chargingStation.setOperator(operator);

        return chargingRepo.save(chargingStation);
    }

    public ChargingStation addChargingStationWithAddress(String address, Long operatorId){
        Operator operator = operatorRepo.findById(operatorId)
            .orElseThrow(() -> new RuntimeException("Operator with ID " + operatorId + " not found"));
        
        ChargingStation chargingStation = new ChargingStation();
        Coordinates coordinates = osmService.geocode(address);
        chargingStation.setLatitude(coordinates.lat());
        chargingStation.setLongitude(coordinates.lon());
        chargingStation.setStatus(ChargingStationStatus.AVAILABLE);
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
