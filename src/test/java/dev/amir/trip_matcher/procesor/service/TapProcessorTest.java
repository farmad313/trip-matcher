package dev.amir.trip_matcher.procesor.service;

import dev.amir.trip_matcher.datastore.TripFareManager;
import dev.amir.trip_matcher.reader.model.TapModel;
import dev.amir.trip_matcher.writer.model.TripModel;
import dev.amir.trip_matcher.writer.model.TripStatus;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TapProcessorTest {

    private TapProcessor tapProcessor;
    private TripFareManager tripFareManager;

    @BeforeEach
    void setUp() {
        tripFareManager = new TripFareManager();
        tapProcessor = new TapProcessor(tripFareManager);
    }


    @Test
    @Description("Cancelled Trips Scenario: When a passenger taps ON and taps OFF at the same stop, the passenger should not be charged")
    void tripMaker_WhenAPassengerTapONAndTapOFFatSameStop_ShouldNotBeCharged() {
        //given
        LocalDateTime start = LocalDateTime.parse("2024-01-01T01:01:01");
        LocalDateTime end = LocalDateTime.parse("2024-01-01T01:01:02");

        List<TapModel> taps = List.of(
                new TapModel(1, start, "ON", "Stop1", "Company1", "Bus1", "PAN1"),
                new TapModel(2, end, "OFF", "Stop1", "Company1", "Bus1", "PAN1")
        );

        tripFareManager.addPriceRule("Stop1", "Stop1", new BigDecimal("0.0"));

        //when
        List<TripModel> expectedTrips = new ArrayList<>();
        expectedTrips.add(TripModel.builder()
                .started(start)
                .finished(end)
                .durationSecs(Duration.between(start, end).getSeconds())
                .fromStopId("Stop1")
                .toStopId("Stop1")
                .chargeAmount("0.0")
                .companyId("Company1")
                .busId("Bus1")
                .pan("PAN1")
                .status(TripStatus.valueOf("CANCELLED"))
                .build());

        //then
        List<TripModel> actualTrips = tapProcessor.tripMaker(taps);
        assertEquals(expectedTrips, actualTrips);
    }


    @Test
    @Description("Incomplete Trips Scenario: When a passenger taps ON and does not tap OFF till end of process period. The passenger should be charged the maximum fare")
    void tripMaker_WhenAPassengerTapONAndDoesNotTapOFFTillByProcessPeriodClose_ShouldBeChargedMaxFare() {
        //given
        LocalDateTime start = LocalDateTime.parse("2024-01-01T01:01:01");

        List<TapModel> taps = List.of(
                new TapModel(1, start, "ON", "Stop1", "Company1", "Bus1", "PAN1")
        );

        loadSampleTripFareIntoDatastore();

        //when
        List<TripModel> expectedTrips = new ArrayList<>();
        expectedTrips.add(TripModel.builder()
                .started(start)
                .finished(null)
                .durationSecs(0)
                .fromStopId("Stop1")
                .toStopId("UNKNOWN")
                .chargeAmount("7.30")
                .companyId("Company1")
                .busId("Bus1")
                .pan("PAN1")
                .status(TripStatus.valueOf("INCOMPLETE"))
                .build());

        //then
        List<TripModel> actualTrips = tapProcessor.tripMaker(taps);
        assertEquals(expectedTrips, actualTrips);
    }


    @Test
    @Description("Incomplete Trips Scenario: When a passenger taps ON and does not tap OFF, and then tap on to start a new trip. The passenger should be charged the maximum fare")
    void tripMaker_WhenAPassengerTapONAndDoesNotTapOFFAndStartANewTrip_ShouldBeChargedMaxFare() {
        //given
        LocalDateTime start1 = LocalDateTime.parse("2024-01-01T01:01:01");
        LocalDateTime start2 = LocalDateTime.parse("2024-01-01T03:01:01");

        List<TapModel> taps = List.of(
                new TapModel(1, start1, "ON", "Stop1", "Company1", "Bus1", "PAN1"),
                new TapModel(2, start2, "ON", "Stop2", "Company1", "Bus1", "PAN1")
        );

        loadSampleTripFareIntoDatastore();

        //when
        // First incomplete trip by starting at Stop1 and not finishing and starting a new trip at Stop2
        List<TripModel> expectedTrips = new ArrayList<>();
        expectedTrips.add(TripModel.builder()
                .started(start1)
                .finished(null)
                .durationSecs(0)
                .fromStopId("Stop1")
                .toStopId("UNKNOWN")
                .chargeAmount("7.30")
                .companyId("Company1")
                .busId("Bus1")
                .pan("PAN1")
                .status(TripStatus.valueOf("INCOMPLETE"))
                .build());

        // Second incomplete trip by starting at Stop2 and not finishing and closing the process time
        expectedTrips.add(TripModel.builder()
                .started(start2)
                .finished(null)
                .durationSecs(0)
                .fromStopId("Stop2")
                .toStopId("UNKNOWN")
                .chargeAmount("5.50")
                .companyId("Company1")
                .busId("Bus1")
                .pan("PAN1")
                .status(TripStatus.valueOf("INCOMPLETE"))
                .build());

        //then
        List<TripModel> actualTrips = tapProcessor.tripMaker(taps);
        assertEquals(expectedTrips, actualTrips);
    }

    @Test
    @Description("Complete Trips Scenario: When a passenger taps ON and taps OFF at different stops, the passenger should be charged the correct fare")
    void tripMaker_WhenAPassengerTapONAndTapOFFAtDifferentStops_ShouldBeChargedCorrectFare() {
        //given
        LocalDateTime start = LocalDateTime.parse("2024-01-01T01:01:01");
        LocalDateTime end = LocalDateTime.parse("2024-01-01T01:01:02");

        List<TapModel> taps = List.of(
                new TapModel(1, start, "ON", "Stop1", "Company1", "Bus1", "PAN1"),
                new TapModel(2, end, "OFF", "Stop2", "Company1", "Bus1", "PAN1")
        );

        loadSampleTripFareIntoDatastore();

        //when
        List<TripModel> expectedTrips = new ArrayList<>();
        expectedTrips.add(TripModel.builder()
                .started(start)
                .finished(end)
                .durationSecs(Duration.between(start, end).getSeconds())
                .fromStopId("Stop1")
                .toStopId("Stop2")
                .chargeAmount("3.25")
                .companyId("Company1")
                .busId("Bus1")
                .pan("PAN1")
                .status(TripStatus.valueOf("COMPLETED"))
                .build());

        //then
        List<TripModel> actualTrips = tapProcessor.tripMaker(taps);
        assertEquals(expectedTrips, actualTrips);
    }


    @Test
    @Description("Complete Trips Scenario For Two Passengers: When two passengers tap ON and tap OFF, the passengers should be charged the correct fare")
    void tripMaker_WhenTwoPassengersTapONAndTapOFFAtDifferentStops_ShouldBeChargedCorrectFare() {
        //given
        LocalDateTime start = LocalDateTime.parse("2024-01-01T01:01:01");
        LocalDateTime end = LocalDateTime.parse("2024-01-01T01:01:02");

        List<TapModel> taps = List.of(
                new TapModel(1, start, "ON", "Stop1", "Company1", "Bus1", "PAN1"),
                new TapModel(2, start, "ON", "Stop1", "Company1", "Bus1", "PAN2"),
                new TapModel(3, end, "OFF", "Stop2", "Company1", "Bus1", "PAN1"),
                new TapModel(4, end, "OFF", "Stop2", "Company1", "Bus1", "PAN2")
        );

        loadSampleTripFareIntoDatastore();

        //when
        List<TripModel> expectedTrips = new ArrayList<>();

        expectedTrips.add(TripModel.builder()
                .started(start)
                .finished(end)
                .durationSecs(Duration.between(start, end).getSeconds())
                .fromStopId("Stop1")
                .toStopId("Stop2")
                .chargeAmount("3.25")
                .companyId("Company1")
                .busId("Bus1")
                .pan("PAN2")
                .status(TripStatus.valueOf("COMPLETED"))
                .build());

        expectedTrips.add(TripModel.builder()
                .started(start)
                .finished(end)
                .durationSecs(Duration.between(start, end).getSeconds())
                .fromStopId("Stop1")
                .toStopId("Stop2")
                .chargeAmount("3.25")
                .companyId("Company1")
                .busId("Bus1")
                .pan("PAN1")
                .status(TripStatus.valueOf("COMPLETED"))
                .build());


        //then
        List<TripModel> actualTrips = tapProcessor.tripMaker(taps);
        assertEquals(expectedTrips, actualTrips);
    }


    private void loadSampleTripFareIntoDatastore() {
        tripFareManager.addPriceRule("Stop1", "Stop2", new BigDecimal("3.25"));
        tripFareManager.addPriceRule("Stop1", "Stop3", new BigDecimal("7.30"));
        tripFareManager.addPriceRule("Stop2", "Stop3", new BigDecimal("5.50"));
        tripFareManager.addPriceRule("Stop2", "Stop1", new BigDecimal("3.25"));
        tripFareManager.addPriceRule("Stop3", "Stop1", new BigDecimal("7.30"));
        tripFareManager.addPriceRule("Stop3", "Stop2", new BigDecimal("5.50"));
        tripFareManager.addPriceRule("Stop1", "Stop1", new BigDecimal("0.0"));
        tripFareManager.addPriceRule("Stop2", "Stop2", new BigDecimal("0.0"));
        tripFareManager.addPriceRule("Stop3", "Stop3", new BigDecimal("0.0"));
        tripFareManager.addPriceRule("Stop1", "UNKNOWN", new BigDecimal("7.30"));
        tripFareManager.addPriceRule("Stop2", "UNKNOWN", new BigDecimal("5.50"));
        tripFareManager.addPriceRule("Stop3", "UNKNOWN", new BigDecimal("7.30"));
    }
}
