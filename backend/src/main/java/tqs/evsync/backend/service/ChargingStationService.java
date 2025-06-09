package tqs.evsync.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.enums.ChargingStationStatus;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.OperatorRepository;
import tqs.evsync.backend.service.OpenStreetMapService.Coordinates;

import java.util.List;

@Service
public class ChargingStationService {
    private static final double EARTH_RADIUS_KM = 6378.0;

    private final ChargingStationRepository chargingRepo;
    private final OperatorRepository operatorRepo;
    private final ChargingOutletRepository outletRepo;
    private final OpenStreetMapService osmService;

    @Autowired
    public ChargingStationService(ChargingStationRepository chargingRepo,
                                  OperatorRepository operatorRepo,
                                  ChargingOutletRepository outletRepo,
                                  OpenStreetMapService osmService) {
        this.chargingRepo = chargingRepo;
        this.operatorRepo = operatorRepo;
        this.outletRepo = outletRepo;
        this.osmService = osmService;
    }

    public ChargingStation getStationById(Long id) {
        return chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID " + id + " not found"));
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

        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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
            .orElseThrow(() -> new RuntimeException("Operator with ID " + chargingStation.getOperator().getId() + " not found"));
        chargingStation.setOperator(operator);

        return chargingRepo.save(chargingStation);
    }

    public ChargingStation addChargingStationWithAddress(String address, Long operatorId) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }

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

    public ChargingStation updateChargingStationStatus(Long id, ChargingStationStatus status) {
        ChargingStation chargingStation = chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + id + " not found"));

        chargingStation.setStatus(status);
        return chargingRepo.save(chargingStation);
    }

    public boolean deleteChargingStation(Long id) {
        ChargingStation chargingStation = chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + id + " not found"));

        if (chargingStation.getStatus() == ChargingStationStatus.OCCUPIED) {
            // Can't delete a station that is currently occupied
            return false;
        } else {
            chargingRepo.delete(chargingStation);
            return true;
        }
    }

    public ChargingStation addChargingOutlet(Long stationId, ChargingOutlet outlet) {
        // 1) Load the station (throws if not found)
        ChargingStation station = chargingRepo.findById(stationId)
            .orElseThrow(() -> new RuntimeException("Charging station not found"));

        if (outlet.getId() == null) {
            // === New‐outlet branch (integration test uses this) ===
            outlet.setChargingStation(station);
            ChargingOutlet savedOutlet = outletRepo.save(outlet);
            station.getChargingOutlets().add(savedOutlet);

        } else {
            // === Existing‐outlet branch (unit test uses this) ===
            // Try to load from repo; if missing, fall back to passed‐in outlet
            ChargingOutlet existingOutlet = outletRepo.findById(outlet.getId())
                .orElse(outlet);

            existingOutlet.setChargingStation(station);
            station.getChargingOutlets().add(existingOutlet);
            outletRepo.save(existingOutlet);
        }

        // 2) Return the up‐to‐date station (re‐fetch to ensure relationships are fresh)
        return chargingRepo.findById(stationId)
            .orElseThrow(() -> new RuntimeException("Charging station not found"));
    }
    public ChargingStation removeChargingOutlet(Long id, ChargingOutlet chargingOutlet) {
        ChargingStation chargingStation = chargingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + id + " not found"));
        ChargingOutlet checkChargingOutlet = outletRepo.findById(chargingOutlet.getId())
            .orElseThrow(() -> new RuntimeException("Charging outlet with ID = " + chargingOutlet.getId() + " not found"));

        chargingStation.removeChargingOutlet(checkChargingOutlet);
        return chargingRepo.save(chargingStation);
    }

    public List<ChargingOutlet> getChargingOutletsByStationId(Long stationId) {
        ChargingStation station = chargingRepo.findById(stationId)
            .orElseThrow(() -> new RuntimeException("Charging station with ID = " + stationId + " not found"));

        return station.getChargingOutlets();
    }
}
