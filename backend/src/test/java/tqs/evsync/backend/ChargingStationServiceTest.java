package tqs.evsync.backend;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.model.enums.ChargingStationStatus;
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
        s1.setId(1L); s1.setLatitude(38.7223); s1.setLongitude(-9.1393); s1.setStatus(ChargingStationStatus.AVAILABLE);

        ChargingStation s2 = new ChargingStation();
        s2.setId(2L); s2.setLatitude(48.8566); s2.setLongitude(2.3522); s2.setStatus(ChargingStationStatus.AVAILABLE);

        when(stationRepo.findAll()).thenReturn(List.of(s1, s2));

        List<ChargingStation> result = service.getStationsNear(38.7223, -9.1393, 10.0);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
