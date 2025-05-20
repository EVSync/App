package tqs.evsync.backend.modelTests;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.Consumer;
import tqs.evsync.backend.model.Reservation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumerTest {

    @Test
    void testWalletGetterSetter() {
        Consumer consumer = new Consumer();
        consumer.setWallet(50);
        assertEquals(50, consumer.getWallet());
    }

    @Test
    void testReservationsGetterSetter() {
        Consumer consumer = new Consumer();
        Reservation r1 = new Reservation();
        Reservation r2 = new Reservation();
        consumer.setReservations(List.of(r1, r2));
        assertEquals(2, consumer.getReservations().size());
    }
    
}