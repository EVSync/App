package tqs.evsync.backend.unit;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.model.enums.ReservationStatus;
import tqs.evsync.backend.repository.*;
import tqs.evsync.backend.service.ReservationService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    // @Test
    // void testCreateReservationPending() {
    //     Consumer consumer = new Consumer();
    //     consumer.setId(1L);

    //     ChargingStation station = new ChargingStation();
    //     station.setId(2L);

    //     ConsumerRepository consumerRepo = mock(ConsumerRepository.class);
    //     ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
    //     ReservationRepository reservationRepo = mock(ReservationRepository.class);
    //     ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);

    //     when(outletRepo.findById(1L)).thenReturn(Optional.of(new ChargingOutlet()));
 
    //     when(consumerRepo.findById(1L)).thenReturn(Optional.of(consumer));
    //     when(stationRepo.findById(2L)).thenReturn(Optional.of(station));
    //     when(reservationRepo.save(any(Reservation.class)))
    //         .thenAnswer(invocation -> invocation.getArgument(0));

    //     ReservationService service = new ReservationService(reservationRepo, consumerRepo, stationRepo,outletRepo);

    //     Reservation r = service.createReservation(1L, 2L,1L ,"2025-05-20T18:00", 1.5);

    //     assertEquals(ReservationStatus.PENDING, r.getStatus());
    //     assertEquals(consumer, r.getConsumer());
    //     assertEquals(1.5, r.getDuration());
    //     assertEquals("2025-05-20T18:00", r.getStartTime());
    // }

    // @Test
    // void testConfirmReservation() { 
    //     Reservation reservation = new Reservation();
    //     reservation.setId(1L);
    //     reservation.setStatus(ReservationStatus.PENDING);

    //     ReservationRepository reservationRepo = mock(ReservationRepository.class);
    //     ConsumerRepository consumerRepo = mock(ConsumerRepository.class);
    //     ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
    //     ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);



    //     when(reservationRepo.findById(1L)).thenReturn(Optional.of(reservation));
    //     when(reservationRepo.save(any(Reservation.class)))
    //         .thenAnswer(invocation -> invocation.getArgument(0));

    //     ReservationService service = new ReservationService(reservationRepo, consumerRepo, stationRepo,outletRepo);

    //     Reservation updated = service.confirmReservation(1L);

    //     assertEquals(ReservationStatus.CONFIRMED, updated.getStatus());
    // }

    // @Test
    // void testCancelReservation() {
    //     Reservation reservation = new Reservation();
    //     reservation.setId(2L);
    //     reservation.setStatus(ReservationStatus.PENDING);

    //     ReservationRepository reservationRepo = mock(ReservationRepository.class);
    //     ConsumerRepository consumerRepo = mock(ConsumerRepository.class);
    //     ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
    //     ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);        when(reservationRepo.findById(2L)).thenReturn(Optional.of(reservation));
    //     when(reservationRepo.save(any(Reservation.class)))
    //         .thenAnswer(invocation -> invocation.getArgument(0));

    //     ReservationService service = new ReservationService(reservationRepo, consumerRepo, stationRepo,outletRepo);

    //     Reservation updated = service.cancelReservation(2L);

    //     assertEquals(ReservationStatus.CANCELLED, updated.getStatus());
    // }

    // @Test
    // void testWalletDecreaseOnPayment() {
    //     Consumer consumer = new Consumer();
    //     consumer.setId(1L);
    //     consumer.setWallet(100); 

    //     Reservation reservation = new Reservation();
    //     reservation.setId(5L);
    //     reservation.setDuration(2.0);
    //     reservation.setStatus(ReservationStatus.PENDING);
    //     reservation.setConsumer(consumer);

    //     ReservationRepository reservationRepo = mock(ReservationRepository.class);
    //     ConsumerRepository consumerRepo = mock(ConsumerRepository.class);
    //     ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
    //     ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);        when(reservationRepo.findById(5L)).thenReturn(Optional.of(reservation));
    //     when(reservationRepo.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

    //     ReservationService service = new ReservationService(reservationRepo, consumerRepo, stationRepo,outletRepo);

    //     double pricePerHour = 10.0;

    //     Reservation updated = service.processPayment(5L, pricePerHour);

    //     assertEquals(80, updated.getConsumer().getWallet());
    //     assertEquals(ReservationStatus.CONFIRMED, updated.getStatus());
    // }

    // @Test
    // void testInsufficientWalletThrowsException() {
    //     Consumer consumer = new Consumer();
    //     consumer.setId(1L);
    //     consumer.setWallet(5);

    //     Reservation reservation = new Reservation();
    //     reservation.setId(10L);
    //     reservation.setDuration(2.0);
    //     reservation.setStatus(ReservationStatus.PENDING);
    //     reservation.setConsumer(consumer);

    //     ReservationRepository reservationRepo = mock(ReservationRepository.class);
    //     ConsumerRepository consumerRepo = mock(ConsumerRepository.class);
    //     ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
    //     ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);        when(reservationRepo.findById(10L)).thenReturn(Optional.of(reservation));

    //     ReservationService service = new ReservationService(reservationRepo, consumerRepo, stationRepo,outletRepo);

    //     assertThrows(IllegalStateException.class, () -> {
    //         service.processPayment(10L, 10.0);
    //     });
    // }

    // @Test
    // void testCreateReservationWithInvalidStation() {
    //     Consumer consumer = new Consumer();
    //     consumer.setId(1L);

    //     ReservationRepository reservationRepo = mock(ReservationRepository.class);
    //     ConsumerRepository consumerRepo = mock(ConsumerRepository.class);
    //     ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
    //     ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);

    //     when(consumerRepo.findById(1L)).thenReturn(Optional.of(consumer));
    //     when(stationRepo.findById(99L)).thenReturn(Optional.empty());

    //     ReservationService service = new ReservationService(reservationRepo, consumerRepo, stationRepo,outletRepo);

    //     assertThrows(IllegalArgumentException.class, () -> {
    //         service.createReservation(1L, 99L,1L, "2025-06-01T09:00", 1.0);
    //     });
    // }

    // @Test
    // void testDuplicateReservationOnSameStation() {
    //     Consumer consumer = new Consumer();
    //     consumer.setId(1L);

    //     ChargingStation station = new ChargingStation();
    //     station.setId(2L);

    //     Reservation existing = new Reservation();
    //     existing.setConsumer(consumer);
    //     existing.setStartTime("2025-06-01T09:00");
    //     existing.setDuration(1.0);
    //     existing.setStatus(ReservationStatus.CONFIRMED);

    //     ReservationRepository reservationRepo = mock(ReservationRepository.class);
    //     ConsumerRepository consumerRepo = mock(ConsumerRepository.class);
    //     ChargingStationRepository stationRepo = mock(ChargingStationRepository.class);
    //     ChargingOutletRepository outletRepo = mock(ChargingOutletRepository.class);
        
    //     when(consumerRepo.findById(1L)).thenReturn(Optional.of(consumer));
    //     when(stationRepo.findById(2L)).thenReturn(Optional.of(station));
    //     when(reservationRepo.save(any(Reservation.class)))
    //         .thenAnswer(i -> {
    //             // simular que o mesmo consumidor já tem uma reserva ativa nesta estação
    //             throw new IllegalStateException("Duplicate reservation not allowed");
    //         });

    //         ReservationService service = new ReservationService(reservationRepo, consumerRepo, stationRepo,outletRepo);

    //     assertThrows(IllegalStateException.class, () -> {
    //         service.createReservation(1L, 2L,1L, "2025-06-01T09:00", 1.0);
    //     });
    // }

}
