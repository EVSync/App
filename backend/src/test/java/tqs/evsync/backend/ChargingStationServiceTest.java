package tqs.evsync.backend;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.service.ChargingStationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ChargingStationServiceTest {

    @Test
    void testGetStationsNear() {
        ChargingStationRepository repo = mock(ChargingStationRepository.class);
        ChargingStationService service = new ChargingStationService(repo);

        ChargingStation s1 = new ChargingStation();
        s1.setId(1L); s1.setLatitude(40.64); s1.setLongitude(-8.65);

        ChargingStation s2 = new ChargingStation();
        s2.setId(2L); s2.setLatitude(42.0); s2.setLongitude(-9.0);

        when(repo.findAll()).thenReturn(List.of(s1, s2));

        List<ChargingStation> result = service.getStationsNear(40.64, -8.65, 10.0);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
