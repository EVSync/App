package tqs.evsync.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.enums.ChargingStationStatus;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.OperatorRepository;
import tqs.evsync.backend.service.ChargingStationService;
import tqs.evsync.backend.service.OpenStreetMapService;
import org.mockito.InjectMocks;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargingStationServiceTest {

    @Mock
    private ChargingStationRepository stationRepo;

    @Mock
    private OperatorRepository operatorRepo;

    @Mock
    private ChargingOutletRepository outletRepo;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private OpenStreetMapService osmService;

    @InjectMocks
    private ChargingStationService service;

    private ChargingStation station;
    private Operator operator;
    private ChargingOutlet outlet;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        
        osmService = new OpenStreetMapService(restTemplateBuilder);

        operator = new Operator();
        operator.setId(1L);

        station = new ChargingStation();
        station.setId(1L);
        station.setLatitude(38.7223);
        station.setLongitude(-9.1393);
        station.setStatus(ChargingStationStatus.AVAILABLE);
        station.setOperator(operator);

        outlet = new ChargingOutlet();
        outlet.setId(1L);
    }

    @Test
    void testGetStationById_Found() {
        when(stationRepo.findById(1L)).thenReturn(Optional.of(station));
        
        ChargingStation result = service.getStationById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetStationById_NotFound() {
        when(stationRepo.findById(1L)).thenReturn(Optional.empty());
        
        ChargingStation result = service.getStationById(1L);
        assertNull(result);
    }

    @Test
    void testGetAllStations() {
        when(stationRepo.findAll()).thenReturn(List.of(station));
        
        List<ChargingStation> result = service.getAllStations();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetAvailableStationsNear() {
        ChargingStation occupiedStation = new ChargingStation();
        occupiedStation.setId(2L);
        occupiedStation.setLatitude(38.7224);
        occupiedStation.setLongitude(-9.1394);
        occupiedStation.setStatus(ChargingStationStatus.OCCUPIED);

        when(stationRepo.findAll()).thenReturn(List.of(station, occupiedStation));
        
        List<ChargingStation> result = service.getAvailableStationsNear(38.7223, -9.1393, 1.0);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetStationsNear() {
        ChargingStation farStation = new ChargingStation();
        farStation.setId(2L);
        farStation.setLatitude(48.8566);
        farStation.setLongitude(2.3522);

        when(stationRepo.findAll()).thenReturn(List.of(station, farStation));
        
        List<ChargingStation> result = service.getStationsNear(38.7223, -9.1393, 10.0);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetStationsByOperator_Exists() {
        when(operatorRepo.existsById(1L)).thenReturn(true);
        when(stationRepo.findAll()).thenReturn(List.of(station));
        
        List<ChargingStation> result = service.getStationsByOperator(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetStationsByOperator_NotExists() {
        when(operatorRepo.existsById(1L)).thenReturn(false);
        
        List<ChargingStation> result = service.getStationsByOperator(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddChargingStation() {
        when(operatorRepo.findById(1L)).thenReturn(Optional.of(operator));
        when(stationRepo.save(any(ChargingStation.class))).thenReturn(station);
        
        ChargingStation result = service.addChargingStation(station);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testAddChargingStationWithAddress() {
        // Need to complete test (runned with errors)
    }


    @Test
    void testUpdateChargingStationStatus() {
        when(stationRepo.findById(1L)).thenReturn(Optional.of(station));
        when(stationRepo.save(any(ChargingStation.class))).thenReturn(station);
        
        ChargingStation result = service.updateChargingStationStatus(1L, ChargingStationStatus.OCCUPIED);
        assertEquals(ChargingStationStatus.OCCUPIED, result.getStatus());
    }

    @Test
    void testDeleteChargingStation_Success() {
        when(stationRepo.findById(1L)).thenReturn(Optional.of(station));
        
        boolean result = service.deleteChargingStation(1L);
        assertTrue(result);
        verify(stationRepo).delete(station);
    }

    @Test
    void testDeleteChargingStation_FailWhenOccupied() {
        station.setStatus(ChargingStationStatus.OCCUPIED);
        when(stationRepo.findById(1L)).thenReturn(Optional.of(station));
        
        boolean result = service.deleteChargingStation(1L);
        assertFalse(result);
        verify(stationRepo, never()).delete(any());
    }

    @Test
    void testAddChargingOutlet() {
        when(stationRepo.findById(1L)).thenReturn(Optional.of(station));
        when(outletRepo.findById(1L)).thenReturn(Optional.of(outlet));
        when(stationRepo.save(any(ChargingStation.class))).thenReturn(station);
        
        ChargingStation result = service.addChargingOutlet(1L, outlet);
        assertNotNull(result);
        assertTrue(result.getChargingOutlets().contains(outlet));
    }

    @Test
    void testRemoveChargingOutlet() {
        station.addChargingOutlet(outlet);
        when(stationRepo.findById(1L)).thenReturn(Optional.of(station));
        when(outletRepo.findById(1L)).thenReturn(Optional.of(outlet));
        when(stationRepo.save(any(ChargingStation.class))).thenReturn(station);
        
        ChargingStation result = service.removeChargingOutlet(1L, outlet);
        assertNotNull(result);
        assertFalse(result.getChargingOutlets().contains(outlet));
    }
}