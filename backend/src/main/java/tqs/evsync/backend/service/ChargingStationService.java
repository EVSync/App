package tqs.evsync.backend.service;

import org.springframework.stereotype.Service;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.repository.ChargingStationRepository;

import java.util.List;

@Service
public class ChargingStationService {

    private final ChargingStationRepository repo;

    public ChargingStationService(ChargingStationRepository repo) {
        this.repo = repo;
    }

    public List<ChargingStation> getAllStations() {
        return repo.findAll();
    }

    public List<ChargingStation> getStationsNear(double lat, double lon, double maxDistanceKm) {
        return repo.findAll().stream()
                .filter(s -> distanceKm(lat, lon, s.getLatitude(), s.getLongitude()) <= maxDistanceKm)
                .toList();
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2)) * 111;
    }
}
