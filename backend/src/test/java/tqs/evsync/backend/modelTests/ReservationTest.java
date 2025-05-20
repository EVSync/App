package tqs.evsync.backend.modelTests;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.Reservation;
import tqs.evsync.backend.model.Consumer;
import tqs.evsync.backend.model.enums.ReservationStatus;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationTest {

    @Test
    void testSettersAndGetters() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartTime("2025-05-20T18:00");
        reservation.setDuration(2.0);
        reservation.setStatus(ReservationStatus.PENDING);

        Consumer consumer = new Consumer();
        reservation.setConsumer(consumer);

        assertEquals(1L, reservation.getId());
        assertEquals("2025-05-20T18:00", reservation.getStartTime());
        assertEquals(2.0, reservation.getDuration());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertEquals(consumer, reservation.getConsumer());
    }
}
