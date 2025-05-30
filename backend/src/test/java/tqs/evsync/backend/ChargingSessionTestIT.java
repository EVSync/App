package tqs.evsync.backend;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.model.enums.*;
import tqs.evsync.backend.repository.*;
import tqs.evsync.backend.model.ChargingSession;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChargingSessionTestIT {

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ReservationRepository reservationRepo;
    @Autowired
    private ChargingSessionRepository sessionRepo;
    @Autowired
    private ChargingOutletRepository outletRepo;
    @Autowired
    private ChargingStationRepository stationRepo;
    @Autowired
    private ConsumerRepository consumerRepo;

    private Reservation reservation;
    private ChargingOutlet outlet;

    @BeforeEach
    void setUp() {
        // 1) Create a station & outlet
        ChargingStation station = new ChargingStation();
        station.setLatitude(0.0);
        station.setLongitude(0.0);
        station.setStatus(ChargingStationStatus.AVAILABLE);
        station = stationRepo.save(station);

        outlet = new ChargingOutlet();
        outlet.setStatus(OutletStatus.AVAILABLE);
        outlet.setCostPerHour(2.0);
        outlet.setMaxPower(50);
        outletRepo.save(outlet);

        // 2) Create a consumer & a reservation
        Consumer consumer = new Consumer();
        consumer.setWallet(1000);
        consumer = consumerRepo.save(consumer);

        reservation = new Reservation();
        reservation.setConsumer(consumer);
        reservation.setStation(station);
        reservation.setOutlet(outlet);
        reservation.setStartTime(LocalDateTime.now().toString());
        reservation.setDuration(1.0);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation = reservationRepo.save(reservation);
    }

    @AfterEach
    void tearDown() {
        sessionRepo.deleteAll();
        reservationRepo.deleteAll();
        outletRepo.deleteAll();
        stationRepo.deleteAll();
        consumerRepo.deleteAll();
    }

    @Test
    void whenStartAndStopSession_thenReturnsCompletedSession() {
        // Start
        ResponseEntity<ChargingSession> startResp =
            rest.postForEntity(url("/api/sessions/start/" + reservation.getId()),
                               null,
                               ChargingSession.class);

        assertThat(startResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        ChargingSession started = startResp.getBody();
        assertThat(started).isNotNull();
        assertThat(started.getStatus()).isEqualTo(ChargingSessionStatus.ACTIVE);
        assertThat(started.getStartTime()).isNotNull();

        // Stop
        ResponseEntity<ChargingSession> stopResp =
            rest.postForEntity(url("/api/sessions/stop/" + started.getId()),
                               null,
                               ChargingSession.class);

        assertThat(stopResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        ChargingSession stopped = stopResp.getBody();
        assertThat(stopped.getStatus()).isEqualTo(ChargingSessionStatus.COMPLETED);
        assertThat(stopped.getEndTime()).isNotNull();
        assertThat(stopped.getTotalCost()).isGreaterThan(0.0);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
