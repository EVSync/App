package tqs.evsync.backend.service;

import org.springframework.stereotype.Service;

@Service
public class EnvironmentalImpactService {
    public double calculateCO2(double energyUsed){
        return energyUsed * 0.25; 
    }

    public double CalKM(double energyUsed){
        return energyUsed * 6.0; 
    }

}
