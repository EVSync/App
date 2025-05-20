package tqs.evsync.backend.modelTests;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.enums.ChargingStationStatus;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ChargingStationTest {

    @Test
    void testSettersAndGetters() {
        ChargingStation station = new ChargingStation();
        station.setId(1L);
        station.setLatitude(40.0);
        station.setLongitude(-8.0);
        station.setStatus(ChargingStationStatus.AVAILABLE);

        assertEquals(1L, station.getId());
        assertEquals(40.0, station.getLatitude());
        assertEquals(-8.0, station.getLongitude());
        assertEquals(ChargingStationStatus.AVAILABLE, station.getStatus());
    }

    @Test
    void testOperatorSetterGetter() {
        ChargingStation station = new ChargingStation();
        Operator operator = new Operator();
        station.setOperator(operator);
        assertEquals(operator, station.getOperator());
    }

    @Test
    void testChargingOutlets() {
        ChargingStation station = new ChargingStation();
        station.setChargingOutlets(new ArrayList<>());
        ChargingOutlet outlet = new ChargingOutlet();
        station.addChargingOutlet(outlet);
        assertEquals(1, station.getChargingOutlets().size());
        station.removeChargingOutlet(outlet);
        assertEquals(0, station.getChargingOutlets().size());
    }
}
