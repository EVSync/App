package tqs.evsync.backend.modelTests;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.ChargingOutlet;

import static org.junit.jupiter.api.Assertions.*;

public class ChargingOutletTest {

    @Test
    void testIdGetterSetter() {
        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setId(42L);
        assertEquals(42L, outlet.getId());
    }

    @Test
    void testCostPerHourGetterSetter() {
        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setCostPerHour(5.5);
        assertEquals(5.5, outlet.getCostPerHour());
    }

    @Test
    void testMaxPowerGetterSetter() {
        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setMaxPower(22);
        assertEquals(22, outlet.getMaxPower());
    }
}