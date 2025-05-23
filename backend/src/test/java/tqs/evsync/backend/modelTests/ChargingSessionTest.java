package tqs.evsync.backend.modelTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingSession;
import tqs.evsync.backend.model.Reservation;

public class ChargingSessionTest {
    @Test
    void testGettersAndSetters() {
        ChargingSession session = new ChargingSession();
        
        session.setId(1L);
        assertEquals(1L, session.getId());
        
        Reservation reservation = new Reservation();
        session.setReservation(reservation);
        assertEquals(reservation, session.getReservation());
        
        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setCostPerHour(0.30);
        session.setOutlet(outlet);
        assertEquals(outlet, session.getOutlet());
        
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now);
        assertEquals(now, session.getStartTime());
        
        session.setEndTime(now.plusHours(2));
        assertEquals(now.plusHours(2), session.getEndTime());
        
        session.setEnergyConsumed(15.5);
        assertEquals(15.5, session.getEnergyConsumed());
    }

    @Test
    void testTotalCostWithNullTimes() {
        ChargingSession session = new ChargingSession();
        assertEquals(0.0, session.getTotalCost(), 0.001);
    }

    @Test
    void testNullOutletInCostCalculation() {
        ChargingSession session = new ChargingSession();
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now().plusHours(1));
        assertEquals(0.0, session.getTotalCost());
    }

    @Test
    void testNegativeDuration() {
        ChargingSession session = new ChargingSession();
        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setCostPerHour(1.0);
        session.setOutlet(outlet);
        
        LocalDateTime now = LocalDateTime.now();
        session.setStartTime(now);
        session.setEndTime(now.minusHours(1)); 
        
        assertEquals(0.0, session.getTotalCost());
    }
}
