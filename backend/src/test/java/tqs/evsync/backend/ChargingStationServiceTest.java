package tqs.evsync.backend;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.OperatorRepository;
import tqs.evsync.backend.service.ChargingStationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ChargingStationServiceTest {

    @Test
    void testGetStationsNear() {
        ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
        OperatorRepository operatorRepo = mock(OperatorRepository.class);
        ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);
        ChargingStationService service = new ChargingStationService(stationRepo, operatorRepo, outletRepo);

        ChargingStation s1 = new ChargingStation();
        s1.setId(1L); s1.setLatitude(40.64); s1.setLongitude(-8.65);

        ChargingStation s2 = new ChargingStation();
        s2.setId(2L); s2.setLatitude(42.0); s2.setLongitude(-9.0);

        when(stationRepo.findAll()).thenReturn(List.of(s1, s2));

        List<ChargingStation> result = service.getStationsNear(40.64, -8.65, 10.0);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
